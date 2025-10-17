package com.gu.recipe

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatNumberTest {
    @Test
    fun `integer value no decimals`() {
        assertEquals("5", formatNumber(5f, 2, false))
        assertEquals("0", formatNumber(0f, 2, false))
    }

    @Test
    fun `decimal value rounded correctly`() {
        assertEquals("2.5", formatNumber(2.5f, 1, false))
        assertEquals("2.56", formatNumber(2.555f, 2, false))
        assertEquals("2.56", formatNumber(2.555f, 2, true))
    }

    @Test
    fun `fractional value unicode fractions`() {
        assertEquals("½", formatNumber(0.5f, 2, true))
        assertEquals("¼", formatNumber(0.25f, 2, true))
        assertEquals("¾", formatNumber(0.75f, 2, true))
        assertEquals("1½", formatNumber(1.5f, 2, true))
        assertEquals("2¼", formatNumber(2.25f, 2, true))
    }

    @Test
    fun `fraction parameter false returns decimals`() {
        assertEquals("0.5", formatNumber(0.5f, 2, false))
        assertEquals("2.25", formatNumber(2.25f, 2, false))
    }

    @Test
    fun `decimals parameter effect`() {
        assertEquals("2", formatNumber(2f, 0, false))
        assertEquals("2.6", formatNumber(2.555f, 1, false))
        assertEquals("2.56", formatNumber(2.555f, 2, false))
    }

    @Test
    fun `edge cases`() {
        assertEquals("0", formatNumber(0f, 2, true))
        assertEquals("0", formatNumber(0f, 2, false))
    }
}

