package com.gu.recipe.unit

import com.gu.recipe.Amount

object UnitConversion {

    val ONCES_IN_GRAM = 28.3495f
    val POUNDS_IN_KILOGRAM = 2.20462f
    val ML_IN_CUP = 236.56f

    val CM_IN_INCH = 2.54f

    fun toUSCustomary(amount: Amount): Amount {
        return when (amount.unit) {
            Units.MILLILITRE -> {
                val cups = amount.min / ML_IN_CUP
                val maxCups = amount.max?.let { it / ML_IN_CUP }
                Amount(cups, maxCups, Units.CUP)
            }

            Units.LITRE -> {
                val cups = (amount.min * 1000f) / ML_IN_CUP
                val maxCups = amount.max?.let { (it * 1000f) / ML_IN_CUP }
                Amount(cups, maxCups, Units.CUP)
            }

            else -> toImperial(amount) // Fall back to imperial conversion for other units
        }
    }

    fun toImperial(amount: Amount): Amount {
        return when (amount.unit) {
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

            Units.CENTIMETRE ->{
                val minInches = amount.min / CM_IN_INCH
                val maxInches = amount.max?.let { it / CM_IN_INCH }
                Amount(minInches, maxInches, Units.INCH)
            }

            Units.MILLIMETRE -> {
                val minInches = amount.min / CM_IN_INCH / 10f
                val maxInches = amount.max?.let { it / CM_IN_INCH / 10f }
                Amount(minInches, maxInches, Units.INCH)
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