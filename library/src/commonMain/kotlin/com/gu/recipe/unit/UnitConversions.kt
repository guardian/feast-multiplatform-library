package com.gu.recipe.unit

import com.gu.recipe.Amount
import kotlin.math.floor
import kotlin.math.roundToInt

object UnitConversions {
    private val CUPS_PER_ML = 0.00422675f
    private val TSP_PER_ML = 0.202884f

    val CONVENIENCE_UNITS = setOf(
        Units.METRIC_TEASPOON,
        Units.METRIC_TABLESPOON,
        Units.METRIC_CUP,
    )
    val METRIC_CONVENIENCE_LADDER = listOf<Pair<Float, MeasurementUnit>>(
        0f to Units.GRAM,
        1000f to Units.KILOGRAM,

        0f to Units.MILLILITRE,
        2.5f to Units.METRIC_TEASPOON, // start using teaspoons at 1/2 tsp
        15f to Units.METRIC_TABLESPOON,
        125f to Units.METRIC_CUP, // start using cups at 1/2 cup
        1000f to Units.LITRE,

        0f to Units.MILLIMETRE,
        10f to Units.CENTIMETRE,
    )

    val METRIC_CONVERSION_LADDER = listOf<Pair<Float, MeasurementUnit>>(
        0f to Units.GRAM,
        1000f to Units.KILOGRAM,

        0f to Units.MILLILITRE,
        1000f to Units.LITRE,

        0f to Units.MILLIMETRE,
        10f to Units.CENTIMETRE,
    )

    val US_CUSTOMARY_CONVERSION_LADDER = listOf<Pair<Float, MeasurementUnit>>(
        0f to Units.OUNCE,
        16f * Units.OUNCE.quantity to Units.POUND,

        0f to Units.US_TEASPOON,
        16f * Units.US_TEASPOON.quantity to Units.US_TABLESPOON,
        48f * Units.US_TEASPOON.quantity to Units.US_CUP,
        192 * Units.US_TEASPOON.quantity to Units.US_QUART,
        768f * Units.US_TEASPOON.quantity to Units.US_GALLON,

        0f to Units.INCH,
    )

    val IMPERIAL_CONVERSION_LADDER = listOf<Pair<Float, MeasurementUnit>>(
        0f to Units.OUNCE,
        16f * Units.OUNCE.quantity to Units.POUND,

        0f to Units.US_TEASPOON,
        3f * Units.US_TEASPOON.quantity to Units.US_TABLESPOON,
        6f * Units.US_TEASPOON.quantity to Units.FLUID_OUNCE,
        768f * Units.US_TEASPOON.quantity to Units.US_GALLON,

        0f to Units.INCH,
    )

    /**
     * Converts the given amount to the smallest unit in its measuring system, making jumping from one system to another easier
     */
    fun toSmallestUnit(amount: Amount): Amount {
        return if (amount.unit != null) {
            amount.copy(
                min = amount.min * amount.unit.quantity,
                max = amount.max?.let { it * amount.unit.quantity },
                unit = when (amount.unit.unitType) {
                    UnitType.LENGTH -> Units.MILLIMETRE
                    UnitType.VOLUME -> Units.MILLILITRE
                    UnitType.WEIGHT -> Units.GRAM
                    else -> amount.unit
                },
            )
        } else {
            amount
        }
    }

    fun convertUnitSystemAndScale(amount: Amount, target: MeasuringSystem.MeasuringSystemInternal, factor: Float = 1f, density: Float?): Amount {
        val scaledAmount = amount.copy(min = amount.min * factor, max = amount.max?.let { it * factor })

        if (scaledAmount.unit == null || (target == MeasuringSystem.Metric && factor == 1f)) {
            return scaledAmount
        }

        val smallestUnitAmount = toSmallestUnit(scaledAmount)
        println(smallestUnitAmount)
        val ladder = when (target) {
            MeasuringSystem.Metric -> if (CONVENIENCE_UNITS.contains(amount.unit))
                METRIC_CONVENIENCE_LADDER
            else
                METRIC_CONVERSION_LADDER

            MeasuringSystem.USCustomary -> US_CUSTOMARY_CONVERSION_LADDER

            MeasuringSystem.Imperial -> IMPERIAL_CONVERSION_LADDER
        }

        val amountToConvert = if(
            amount.usCust==true &&
            target== MeasuringSystem.USCustomary &&
            density!=null &&
            amount.unit?.unitType== UnitType.WEIGHT
            ) {
            //convert from g to ml. Metric -> US unit conversion is done below.
            // Assume that incoming weight here is in g (smallest unit in metric set)
            //density is in g/ml, so divide by density to go g -> ml
            Amount(
                min=smallestUnitAmount.min / density,
                max=smallestUnitAmount.max?.let { (it / density) },
                unit=Units.MILLILITRE,
            )
        } else {
            smallestUnitAmount
        }

        val mostRelevantUnit = ladder
            .filter { it.second.unitType == amountToConvert.unit?.unitType }
            .lastOrNull { amountToConvert.min >= it.first }?.second

        return mostRelevantUnit?.let {
            Amount(
                min = floor((amountToConvert.min / it.quantity) + 0.1f),    //the extra +0.1 ensures that anything over 0.89999999 goes up
                max = amountToConvert.max?.let { max -> floor((max / it.quantity) + 0.1f) },
                unit = it,
                remainderMin = (amountToConvert.min % it.quantity) / it.quantity,
                remainderMax = amountToConvert.max?.let { max -> (max % it.quantity) / it.quantity },
            )
        } ?: amountToConvert
    }
}