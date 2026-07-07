package com.gu.recipe.loader

import kotlinx.coroutines.await
import kotlin.js.Promise

class JsDataLoaderBridge : DataLoaderBridge {

    override suspend fun loadData(url: String, authToken: String?): DataLoadResult {
        return try {
            val headers = js("({})")
            if (authToken != null) {
                headers["Authorization"] = "Bearer $authToken"
            }
            headers["Accept"] = "application/json"

            val init = js("({})")
            init.method = "GET"
            init.headers = headers

            val fetchFn: dynamic = js("globalThis.fetch")
            val response: dynamic = (fetchFn(url, init) as Promise<dynamic>).await()
            val status = (response.status as Number).toInt()
            val ok = response.ok as Boolean

            if (ok) {
                val body = (response.text() as Promise<String>).await()
                if (body.isNotEmpty()) {
                    DataLoadResult.Success(body)
                } else {
                    DataLoadResult.Failure("HTTP $status but empty body")
                }
            } else {
                DataLoadResult.Failure("HTTP $status")
            }
        } catch (e: Throwable) {
            DataLoadResult.Failure("Fetch failed: ${e.message}")
        }
    }
}
