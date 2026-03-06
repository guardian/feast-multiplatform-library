package com.gu.recipe

import kotlinx.browser.window
import org.w3c.fetch.RequestInit
import kotlinx.coroutines.await
import org.w3c.fetch.Headers
import kotlin.js.Promise

private fun Headers.toMap(): Map<String, String> =
    (js("Array.from(this.entries())") as Array<Array<String>>)
        .associate { (key, value) -> key to value }

fun Map<String, String>.toJsHeaders(): dynamic {
    val obj = js("({})")
    forEach { (key, value) -> obj[key] = value }
    return obj
}

private fun fetchJs(url: String, method: String, headers: dynamic, body: dynamic): dynamic {
    return js("fetch(url, { method: method, headers: headers, body: body })")
}

actual suspend fun makeHttpRequest(method: String, url: String, body: String?, headers:Map<String, String>?): Loader.HttpResponse {
    try {
        val promise = fetchJs(url, method, headers?.toJsHeaders(), body).then { response ->
            Loader.HttpResponse(
                response.status.toInt(),
                response.headers.toMap(),
                response.body.decodeToString()
            )
        }

        return promise.await()
    } catch (e: Exception) {
        println("Error loading $url: ${e.message}")
        return Loader.HttpResponse(
            504,
            responseHeaders = null,
            body = e.message
        )
    }
}

actual fun readCachedDensityData(): Loader.CachedDensityData? {
    //We don't implement caching on JS, assume always online
    return null
}

actual fun writeCachedDensityData(data: Loader.CachedDensityData): Result<Unit> {
    //We don't implement caching on JS, assume always online
    return Result.success(Unit )
}

actual fun loadErrorOccurred(message: String) {
    println("ERROR: $message")
}
