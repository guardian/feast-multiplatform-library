package com.gu.recipe.density

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DensityTableTest {
    @Test
    fun `load in embedded density table`() {
        val densityTable = loadInternalDensityTable()
        assertTrue { densityTable.isSuccess }

        assertEquals(0.47f,densityTable.getOrNull()?.let { it.densityFor("Olive oil") })
        assertEquals(0.47f, densityTable.getOrNull()?.let { it.densityForNorm("olive oil") })
    }
}