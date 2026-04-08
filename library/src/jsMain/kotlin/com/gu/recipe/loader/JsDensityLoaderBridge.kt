package com.gu.recipe.loader

import kotlinx.coroutines.await
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Headers
import kotlin.js.json

class JsDensityLoaderBridge : DensityLoaderBridge {

    override suspend fun loadDensityData(url: String, authToken: String): DensityLoadResult {
        return try {
            val headers = Headers()
            headers.append("Authorization", "Bearer $authToken")
            headers.append("Accept", "application/json")

            val init = RequestInit(
                method = "GET",
                headers = headers
            )

            val response = kotlinx.browser.window.fetch(url, init).await()

            if (response.ok) {
                val body = response.text().await()
                if (body.isNotEmpty()) {
                    DensityLoadResult.Success(body)
                } else {
                    DensityLoadResult.Failure("HTTP ${response.status} but empty body")
                }
            } else {
                DensityLoadResult.Failure("HTTP ${response.status}")
            }
        } catch (e: Throwable) {
            DensityLoadResult.Failure("Fetch failed: ${e.message}")
        }
    }
}

