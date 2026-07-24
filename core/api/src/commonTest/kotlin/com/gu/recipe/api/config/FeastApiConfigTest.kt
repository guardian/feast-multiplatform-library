package com.gu.recipe.api.config

import com.gu.recipe.api.model.FeastEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class FeastApiConfigTest {
    @Test
    fun `code environment resolves the code graphql url`() {
        val config = FeastApiConfig(
            environment = FeastEnvironment.CODE,
        )

        assertEquals(
            expected = "https://recipes.code.dev-guardianapis.com/graphql",
            actual = config.toGraphQlConfig().serverUrl,
        )
    }

    @Test
    fun `prod environment resolves the prod graphql url`() {
        val config = FeastApiConfig(
            environment = FeastEnvironment.PROD,
        )

        assertEquals(
            expected = "https://recipes.guardianapis.com/graphql",
            actual = config.toGraphQlConfig().serverUrl,
        )
    }
}

