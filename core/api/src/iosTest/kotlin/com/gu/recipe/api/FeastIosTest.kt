package com.gu.recipe.api

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
        FeastIos.start(baseUrl = "https://recipes.code.dev-guardianapis.com")

        val repository = FeastIos.recipeRepository()

        assertNotNull(repository)
    }
}


