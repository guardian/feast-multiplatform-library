package com.gu.recipe.loader

import kotlin.js.Promise
import kotlinx.coroutines.await

class JsDensityLoaderBridge : DensityLoaderBridge {

    override suspend fun loadDensityData(url: String, authToken: String): DensityLoadResult {
        return try {
            val init = js("({})")
            init.method = "GET"
            val headers = js("({})")
            headers["Authorization"] = "Bearer $authToken"
            headers["Accept"] = "application/json"
            init.headers = headers

            val response: dynamic = (js("fetch(url, init)") as Promise<dynamic>).await()
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

