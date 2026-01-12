package com.gu.recipe.unit

import com.gu.recipe.Amount

object UnitConversion {
    val GRAMS_PER_ONCE = 28.3495f
    val KILOGRAMS_PER_POUND = 0.4535f
    val ML_PER_CUP = 236.56f
    val CM_PER_INCH = 2.54f
    val LITRE_PER_QUART = 1.0567f

    fun toUSCustomary(amount: Amount): Amount {
        return when (amount.unit) {
            Units.MILLILITRE -> {
                val cups = amount.min / ML_PER_CUP
                val maxCups = amount.max?.let { it / ML_PER_CUP }
                Amount(cups, maxCups, Units.CUP)
            }

            Units.LITRE -> {
                val cups = (amount.min * 1000f) / ML_PER_CUP
                val maxCups = amount.max?.let { (it * 1000f) / ML_PER_CUP }
                Amount(cups, maxCups, Units.CUP)
            }

            else -> toImperial(amount) // Fall back to imperial conversion for other units
        }
    }

    fun toImperial(amount: Amount): Amount {
        return when (amount.unit) {
            Units.GRAM -> {
                val minOunces = amount.min / GRAMS_PER_ONCE
                val maxOunces = amount.max?.let { it / GRAMS_PER_ONCE }
                Amount(minOunces, maxOunces, Units.OUNCE)
            }

            Units.KILOGRAM -> {
                val minPounds = amount.min / KILOGRAMS_PER_POUND
                val maxPounds = amount.max?.let { it / KILOGRAMS_PER_POUND }
                Amount(minPounds, maxPounds, Units.POUND)
            }

            Units.CENTIMETRE ->{
                val minInches = amount.min / CM_PER_INCH
                val maxInches = amount.max?.let { it / CM_PER_INCH }
                Amount(minInches, maxInches, Units.INCH)
            }

            Units.MILLIMETRE -> {
                val minInches = amount.min / CM_PER_INCH / 10f
                val maxInches = amount.max?.let { it / CM_PER_INCH / 10f }
                Amount(minInches, maxInches, Units.INCH)
            }

            Units.LITRE -> {
                val minQuarts = amount.min / LITRE_PER_QUART
                val maxQuarts = amount.max?.let { it / LITRE_PER_QUART }
                Amount(minQuarts, maxQuarts, Units.QUART)
            }

            else -> amount
        }
    }

    fun convertUnitSystem(amount: Amount, target: MeasuringSystem): Amount {
        if (amount.unit == null
            || amount.unit.measuringSystem == target
            || target == MeasuringSystem.Metric) {
            return amount
        }

        return if (amount.usCust == true && target == MeasuringSystem.USCustomary) {
            toUSCustomary(amount)
        } else if (target == MeasuringSystem.Imperial || target == MeasuringSystem.USCustomary) {
            toImperial(amount)
        } else {
            amount
        }

    }
}