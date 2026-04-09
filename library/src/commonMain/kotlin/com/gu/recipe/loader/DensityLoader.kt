package com.gu.recipe.loader

import com.gu.recipe.TemplateSession
import com.gu.recipe.newTemplateSession
import com.gu.recipe.noCustomaryTemplateSession

class DensityLoader(
    private val bridge: DensityLoaderBridge,
    private val onError: ((String) -> Unit)? = null
) {
    /**
     * Fetches remote density data and returns a ready-to-use TemplateSession.
     * Always returns a usable session — never throws.
     * On failure, falls back to bundled internal density data.
     */
    suspend fun initialiseConversionSession(
        url: String,
        authToken: String
    ): TemplateSession {
        return try {
            when (val result = bridge.loadDensityData(url, authToken)) {
                is DensityLoadResult.Success -> {
                    newTemplateSession(result.content).getOrElse {
                        onError?.invoke("Remote data failed validation: ${it.message}")
                        fallbackSession()
                    }
                }
                is DensityLoadResult.Failure -> {
                    result.reason?.let { onError?.invoke(it) }
                    fallbackSession()
                }
            }
        } catch (e: Exception) {
            onError?.invoke("Bridge exception: ${e.message}")
            fallbackSession()
        }
    }

    private fun fallbackSession(): TemplateSession {
        return newTemplateSession(null).getOrElse {
            onError?.invoke("Internal data also failed: ${it.message}")
            noCustomaryTemplateSession()
        }
    }
}

