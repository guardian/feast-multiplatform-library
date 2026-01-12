package com.gu.recipe.unit

import com.gu.recipe.Amount

data class UnitConversion(
    val fromUnit: Unit,
    val toUnit: Unit,
    // expressed in fromUnit per toUnit, so ml per teaspoon etc
    val conversionRatio: Float,
    // above which amount (expressed in fromUnit) should we use this conversion. eg > 6 teaspoons -> tablespoons
    val threshold: Float,
)

object UnitConversions {
    // Volume
    val ML_TO_TEASPOON = UnitConversion(
        fromUnit = Units.MILLILITRE,
        toUnit = Units.TEASPOON,
        conversionRatio = 4.9289f, // in ml per teaspoon
        threshold = 0f
    )

    val ML_TO_TABLESPOON = UnitConversion(
        fromUnit = Units.MILLILITRE,
        toUnit = Units.TABLESPOON,
        conversionRatio = 3 * ML_TO_TEASPOON.conversionRatio,
        threshold = 6 * ML_TO_TEASPOON.conversionRatio
    )

    val ML_TO_CUP = UnitConversion(
        fromUnit = Units.MILLILITRE,
        toUnit = Units.CUP,
        conversionRatio = 48 * ML_TO_TEASPOON.conversionRatio,
        threshold = 4 * ML_TO_TABLESPOON.conversionRatio // 4 tablespoons = 1/4 cup
    )

    val ML_TO_FLUID_OUNCE = UnitConversion(
        fromUnit = Units.MILLILITRE,
        toUnit = Units.FLUID_OUNCE,
        conversionRatio = 6 * ML_TO_TEASPOON.conversionRatio,
        threshold = 0f
    )

    val ML_TO_QUART = UnitConversion(
        fromUnit = Units.MILLILITRE,
        toUnit = Units.QUART,
        conversionRatio = 4 * ML_TO_CUP.conversionRatio,
        threshold = 4 * ML_TO_CUP.conversionRatio
    )

    val ML_TO_GALLON = UnitConversion(
        fromUnit = Units.MILLILITRE,
        toUnit = Units.GALLON,
        conversionRatio = 16 * ML_TO_CUP.conversionRatio,
        threshold = 16 * ML_TO_CUP.conversionRatio
    )

    // Weight
    val GRAMS_TO_OUNCE = UnitConversion(
        fromUnit = Units.GRAM,
        toUnit = Units.OUNCE,
        conversionRatio = 28.3495f,
        threshold = 0f
    )

    val GRAMS_TO_POUND = UnitConversion(
        fromUnit = Units.GRAM,
        toUnit = Units.POUND,
        conversionRatio = 16 * GRAMS_TO_OUNCE.conversionRatio,
        threshold = 16 * GRAMS_TO_OUNCE.conversionRatio
    )

    // Length
    val MM_TO_INCH = UnitConversion(
        fromUnit = Units.MILLIMETRE,
        toUnit = Units.INCH,
        conversionRatio = 25.4f,
        threshold = 0f
    )

    val US_CUSTOMARY_VOLUMES = listOf(
        ML_TO_TEASPOON,
        ML_TO_TABLESPOON,
        ML_TO_CUP,
        ML_TO_QUART,
        ML_TO_GALLON,
    )

    val IMPERIAL_VOLUMES = listOf(
        ML_TO_TEASPOON,
        ML_TO_TABLESPOON,
        ML_TO_FLUID_OUNCE,
        ML_TO_QUART,
        ML_TO_GALLON,
    )

    val IMPERIAL_WEIGHTS = listOf(
        GRAMS_TO_OUNCE,
        GRAMS_TO_POUND,
    )

    val IMPERIAL_LENGTHS = listOf(
        MM_TO_INCH,
    )

    /**
     * Converts the given amount to the smallest unit in its measuring system, making jumping from one system to another easier
     */
    fun toSmallestUnit(amount: Amount): Amount {
        return when (amount.unit) {
            Units.KILOGRAM -> {
                val grams = amount.min * 1000f
                val maxGrams = amount.max?.let { it * 1000f }
                Amount(grams, maxGrams, Units.GRAM)
            }

            Units.CENTILITRE -> {
                val millilitres = amount.min * 10f
                val maxMillilitres = amount.max?.let { it * 10f }
                Amount(millilitres, maxMillilitres, Units.MILLILITRE)
            }

            Units.LITRE -> {
                val millilitres = amount.min * 1000f
                val maxMillilitres = amount.max?.let { it * 1000f }
                Amount(millilitres, maxMillilitres, Units.MILLILITRE)
            }

            Units.CENTIMETRE -> {
                val millimetres = amount.min * 10f
                val maxMillimetres = amount.max?.let { it * 10f }
                Amount(millimetres, maxMillimetres, Units.MILLIMETRE)
            }

            else -> amount
        }
    }

    /**
     * Finds the most relvant unit for the given amount based on the provided conversions
     * amount: in fromUnit
     * conversions: ordered list of conversions to try, ordered by increasing threshold
     */
    fun convertToMostRelevantUnit(amount: Amount, conversions: List<UnitConversion>): Amount {
        // find first valid conversion
        val conversion = conversions.lastOrNull {
            it.fromUnit == amount.unit && amount.min >= it.threshold
        }
        return if (conversion != null) {
            val newMin = amount.min / conversion.conversionRatio
            val newMax = amount.max?.let { it / conversion.conversionRatio }
            Amount(newMin, newMax, conversion.toUnit)
        } else {
            amount
        }
    }

    fun toUSCustomary(amount: Amount): Amount {
        val smallestUnitAmount = toSmallestUnit(amount)
        return when (smallestUnitAmount.unit) {
            Units.MILLILITRE -> {
                convertToMostRelevantUnit(smallestUnitAmount, US_CUSTOMARY_VOLUMES)
            }

            Units.GRAM -> {
                convertToMostRelevantUnit(smallestUnitAmount, IMPERIAL_WEIGHTS)
            }

            Units.MILLIMETRE -> {
                convertToMostRelevantUnit(smallestUnitAmount, IMPERIAL_LENGTHS)
            }

            else -> {
                amount
            }
        }
    }

    fun toImperial(amount: Amount): Amount {
        val smallestUnitAmount = toSmallestUnit(amount)
        return when (smallestUnitAmount.unit) {
            Units.MILLILITRE -> {
                convertToMostRelevantUnit(smallestUnitAmount, IMPERIAL_VOLUMES)
            }

            Units.GRAM -> {
                convertToMostRelevantUnit(smallestUnitAmount, IMPERIAL_WEIGHTS)
            }

            Units.MILLIMETRE -> {
                convertToMostRelevantUnit(smallestUnitAmount, IMPERIAL_LENGTHS)
            }

            else -> {
                amount
            }
        }
    }

    fun convertUnitSystem(amount: Amount, target: MeasuringSystem): Amount {
        if (amount.unit == null
            || amount.unit.measuringSystem == target
            || target == MeasuringSystem.Metric
        ) {
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