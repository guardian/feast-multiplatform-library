package com.gu.recipe.loader

import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.*
import kotlin.coroutines.resume

class IosDataLoaderBridge(private val cachesDirectory: String) : DataLoaderBridge {

    private fun cachePath(url: String) = "$cachesDirectory/recipe_data/${url.hashCode()}_cache.json".toPath()

    override suspend fun loadData(url: String, authToken: String?): DataLoadResult {
        val cached = readCache(url)
        return try {
            if (cached != null && isCacheFresh(url)) {
                return DataLoadResult.Success(cached.content)
            }

            performRequest(url, authToken, cached)
        } catch (e: Exception) {
            if (cached != null) {
                DataLoadResult.Success(cached.content)
            } else {
                DataLoadResult.Failure("Exception: ${e.message}")
            }
        }
    }

    private fun readCache(url: String): DataCacheEntry? {
        return try {
            if (!FileSystem.SYSTEM.exists(cachePath(url))) return null
            val raw = FileSystem.SYSTEM.read(cachePath(url)) { readUtf8() }
            Json.decodeFromString<DataCacheEntry>(raw)
        } catch (_: Exception) {
            null
        }
    }

    private fun isCacheFresh(url: String): Boolean {
        return try {
            val lastModified = FileSystem.SYSTEM.metadata(cachePath(url)).lastModifiedAtMillis
                ?: return false
            val now = (NSDate().timeIntervalSince1970 * 1000).toLong()
            now - lastModified < CACHE_FRESHNESS_MS
        } catch (_: Exception) {
            false
        }
    }

    private fun writeCache(url: String, entry: DataCacheEntry) {
        try {
            val parent = cachePath(url).parent ?: return
            FileSystem.SYSTEM.createDirectories(parent)
            FileSystem.SYSTEM.write(cachePath(url)) {
                writeUtf8(Json.encodeToString(entry))
            }
        } catch (_: Exception) {
            // Write failure — do not crash
        }
    }

    private suspend fun performRequest(
        url: String,
        authToken: String?,
        cached: DataCacheEntry?
    ): DataLoadResult {
        return when (val result = httpGet(url, authToken, cached?.lastModified)) {
            is HttpResult.Success -> {
                when {
                    result.statusCode == 200 && result.body.isNotEmpty() && result.lastModified != null -> {
                        writeCache(url, DataCacheEntry(result.lastModified, result.body))
                        DataLoadResult.Success(result.body)
                    }
                    result.statusCode == 200 -> {
                        if (cached != null) DataLoadResult.Success(cached.content)
                        else DataLoadResult.Failure("HTTP 200 but missing body or Last-Modified header")
                    }
                    result.statusCode == 304 -> {
                        if (cached != null) {
                            writeCache(url, cached)
                            DataLoadResult.Success(cached.content)
                        } else {
                            DataLoadResult.Failure("HTTP 304 but no cached data available")
                        }
                    }
                    else -> {
                        if (cached != null) DataLoadResult.Success(cached.content)
                        else DataLoadResult.Failure("HTTP ${result.statusCode}")
                    }
                }
            }
            is HttpResult.Error -> {
                if (cached != null) DataLoadResult.Success(cached.content)
                else DataLoadResult.Failure(result.reason)
            }
        }
    }

    private sealed interface HttpResult {
        data class Success(val statusCode: Int, val body: String, val lastModified: String?) : HttpResult
        data class Error(val reason: String) : HttpResult
    }

    private suspend fun httpGet(
        url: String,
        authToken: String?,
        ifModifiedSince: String?
    ): HttpResult = suspendCancellableCoroutine { cont ->
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) {
            cont.resume(HttpResult.Error("Invalid URL: $url"))
            return@suspendCancellableCoroutine
        }

        val request = NSMutableURLRequest.requestWithURL(nsUrl).apply {
            setHTTPMethod("GET")
            if (authToken != null) {
                setValue("Bearer $authToken", forHTTPHeaderField = "Authorization")
            }
            setValue("application/json", forHTTPHeaderField = "Accept")
            if (ifModifiedSince != null) {
                setValue(ifModifiedSince, forHTTPHeaderField = "If-Modified-Since")
            }
        }

        val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
            if (!cont.isActive) return@dataTaskWithRequest

            if (error != null) {
                cont.resume(HttpResult.Error("Network error: ${error.localizedDescription}"))
                return@dataTaskWithRequest
            }

            val httpResponse = response as? NSHTTPURLResponse
            if (httpResponse == null) {
                cont.resume(HttpResult.Error("No HTTP response"))
                return@dataTaskWithRequest
            }

            val statusCode = httpResponse.statusCode.toInt()
            val body = data?.toKString() ?: ""
            val lastModified = httpResponse.allHeaderFields["Last-Modified"] as? String

            cont.resume(HttpResult.Success(statusCode, body, lastModified))
        }

        cont.invokeOnCancellation { task.cancel() }
        task.resume()
    }

    @OptIn(BetaInteropApi::class)
    private fun NSData.toKString(): String {
        return NSString.create(data = this, encoding = NSUTF8StringEncoding)?.toString() ?: ""
    }
}
