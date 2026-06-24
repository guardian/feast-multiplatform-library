package com.gu.recipe.loader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class AndroidDensityLoaderBridge(private val cacheDir: File) : DataLoaderBridge {

    private val cachePath = File(cacheDir, "recipe_data/density_cache.json").toOkioPath()

    override suspend fun loadData(url: String, authToken: String?): DataLoadResult {
        return withContext(Dispatchers.IO) {
            val cached = readCache()
            try {
                if (cached != null && isCacheFresh()) {
                    return@withContext DataLoadResult.Success(cached.content)
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
    }

    private fun readCache(): DataCacheEntry? {
        return try {
            if (!FileSystem.SYSTEM.exists(cachePath)) return null
            val raw = FileSystem.SYSTEM.read(cachePath) { readUtf8() }
            Json.decodeFromString<DataCacheEntry>(raw)
        } catch (_: Exception) {
            null
        }
    }

    private fun isCacheFresh(): Boolean {
        return try {
            val lastModified = FileSystem.SYSTEM.metadata(cachePath).lastModifiedAtMillis
                ?: return false
            System.currentTimeMillis() - lastModified < CACHE_FRESHNESS_MS
        } catch (_: Exception) {
            false
        }
    }

    private fun writeCache(entry: DataCacheEntry) {
        try {
            val parent = cachePath.parent ?: return
            FileSystem.SYSTEM.createDirectories(parent)
            FileSystem.SYSTEM.write(cachePath) {
                writeUtf8(Json.encodeToString(entry))
            }
        } catch (_: Exception) {
            // Write failure — do not crash
        }
    }

    private fun performRequest(
        url: String,
        authToken: String?,
        cached: DataCacheEntry?
    ): DataLoadResult {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.requestMethod = "GET"
            if (authToken != null) {
                connection.setRequestProperty("Authorization", "Bearer $authToken")
            }
            connection.setRequestProperty("Accept", "application/json")
            if (cached != null) {
                connection.setRequestProperty("If-Modified-Since", cached.lastModified)
            }
            connection.connect()

            val responseCode = connection.responseCode

            return when {
                responseCode == HttpURLConnection.HTTP_OK -> {
                    val body = connection.inputStream.bufferedReader().use { it.readText() }
                    val lastModified = connection.getHeaderField("Last-Modified")

                    if (body.isNotEmpty() && lastModified != null) {
                        writeCache(DataCacheEntry(lastModified, body))
                        DataLoadResult.Success(body)
                    } else if (cached != null) {
                        DataLoadResult.Success(cached.content)
                    } else {
                        DataLoadResult.Failure("HTTP 200 but missing body or Last-Modified header")
                    }
                }
                responseCode == HttpURLConnection.HTTP_NOT_MODIFIED -> {
                    if (cached != null) {
                        // Touch the cache so freshness resets
                        writeCache(cached)
                        DataLoadResult.Success(cached.content)
                    } else {
                        DataLoadResult.Failure("HTTP 304 but no cached data available")
                    }
                }
                else -> {
                    if (cached != null) {
                        DataLoadResult.Success(cached.content)
                    } else {
                        DataLoadResult.Failure("HTTP $responseCode")
                    }
                }
            }
        } catch (e: Exception) {
            return if (cached != null) {
                DataLoadResult.Success(cached.content)
            } else {
                DataLoadResult.Failure("Network error: ${e.message}")
            }
        } finally {
            connection.disconnect()
        }
    }
}
