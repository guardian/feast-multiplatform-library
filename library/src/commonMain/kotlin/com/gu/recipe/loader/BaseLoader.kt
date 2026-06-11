package com.gu.recipe.loader

abstract class BaseLoader<T>(
    private val bridge: BaseLoaderBridge,
    protected val onError: ((String) -> Unit)? = null
) {
    suspend fun initialiseSession(url: String, authToken: String? = null): T {
        return try {
            when (val result = bridge.loadData(url, authToken)) {
                is BaseLoadResult.Success -> {
                    loadTable(result.content).getOrElse {
                        onError?.invoke("Remote data failed validation: ${it.message}")
                        fallbackSession()
                    }
                }
                is BaseLoadResult.Failure -> {
                    result.reason?.let { onError?.invoke(it) }
                    fallbackSession()
                }
                else -> {
                    onError?.invoke("Unexpected result type")
                    fallbackSession()
                }
            }
        } catch (e: Exception) {
            onError?.invoke("Bridge exception: ${e.message}")
            fallbackSession()
        }
    }

    protected abstract fun loadTable(data: String?): Result<T>
    protected abstract fun fallbackSession(): T
}