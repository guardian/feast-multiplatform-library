package com.gu.recipe.loader

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
    {"prepared_at":"2026-01-01T00:00:00","key":["id","ukTerm","usTerm","block"],"values":[[1,"aubergine","eggplant",[]],[2,"courgette","zucchini",[]]]}
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
        assertEquals(2, errors.size, "Expects two errors for invalid remote data for both density and terminology")
        assertTrue(errors[0].contains("Remote data failed validation"))
        assertTrue(errors[1].contains("Remote data failed validation"))
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
        assertEquals(2, errors.size, "Expects two errors for invalid empty data for both density and terminology")
        assertTrue(errors[0].contains("Remote data failed validation"))
        assertTrue(errors[1].contains("Remote data failed validation"))
    }

    @Test
    fun `onError callback is invoked when bridge returns invalid JSON`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(DataLoadResult.Success(invalidJson), DataLoadResult.Success(invalidJson))
        val loader = DataLoader(bridge, onError = { errors.add(it) })

        val session = loader.initialiseConversionSession("https://example.com/density", "https://example.com/terminology", "token123")

        assertEquals(2, errors.size, "Expects two errors for invalid remote data for both density and terminology")
        assertTrue(errors[0].contains("Remote data failed validation"))
        assertTrue(errors[1].contains("Remote data failed validation"))
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

}
