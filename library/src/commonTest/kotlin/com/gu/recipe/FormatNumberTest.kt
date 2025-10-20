package com.gu.recipe

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatNumberTest {
    @Test
    fun `integer value no decimals`() {
        assertEquals("5", formatAmount(5f, 2, false))
        assertEquals("0", formatAmount(0f, 2, false))
    }

    @Test
    fun `decimal value rounded correctly`() {
        assertEquals("2.5", formatAmount(2.5f, 1, false))
        assertEquals("2.56", formatAmount(2.555f, 2, false))
        assertEquals("2.56", formatAmount(2.555f, 2, true))
    }

    @Test
    fun `fractional value unicode fractions`() {
        assertEquals("½", formatAmount(0.5f, 2, true))
        assertEquals("¼", formatAmount(0.25f, 2, true))
        assertEquals("¾", formatAmount(0.75f, 2, true))
        assertEquals("1½", formatAmount(1.5f, 2, true))
        assertEquals("2¼", formatAmount(2.25f, 2, true))
    }

    @Test
    fun `fraction parameter false returns decimals`() {
        assertEquals("0.5", formatAmount(0.5f, 2, false))
        assertEquals("2.25", formatAmount(2.25f, 2, false))
    }

    @Test
    fun `decimals parameter effect`() {
        assertEquals("2", formatAmount(2f, 0, false))
        assertEquals("2.6", formatAmount(2.555f, 1, false))
        assertEquals("2.56", formatAmount(2.555f, 2, false))
    }

    @Test
    fun `edge cases`() {
        assertEquals("0", formatAmount(0f, 2, true))
        assertEquals("0", formatAmount(0f, 2, false))
    }
}

