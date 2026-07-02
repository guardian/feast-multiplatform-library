package com.gu.recipe.api

import com.gu.recipe.core.networking.FeastNetworkApiEndpoint
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class FeastIosTest {
    @AfterTest
    fun tearDown() {
        FeastIos.stop()
    }

    @Test
    fun startExposesRecipeRepositoryForNativeConsumers() {
        FeastIos.start(endpoint = FeastNetworkApiEndpoint.CODE)

        val repository = FeastIos.recipeRepository()

        assertNotNull(repository)
    }
}


