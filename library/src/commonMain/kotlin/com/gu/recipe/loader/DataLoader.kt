package com.gu.recipe.loader

import com.gu.recipe.RenderSession
import com.gu.recipe.newRenderSession
import com.gu.recipe.noCustomaryRenderSession

class DataLoader(
    private val bridge: DataLoaderBridge,
    private val onError: ((String) -> Unit)? = null
) {
    /**
     * Fetches remote density data and terminology data and returns a ready-to-use RenderSession.
     * Always returns a usable session — never throws.
     * On failure, falls back to bundled internal data for both.
     */
    suspend fun initialiseConversionSession(
        densityUrl: String,
        terminologyUrl:String,
        authToken: String? = null,
        convertTerminologies: Boolean = true
    ): RenderSession {
        return try {
            val densityResult = bridge.loadData(densityUrl, authToken)
            val terminologyResult = bridge.loadData(terminologyUrl, authToken)

            when {
                densityResult is DataLoadResult.Success && terminologyResult is DataLoadResult.Success -> {
                    newRenderSession(
                        densityResult.content,
                        terminologyResult.content,
                        convertTerminologies = convertTerminologies,
                    ).getOrElse {
                        onError?.invoke("Remote data failed validation: ${it.message}")
                        fallbackSession()
                    }
                }
                densityResult is DataLoadResult.Failure -> {
                    densityResult.reason?.let { onError?.invoke(it) }
                    fallbackSession()
                }
                terminologyResult is DataLoadResult.Failure -> {
                    terminologyResult.reason?.let { onError?.invoke(it) }
                    fallbackSession()
                }
                else -> fallbackSession()
            }
        } catch (e: Exception) {
            onError?.invoke("Bridge exception: ${e.message}")
            fallbackSession()
        }
    }

    private fun fallbackSession(): RenderSession {
        return newRenderSession(null, null).getOrElse {
            onError?.invoke("Internal data also failed: ${it.message}")
            noCustomaryRenderSession()
        }
    }
}


