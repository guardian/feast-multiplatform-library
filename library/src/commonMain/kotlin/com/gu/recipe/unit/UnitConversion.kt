package com.gu.recipe.unit

import com.gu.recipe.Amount

object UnitConversion {

    val ONCES_IN_GRAM = 28.3495f
    val POUNDS_IN_KILOGRAM = 2.20462f
    val ML_IN_CUP = 236.56f

    fun convertUnitSystem(amount: Amount, target: MeasuringSystem): Amount {
        if (amount.unit == null
            || amount.unit.measuringSystem == target
            || target == MeasuringSystem.Metric) {
            return amount
        }

        return if (amount.usCust == true) {
            when (amount.unit) {
                Units.MILLILITER -> {
                    val cups = amount.min / ML_IN_CUP
                    val maxCups = amount.max?.let { it / ML_IN_CUP }
                    Amount(cups, maxCups, Units.CUP)
                }

                else -> amount
            }
        } else {
            when (amount.unit) {
                Units.GRAM -> {
                    val minOunces = amount.min / ONCES_IN_GRAM
                    val maxOunces = amount.max?.let { it / ONCES_IN_GRAM }
                    Amount(minOunces, maxOunces, Units.OUNCE)
                }

                Units.KILOGRAM -> {
                    val minPounds = amount.min * POUNDS_IN_KILOGRAM
                    val maxPounds = amount.max?.let { it * POUNDS_IN_KILOGRAM }
                    Amount(minPounds, maxPounds, Units.POUND)
                }

                else -> amount
            }
        }

    }
}