package com.gu.recipe.api

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class FeastGraphQLiOSTest {
    @AfterTest
    fun tearDown() {
        FeastGraphQLiOS.stop()
    }

    @Test
    fun startExposesGraphQLRepositoryForNativeConsumers() {
        FeastGraphQLiOS.start(baseUrl = "https://recipes.code.dev-guardianapis.com")

        val repository = FeastGraphQLiOS.graphQLRepository()

        assertNotNull(repository)
    }
}


