package com.gu.recipe.unit

import com.gu.recipe.Amount

object UnitConversion {

    val ONCES_IN_GRAM = 28.3495f
    val POUNDS_IN_KILOGRAM = 2.20462f

    fun convertUnitSystem(amount: Amount, target: MeasuringSystem): Amount {
        if (amount.unit == null) {
            return amount
        }
        if (amount.unit.measuringSystem == target) {
            return amount
        }
        return when (Pair(amount.unit, target)) {
            Pair(Units.GRAM, MeasuringSystem.Imperial) -> {
                val minOunces = amount.min / ONCES_IN_GRAM
                val maxOunces = amount.max?.let { it / ONCES_IN_GRAM }
                Amount(minOunces, maxOunces, Units.OUNCE)
            }

            Pair(Units.KILOGRAM, MeasuringSystem.Imperial) -> {
                val minPounds = amount.min * POUNDS_IN_KILOGRAM
                val maxPounds = amount.max?.let { it * POUNDS_IN_KILOGRAM }
                Amount(minPounds, maxPounds, Units.POUND)
            }

            else -> amount
        }
    }
}