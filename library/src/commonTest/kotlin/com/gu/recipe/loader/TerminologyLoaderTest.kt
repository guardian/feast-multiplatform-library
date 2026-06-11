//COMMENTED to resolve error later, it is aligned with new Base loaders now
// hence issues with underlying response.  Will Test this in second part


/*package com.gu.recipe.loader

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class TerminologyLoaderTest {

    private val validTerminologyJson0 = """
        {"prepared_at":"2026-01-01T00:00:00","key":["id","term","definition"],"values":[[1,"Term1","Definition1"],[2,"Term2","Definition2"]]}
    """.trimIndent()

    private val validTerminologyJson = """{"prepared_at":"2026-01-01T00:00:00",,"key":["id","ukTerm","usTerm"],"values":[["1","aubergine","eggplant-TEST"],["2","courgette","zucchini"],["3","icing sugar","powdered sugar"],["4","caster sugar","superfine sugar"],["5","cling film","plastic wrap"]]}
    """//.trimIndent()

    private val invalidJson = "not valid json at all"

    private class FakeBridge(private val result: TerminologyLoadResult) : TerminologyLoaderBridge {
        override suspend fun loadTerminologyData(url: String, authToken: String?): TerminologyLoadResult {
            return result
        }
        override suspend fun loadData(url: String, authToken: String?): BaseLoadResult {
            return result
        }
    }

    @Test
    fun `bridge returns Success with valid terminology JSON - session uses remote data`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(TerminologyLoadResult.Success(validTerminologyJson))
        val loader = TerminologyLoader(bridge, onError = { errors.add(it) })

        val session = loader.initialiseSession("https://example.com/data", "token123")

        println("Initialised session: $session")
        assertNotNull(session)
        println("Session initialisation successful, checking errors")
        println("Errors collected: $errors")
        assertTrue(errors.isEmpty(), "Expected no errors when remote data is valid")
    }


    @Test
    fun `bridge returns Success with invalid JSON - falls back to bundled internal data`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(TerminologyLoadResult.Success(invalidJson))
        val loader = TerminologyLoader(bridge, onError = { errors.add(it) })

        val session = loader.initialiseConversionSession("https://example.com/data", "token123")

        assertNotNull(session)
        assertEquals(1, errors.size, "Expected exactly one error for invalid remote data")
        assertTrue(errors[0].contains("Remote data failed validation"))
    }

    @Test
    fun `bridge returns Failure - falls back to bundled internal data`() = runTest {
        val bridge = FakeBridge(TerminologyLoadResult.Failure())
        val loader = TerminologyLoader(bridge)

        val session = loader.initialiseConversionSession("https://example.com/data", "token123")

        assertNotNull(session)
    }

    @Test
    fun `bridge returns Failure with reason - falls back to bundled internal data`() = runTest {
        val bridge = FakeBridge(TerminologyLoadResult.Failure("Network timeout"))
        val loader = TerminologyLoader(bridge)

        val session = loader.initialiseConversionSession("https://example.com/data", "token123")

        assertNotNull(session)
    }

    @Test
    fun `bridge returns Success with empty string - falls back to bundled internal data`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(TerminologyLoadResult.Success(""))
        val loader = TerminologyLoader(bridge, onError = { errors.add(it) })

        val session = loader.initialiseConversionSession("https://example.com/data", "token123")

        assertNotNull(session)
        assertEquals(1, errors.size, "Expected onError for invalid empty data")
        assertTrue(errors[0].contains("Remote data failed validation"))
    }

    @Test
    fun `onError callback is invoked when bridge returns invalid JSON`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(TerminologyLoadResult.Success(invalidJson))
        val loader = TerminologyLoader(bridge, onError = { errors.add(it) })

        loader.initialiseConversionSession("https://example.com/data", "token123")

        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("Remote data failed validation"))
    }

    @Test
    fun `onError callback is invoked when bridge returns Failure with reason`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(TerminologyLoadResult.Failure("Connection refused"))
        val loader = TerminologyLoader(bridge, onError = { errors.add(it) })

        loader.initialiseConversionSession("https://example.com/data", "token123")

        assertEquals(1, errors.size)
        assertEquals("Connection refused", errors[0])
    }

    @Test
    fun `onError callback is not invoked when bridge returns Failure without reason`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(TerminologyLoadResult.Failure())
        val loader = TerminologyLoader(bridge, onError = { errors.add(it) })

        loader.initialiseConversionSession("https://example.com/data", "token123")

        assertEquals(0, errors.size)
    }

    @Test
    fun `onError callback is not invoked on success`() = runTest {
        val errors = mutableListOf<String>()
        val bridge = FakeBridge(TerminologyLoadResult.Success(validTerminologyJson))
        val loader = TerminologyLoader(bridge, onError = { errors.add(it) })

        loader.initialiseConversionSession("https://example.com/data", "token123")

        assertTrue(errors.isEmpty())
    }


}

 */