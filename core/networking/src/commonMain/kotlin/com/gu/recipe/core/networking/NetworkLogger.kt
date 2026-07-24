package com.gu.recipe.core.networking

fun interface NetworkLogger {
    fun log(message: String)
}

object NoOpNetworkLogger : NetworkLogger {
    override fun log(message: String) = Unit
}

