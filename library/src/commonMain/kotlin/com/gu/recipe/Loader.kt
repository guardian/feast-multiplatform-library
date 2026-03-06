package com.gu.recipe

import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Make an HTTP request to the network.  Care should be taken to fill in all request headers and to pass back all response headers.
 * If the device is not on the network, a synthetic "504" should be returned without body content.
 * This should NOT throw on error. Rather log the error and return a status code of "504" with other fields empty
 */
expect suspend fun makeHttpRequest(method: String, url: String, body: String?=null, headers:Map<String, String>?=null): Loader.HttpResponse

/**
 * Load the cached density data and timestamp from persistent storage.  If there is an error, or no
 * data to load, then return `null`.  This should NOT throw on error. Rather log the error and return null.
 */
expect fun readCachedDensityData(): Loader.CachedDensityData?
expect fun writeCachedDensityData(data: Loader.CachedDensityData): Result<Unit>
expect fun loadErrorOccurred(message: String): Unit

object Loader {
    data class HttpResponse(
        val statusCode: Int,
        val responseHeaders:Map<String, String>? = null,
        val body: String? = null
    )

    data class CachedDensityData(
        val timestamp: Instant,
        val content: String
    )

    private val httpDateFormat = LocalDateTime.Format {
        dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)  // "Tue"
        chars(", ")
        day(padding = Padding.ZERO)
        char(' ')
        monthName(MonthNames.ENGLISH_ABBREVIATED)       // "Nov"
        char(' ')
        year()
        char(' ')
        hour(); char(':'); minute(); char(':'); second()
        chars(" GMT")
    }

    /**
     * Creates a new conversion session, loading data from the network if available and using
     * cached data if not.
     *
     * Arguments:
     *  - url: URL of the density data on the CDN. This is different for CODE and PROD, assumption is that it's in
     *  Firebase config
     *  - authToken: user's JWT for CDN authentication. Don't include `Bearer` header or similar.
     */
    suspend fun initialiseConversionSession(url: String, authToken: String): Result<TemplateSession> {
        val maybeCachedData = readCachedDensityData()

        val needToLoad = maybeCachedData==null || maybeCachedData.timestamp < Clock.System.now().minus(15.minutes)

        if(needToLoad) {
            val maybeTimestamp = maybeCachedData?.let { data -> {
                val dt = data.timestamp.toLocalDateTime(TimeZone.UTC)
                dt.format(httpDateFormat)
            } }()

            var headers = mapOf(
                "Authorization" to "Bearer $authToken",
                "Accept" to "application/json",
            )

            if(maybeTimestamp!=null) {
                headers = headers + mapOf(
                    "If-Modified-Since" to maybeTimestamp
                )
            }
            val response = makeHttpRequest("GET", url, null, headers)
            when(response.statusCode) {
                304 -> {
                    //The content was not modified, we should use exiting data
                    return newTemplateSession(maybeCachedData?.content) //NOTE - there should be no way `maybeCachedData` is null since then we wouldn't be making a conditional http request
                }
                200 -> {
                    //We got new data
                    val ts = response.responseHeaders?.get("last-modified").let {
                        if(it!=null)
                            LocalDateTime.parse(it, httpDateFormat).toInstant(TimeZone.UTC)
                        else null
                    }

                    if(response.body==null) {
                        loadErrorOccurred("No content returned from CDN, this indicates a code bug")
                        return newTemplateSession(maybeCachedData?.content)
                    }
                    if(ts==null) {
                        loadErrorOccurred("No last-modified returned from CDN, this indicates a code bug")
                        return newTemplateSession(maybeCachedData?.content)
                    }

                    val session = newTemplateSession(response.body)
                    if(session.isSuccess) {
                        //the data is valid. Attempt to write it to cache
                        writeCachedDensityData(CachedDensityData(
                            ts,
                            response.body
                        ))
                        //Now return the initialised session to the client
                        return session
                    } else {
                        //the data is not valid
                        loadErrorOccurred("The density data was not valid: ${session.exceptionOrNull()?.message}, falling back to cached result")
                        //don't write the data
                        return newTemplateSession(maybeCachedData?.content)
                    }
                }
                504 -> {
                    //The content was not available, most likely the device is offline
                    return newTemplateSession(maybeCachedData?.content)
                }
                else -> {
                    loadErrorOccurred("CDN responded with status code ${response.statusCode}, could not load data")
                    return newTemplateSession(maybeCachedData?.content)
                }
            }
        } else {
            //if(!needToLoad)
            return newTemplateSession(maybeCachedData.content)
        }
    }
}