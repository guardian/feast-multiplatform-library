package com.gu.recipe.loader

import com.gu.recipe.generated.IngredientItem
import com.gu.recipe.generated.IngredientsList
import com.gu.recipe.generated.RecipeV3
import com.gu.recipe.unit.MeasuringSystem
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class DataLoaderTest {

    // Uses a distinct prepared_at and a limited ingredient set (only 2 items)
    // so we can distinguish remote-data sessions from bundled fallback sessions
    private val validDensityJson = """
    {"prepared_at":"2026-01-01T00:00:00","key":["id","name","normalised_name","density"],"values":[[1,"Olive oil","olive oil",0.47],[2,"Salt (fine sea salt)","salt",0.36]]}
""".trimIndent()

    private val validTerminologyJson = """
    {"prepared_at":"2026-07-23T14:33:07.361Z","key":["id","ukTerm","usTerm","block","ukGuidance","usGuidance"],"values":[[1,"aubergine","eggplant",["eggplant"],"Testing uk guidance notes for aubergine","Testing us guidance notes for eggplant"],[2,"courgette","zucchini",["zucchini"],"",""]]}
""".trimIndent()


    private val invalidJson = "not valid json at all"

    private class FakeBridge(
        private val densityResult: DataLoadResult,
        private val terminologyResult: DataLoadResult
    ) : DataLoaderBridge {
        override suspend fun loadData(url: String, authToken: String?): DataLoadResult {
            return when (url) {
                "https://example.com/density" -> densityResult
                "https://example.com/terminology" -> terminologyResult
                else -> DataLoadResult.Failure("Unknown URL")
            }
        }
    }

    @Test
    fun `bridge returns Success with valid data and terminology JSON - session uses remote data`() = runTest {
        val errors = mutableListOf<String>()

        val bridge = FakeBridge(
            DataLoadResult.Success(validDensityJson),
            DataLoadResult.Success(validTerminologyJson)
        )
        val loader = DataLoader(bridge, onError = { errors.add(it) })

        val session = loader.initialiseConversionSession(
            "https://example.com/density",
            "https://example.com/terminology",
            "token123"
        )

        assertNotNull(session)
        // onError must NOT be called — proves remote data was used, not fallback
        assertTrue(errors.isEmpty(), "Expected no errors when remote data and terminology data are valid")
    }

    @Test
    fun `bridge returns Success with invalid JSON - falls back to bundled internal data`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(DataLoadResult.Success(invalidJson), DataLoadResult.Success(invalidJson))
        val loader = DataLoader(bridge, onError = { errors.add(it) })

        val session = loader.initialiseConversionSession("https://example.com/density", "https://example.com/terminology", "token123")

        assertNotNull(session)
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("RenderSession initialisation failed"))
    }

    @Test
    fun `bridge returns Failure - falls back to bundled internal data`() = runTest {
        val bridge = FakeBridge(DataLoadResult.Failure(), DataLoadResult.Failure())
        val loader = DataLoader(bridge)

        val session = loader.initialiseConversionSession("https://example.com/density", "https://example.com/terminology", "token123")

        assertNotNull(session)
    }

    @Test
    fun `bridge returns Failure with reason - falls back to bundled internal data`() = runTest {
        val bridge = FakeBridge(DataLoadResult.Failure("Network timeout"), DataLoadResult.Failure("Network timeout"))
        val loader = DataLoader(bridge)

        val session = loader.initialiseConversionSession("https://example.com/density", "https://example.com/terminology", "token123")

        assertNotNull(session)
    }

    @Test
    fun `bridge returns Success with empty string - falls back to bundled internal data`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(DataLoadResult.Success(""), DataLoadResult.Success(""))
        val loader = DataLoader(bridge, onError = { errors.add(it) })

        val session = loader.initialiseConversionSession("https://example.com/density", "https://example.com/terminology", "token123")

        assertNotNull(session)
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("RenderSession initialisation failed"))
    }

    @Test
    fun `onError callback is invoked when density JSON is invalid`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(DataLoadResult.Success(invalidJson), DataLoadResult.Success(validTerminologyJson))
        val loader = DataLoader(bridge, onError = { errors.add(it) })

        val session = loader.initialiseConversionSession("https://example.com/density", "https://example.com/terminology", "token123")

        assertNotNull(session)
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("RenderSession initialisation failed"))
    }

    @Test
    fun `onError callback is invoked when bridge returns Failure with reason`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(DataLoadResult.Failure("Connection refused"), DataLoadResult.Failure("Connection refused"))
        val loader = DataLoader(bridge, onError = { errors.add(it) })

        val session = loader.initialiseConversionSession("https://example.com/density", "https://example.com/terminology", "token123")

        assertEquals(1, errors.size)
        assertEquals("Connection refused", errors[0])
    }

    @Test
    fun `onError callback is not invoked when bridge returns Failure without reason`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(DataLoadResult.Failure(), DataLoadResult.Failure())
        val loader = DataLoader(bridge, onError = { errors.add(it) })

        val session = loader.initialiseConversionSession("https://example.com/density", "https://example.com/terminology", "token123")

        // No reason provided, so onError should not be called for the failure itself
        // (fallback session succeeds with bundled data, so no error there either)
        assertEquals(0, errors.size)
    }

    @Test
    fun `onError callback is not invoked on success`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(DataLoadResult.Success(validDensityJson), DataLoadResult.Success(validTerminologyJson))
        val loader = DataLoader(bridge, onError = { errors.add(it) })

        val session = loader.initialiseConversionSession("https://example.com/density", "https://example.com/terminology", "token123")

        assertTrue(errors.isEmpty())
    }

    private val aubergineRecipe = RecipeV3(
        id = "test-recipe",
        ingredients = listOf(
            IngredientsList(
                ingredientsList = listOf(IngredientItem(text = "1 aubergine"))
            )
        )
    )

    @Test
    fun `convertTerminologies false is respected on the fallback session`() = runTest {
        // Force the fallback path (invalid JSON) which uses bundled internal terminology data
        val bridge = FakeBridge(DataLoadResult.Success(invalidJson), DataLoadResult.Success(invalidJson))
        val loader = DataLoader(bridge)

        val session = loader.initialiseConversionSession(
            "https://example.com/density",
            "https://example.com/terminology",
            "token123",
            convertTerminologies = false
        )

        val rendered = session.renderRecipe(aubergineRecipe, 1f, MeasuringSystem.USCustomary)

        // Ingredient name must NOT be converted because terminologies are disabled
        assertEquals("1 aubergine", rendered.ingredients?.first()?.ingredientsList?.first()?.text)
    }

    @Test
    fun `convertTerminologies default true still converts on the fallback session`() = runTest {
        val bridge = FakeBridge(DataLoadResult.Success(invalidJson), DataLoadResult.Success(invalidJson))
        val loader = DataLoader(bridge)

        val session = loader.initialiseConversionSession(
            "https://example.com/density",
            "https://example.com/terminology",
            "token123"
        )

        val rendered = session.renderRecipe(aubergineRecipe, 1f, MeasuringSystem.USCustomary)

        // With terminologies enabled (default), the ingredient name is converted
        assertEquals("1 <u>eggplant</u>", rendered.ingredients?.first()?.ingredientsList?.first()?.text)
    }

}
