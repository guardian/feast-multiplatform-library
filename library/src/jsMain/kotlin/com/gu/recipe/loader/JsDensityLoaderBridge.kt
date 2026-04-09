package com.gu.recipe.loader

import kotlinx.coroutines.await
import kotlin.js.Promise

class JsDensityLoaderBridge : DensityLoaderBridge {

    override suspend fun loadDensityData(url: String, authToken: String): DensityLoadResult {
        return try {
            val headers = js("({})")
            headers["Authorization"] = "Bearer $authToken"
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
                    DensityLoadResult.Success(body)
                } else {
                    DensityLoadResult.Failure("HTTP $status but empty body")
                }
            } else {
                DensityLoadResult.Failure("HTTP $status")
            }
        } catch (e: Throwable) {
            DensityLoadResult.Failure("Fetch failed: ${e.message}")
        }
    }
}
