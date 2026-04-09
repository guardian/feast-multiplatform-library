package com.gu.recipe.loader

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class DensityLoaderTest {

    // Uses a distinct prepared_at and a limited ingredient set (only 2 items)
    // so we can distinguish remote-data sessions from bundled fallback sessions.
    private val validDensityJson = """
        {"prepared_at":"2026-01-01T00:00:00","key":["id","name","normalised_name","density"],"values":[[1,"Olive oil","olive oil",0.47],[2,"Salt (fine sea salt)","salt",0.36]]}
    """.trimIndent()

    private val invalidJson = "not valid json at all"

    private class FakeBridge(private val result: DensityLoadResult) : DensityLoaderBridge {
        override suspend fun loadDensityData(url: String, authToken: String?): DensityLoadResult {
            return result
        }
    }

    @Test
    fun `bridge returns Success with valid density JSON - session uses remote data`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(DensityLoadResult.Success(validDensityJson))
        val loader = DensityLoader(bridge, onError = { errors.add(it) })

        val session = loader.initialiseConversionSession("https://example.com/data", "token123")

        assertNotNull(session)
        // onError must NOT be called — proves remote data was used, not fallback
        assertTrue(errors.isEmpty(), "Expected no errors when remote data is valid")
    }

    @Test
    fun `bridge returns Success with invalid JSON - falls back to bundled internal data`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(DensityLoadResult.Success(invalidJson))
        val loader = DensityLoader(bridge, onError = { errors.add(it) })

        val session = loader.initialiseConversionSession("https://example.com/data", "token123")

        assertNotNull(session)
        assertEquals(1, errors.size, "Expected exactly one error for invalid remote data")
        assertTrue(errors[0].contains("Remote data failed validation"))
    }

    @Test
    fun `bridge returns Failure - falls back to bundled internal data`() = runTest {
        val bridge = FakeBridge(DensityLoadResult.Failure())
        val loader = DensityLoader(bridge)

        val session = loader.initialiseConversionSession("https://example.com/data", "token123")

        assertNotNull(session)
    }

    @Test
    fun `bridge returns Failure with reason - falls back to bundled internal data`() = runTest {
        val bridge = FakeBridge(DensityLoadResult.Failure("Network timeout"))
        val loader = DensityLoader(bridge)

        val session = loader.initialiseConversionSession("https://example.com/data", "token123")

        assertNotNull(session)
    }

    @Test
    fun `bridge returns Success with empty string - falls back to bundled internal data`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(DensityLoadResult.Success(""))
        val loader = DensityLoader(bridge, onError = { errors.add(it) })

        val session = loader.initialiseConversionSession("https://example.com/data", "token123")

        assertNotNull(session)
        assertEquals(1, errors.size, "Expected onError for invalid empty data")
        assertTrue(errors[0].contains("Remote data failed validation"))
    }

    @Test
    fun `onError callback is invoked when bridge returns invalid JSON`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(DensityLoadResult.Success(invalidJson))
        val loader = DensityLoader(bridge, onError = { errors.add(it) })

        loader.initialiseConversionSession("https://example.com/data", "token123")

        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("Remote data failed validation"))
    }

    @Test
    fun `onError callback is invoked when bridge returns Failure with reason`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(DensityLoadResult.Failure("Connection refused"))
        val loader = DensityLoader(bridge, onError = { errors.add(it) })

        loader.initialiseConversionSession("https://example.com/data", "token123")

        assertEquals(1, errors.size)
        assertEquals("Connection refused", errors[0])
    }

    @Test
    fun `onError callback is not invoked when bridge returns Failure without reason`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(DensityLoadResult.Failure())
        val loader = DensityLoader(bridge, onError = { errors.add(it) })

        loader.initialiseConversionSession("https://example.com/data", "token123")

        // No reason provided, so onError should not be called for the failure itself
        // (fallback session succeeds with bundled data, so no error there either)
        assertEquals(0, errors.size)
    }

    @Test
    fun `onError callback is not invoked on success`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(DensityLoadResult.Success(validDensityJson))
        val loader = DensityLoader(bridge, onError = { errors.add(it) })

        loader.initialiseConversionSession("https://example.com/data", "token123")

        assertTrue(errors.isEmpty())
    }
}
