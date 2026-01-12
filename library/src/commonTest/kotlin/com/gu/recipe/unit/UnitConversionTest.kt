package com.gu.recipe.unit

import com.gu.recipe.Amount
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

        val expectedMin = 3.527f
        val expectedMax = 7.055f
        assertEquals(expectedMin, result.min, absoluteTolerance = 0.001f)
        assertEquals(expectedMax, result.max!!, absoluteTolerance = 0.001f)
        assertEquals(Units.OUNCE, result.unit)
    }

    @Test
    fun `converts kilograms to pounds when target is Imperial`() {
        val amount = Amount(min = 2f, max = 3f, unit = Units.KILOGRAM)
        val result = UnitConversion.convertUnitSystem(amount, MeasuringSystem.Imperial)

        val expectedMin = 4.410f
        val expectedMax = 6.615f
        assertEquals(expectedMin, result.min, absoluteTolerance = 0.001f)
        assertEquals(expectedMax, result.max!!, absoluteTolerance = 0.001f)
        assertEquals(Units.POUND, result.unit)
    }

    @Test
    fun `converts ml to cups when target is USCustomary`() {
        val amount = Amount(min = 100f, unit = Units.MILLILITRE, usCust = true)
        val result = UnitConversion.convertUnitSystem(amount, MeasuringSystem.USCustomary)

        assertEquals(0.423f, result.min, absoluteTolerance = 0.001f)
        assertEquals(Units.CUP, result.unit)
    }

    @Test
    fun `converts centimetres to inches when target is Imperial`() {
        val amount = Amount(min = 10f, max = 20f, unit = Units.CENTIMETRE)
        val result = UnitConversion.convertUnitSystem(amount, MeasuringSystem.Imperial)

        val expectedMin = 3.94f
        val expectedMax = 7.87f
        assertEquals(expectedMin, result.min, absoluteTolerance = 0.01f)
        assertEquals(expectedMax, result.max!!, absoluteTolerance = 0.01f)
        assertEquals(Units.INCH, result.unit)
    }

    @Test
    fun `converts millimetres to inches when target is Imperial`() {
        val amount = Amount(min = 10f, max = 20f, unit = Units.MILLIMETRE)
        val result = UnitConversion.convertUnitSystem(amount, MeasuringSystem.Imperial)

        val expectedMin = 0.39f
        val expectedMax = 0.79f
        assertEquals(expectedMin, result.min, absoluteTolerance = 0.01f)
        assertEquals(expectedMax, result.max!!, absoluteTolerance = 0.01f)
        assertEquals(Units.INCH, result.unit)
    }

    @Test
    fun `converts millilitres to cups when target is USCustomary`() {
        val amount = Amount(min = 236.56f, max = 473.12f, unit = Units.MILLILITRE, usCust = true)
        val result = UnitConversion.convertUnitSystem(amount, MeasuringSystem.USCustomary)

        assertEquals(1f, result.min, absoluteTolerance = 0.001f)
        assertEquals(2f, result.max!!, absoluteTolerance = 0.001f)
        assertEquals(Units.CUP, result.unit)
    }

    @Test
    fun `converts litres to cups when target is USCustomary`() {
        val amount = Amount(min = 1f, max = 2f, unit = Units.LITRE, usCust = true)
        val result = UnitConversion.convertUnitSystem(amount, MeasuringSystem.USCustomary)

        val expectedMin = 4.227f
        val expectedMax = 8.454f
        assertEquals(expectedMin, result.min, absoluteTolerance = 0.001f)
        assertEquals(expectedMax, result.max!!, absoluteTolerance = 0.001f)
        assertEquals(Units.CUP, result.unit)
    }

    @Test
    fun `converts litres to quarts when target is USCustomary`() {
        val amount = Amount(min = 1f, unit = Units.LITRE)
        val result = UnitConversion.convertUnitSystem(amount, MeasuringSystem.USCustomary)

        val expectedMin = 1.0567f
        assertEquals(expectedMin, result.min, absoluteTolerance = 0.001f)
        assertNull(result.max)
        assertEquals(Units.LITRE, result.unit)
    }

    @Test
    fun `falls back to imperial conversion for non-volume units in USCustomary`() {
        val amount = Amount(min = 100f, max = 200f, unit = Units.GRAM, usCust = true)
        val result = UnitConversion.convertUnitSystem(amount, MeasuringSystem.USCustomary)

        val expectedMin = 3.527f
        val expectedMax = 7.055f
        assertEquals(expectedMin, result.min, absoluteTolerance = 0.001f)
        assertEquals(expectedMax, result.max!!, absoluteTolerance = 0.001f)
        assertEquals(Units.OUNCE, result.unit)
    }

    @Test
    fun `returns amount unchanged when unit is already Imperial`() {
        val amount = Amount(min = 5f, max = 10f, unit = Units.OUNCE)
        val result = UnitConversion.convertUnitSystem(amount, MeasuringSystem.Imperial)
        assertEquals(amount, result)
    }

    @Test
    fun `handles null max value correctly in conversions`() {
        val amount = Amount(min = 100f, max = null, unit = Units.GRAM)
        val result = UnitConversion.convertUnitSystem(amount, MeasuringSystem.Imperial)

        val expectedMin = 3.527f
        assertEquals(expectedMin, result.min, absoluteTolerance = 0.001f)
        assertEquals(null, result.max)
        assertEquals(Units.OUNCE, result.unit)
    }
}

