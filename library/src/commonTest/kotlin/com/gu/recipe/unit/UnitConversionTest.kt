package com.gu.recipe.unit

import com.gu.recipe.Amount
import kotlin.test.Test
import kotlin.test.assertEquals

class UnitConversionTest {

    @Test
    fun `returns amount unchanged when unit is null`() {
        val amount = Amount(min = 5f, max = 10f, unit = null)
        val result = UnitConversion.convertUnitSystem(amount, MeasuringSystem.Imperial)
        assertEquals(amount, result)
    }

    @Test
    fun `returns amount unchanged when measuring system is already Metric`() {
        val amount = Amount(min = 1f, max = null, unit = Units.KILOGRAM)
        val result = UnitConversion.convertUnitSystem(amount, MeasuringSystem.Metric)
        assertEquals(amount, result)
    }

    @Test
    fun `converts grams to ounces when target is Imperial`() {
        val amount = Amount(min = 100f, max = 200f, unit = Units.GRAM)
        val result = UnitConversion.convertUnitSystem(amount, MeasuringSystem.Imperial)

        val expectedMin = 100f / UnitConversion.ONCES_IN_GRAM
        val expectedMax = 200f / UnitConversion.ONCES_IN_GRAM
        assertEquals(expectedMin, result.min)
        assertEquals(expectedMax, result.max)
        assertEquals(Units.OUNCE, result.unit)
    }

    @Test
    fun `converts kilograms to pounds when target is Imperial`() {
        val amount = Amount(min = 2f, max = 3f, unit = Units.KILOGRAM)
        val result = UnitConversion.convertUnitSystem(amount, MeasuringSystem.Imperial)

        val expectedMin = 2f * UnitConversion.POUNDS_IN_KILOGRAM
        val expectedMax = 3f * UnitConversion.POUNDS_IN_KILOGRAM
        assertEquals(expectedMin, result.min)
        assertEquals(expectedMax, result.max)
        assertEquals(Units.POUND, result.unit)
    }
}

