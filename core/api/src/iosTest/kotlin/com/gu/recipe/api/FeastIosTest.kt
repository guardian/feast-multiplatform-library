package com.gu.recipe.api

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class FeastIosTest {
    @AfterTest
    fun tearDown() {
        FeastGraphQLiOS.stop()
    }

    @Test
    fun startExposesRecipeRepositoryForNativeConsumers() {
        FeastGraphQLiOS.start(baseUrl = "https://recipes.code.dev-guardianapis.com")

        val repository = FeastGraphQLiOS.recipeRepository()

        assertNotNull(repository)
    }
}


