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
                        onError?.invoke("RenderSession initialisation failed: ${it.message}")
                        fallbackSession(convertTerminologies)
                    }
                }
                densityResult is DataLoadResult.Failure -> {
                    densityResult.reason?.let { onError?.invoke(it) }
                    fallbackSession(convertTerminologies)
                }
                terminologyResult is DataLoadResult.Failure -> {
                    terminologyResult.reason?.let { onError?.invoke(it) }
                    fallbackSession(convertTerminologies)
                }
                else -> fallbackSession(convertTerminologies)
            }
        } catch (e: Exception) {
            onError?.invoke("Bridge exception: ${e.message}")
            fallbackSession(convertTerminologies)
        }
    }

    private fun fallbackSession(convertTerminologies: Boolean): RenderSession {
        return newRenderSession(
            rawDensityData = null,
            rawTerminologyData = null,
            convertTerminologies = convertTerminologies
        ).getOrElse {
            onError?.invoke("Internal data also failed: ${it.message}")
            noCustomaryRenderSession()
        }
    }
}


