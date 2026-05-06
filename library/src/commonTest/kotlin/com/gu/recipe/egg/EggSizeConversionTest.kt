package com.gu.recipe.egg

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EggSizeConversionTest {

    @Test
    fun `UK small stays as US small`() {
        assertEquals("small", convertEggSizeLabel("small", EggRegion.US))
    }

    @Test
    fun `UK medium converts to US large`() {
        assertEquals("large", convertEggSizeLabel("medium", EggRegion.US))
    }

    @Test
    fun `UK large converts to US extra large`() {
        assertEquals("extra large", convertEggSizeLabel("large", EggRegion.US))
    }

    @Test
    fun `UK extra large converts to US jumbo`() {
        assertEquals("jumbo", convertEggSizeLabel("extra large", EggRegion.US))
        assertEquals("jumbo", convertEggSizeLabel("xl", EggRegion.US))
        assertEquals("jumbo", convertEggSizeLabel("extra-large", EggRegion.US))
    }

    @Test
    fun `unknown label returns null`() {
        assertNull(convertEggSizeLabel("tiny", EggRegion.US))
    }

    @Test
    fun `case insensitive matching`() {
        assertEquals("extra large", convertEggSizeLabel("Large", EggRegion.US))
        assertEquals("large", convertEggSizeLabel("MEDIUM", EggRegion.US))
    }

    @Test
    fun `replaces size before egg`() {
        assertEquals("2 extra large eggs", convertEggSizesInText("2 large eggs", EggRegion.US))
        assertEquals("4 large eggs, lightly beaten", convertEggSizesInText("4 medium eggs, lightly beaten", EggRegion.US))
        assertEquals("2 small egg whites", convertEggSizesInText("2 small egg whites", EggRegion.US))
        assertEquals("1 jumbo egg", convertEggSizesInText("1 extra large egg", EggRegion.US))
    }

    @Test
    fun `replaces size after eggs as suffix`() {
        assertEquals("2 eggs, extra large", convertEggSizesInText("2 eggs, large", EggRegion.US))
        assertEquals("4 eggs, large", convertEggSizesInText("4 eggs, medium", EggRegion.US))
    }

    @Test
    fun `leaves text without size word unchanged`() {
        assertEquals("1 egg", convertEggSizesInText("1 egg", EggRegion.US))
    }

    @Test
    fun `does not modify non-egg sizes`() {
        assertEquals("1 large potato", convertEggSizesInText("1 large potato", EggRegion.US))
        assertEquals("2 medium onions", convertEggSizesInText("2 medium onions", EggRegion.US))
    }
}
