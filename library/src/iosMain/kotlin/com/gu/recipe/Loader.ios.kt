package com.gu.recipe

actual suspend fun makeHttpRequest(
    method: String,
    url: String,
    body: String?,
    headers: Map<String, String>?
): com.gu.recipe.Loader.HttpResponse {
    TODO("Not yet implemented")
}

actual fun readCachedDensityData(): Loader.CachedDensityData? {
    TODO("Not yet implemented")
}

actual fun writeCachedDensityData(data: Loader.CachedDensityData): Result<Unit> {
    TODO("Not yet implemented")
}

actual fun loadErrorOccurred(message: String) {
}