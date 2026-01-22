package com.gu.recipe.unit

sealed interface MeasuringSystem {
    object Imperial : MeasuringSystem
    object Metric : MeasuringSystem
    object USCustomary : MeasuringSystem // will try to convert to US customary units where possible, falling back to Imperial
}
enum class UnitType {
    WEIGHT,
    VOLUME,
    LENGTH,
}

data class MeasurementUnit(
    val singular: String,
    val plural: String,
    val symbol: String,
    val symbolPlural: String,
    val unitType: UnitType?,
    val measuringSystems: Set<MeasuringSystem>,
    val quantity: Float, // quantity this unit represents in the metric system: ml for volume, g for weight, mm for length
)

object Units {
    val GRAM = MeasurementUnit(
        singular = "gram",
        plural = "grams",
        symbol = "g",
        symbolPlural = "g",
        unitType = UnitType.WEIGHT,
        measuringSystems = setOf(MeasuringSystem.Metric),
        quantity = 1f,
    )

    val KILOGRAM = MeasurementUnit(
        singular = "kilogram",
        plural = "kilograms",
        symbol = "kg",
        symbolPlural = "kg",
        unitType = UnitType.WEIGHT,
        measuringSystems = setOf(MeasuringSystem.Metric),
        quantity = 1000f,
    )

    val OUNCE = MeasurementUnit(
        singular = "ounce",
        plural = "ounces",
        symbol = "oz",
        symbolPlural = "oz",
        unitType = UnitType.WEIGHT,
        measuringSystems = setOf(MeasuringSystem.Imperial, MeasuringSystem.USCustomary),
        quantity = 28.3495f,
    )

    val POUND = MeasurementUnit(
        singular = "pound",
        plural = "pounds",
        symbol = "lb",
        symbolPlural = "lbs",
        unitType = UnitType.WEIGHT,
        measuringSystems = setOf(MeasuringSystem.Imperial, MeasuringSystem.USCustomary),
        quantity = 16 * OUNCE.quantity,
    )

    val MILLILITRE = MeasurementUnit(
        singular = "millilitre",
        plural = "millilitres",
        symbol = "ml",
        symbolPlural = "ml",
        unitType = UnitType.VOLUME,
        measuringSystems = setOf(MeasuringSystem.Metric),
        quantity = 1f,
    )

    val CENTILITRE = MeasurementUnit(
        singular = "centilitre",
        plural = "centilitres",
        symbol = "cl",
        symbolPlural = "cl",
        unitType = UnitType.VOLUME,
        measuringSystems = setOf(MeasuringSystem.Metric),
        quantity = 10f,
    )

    val LITRE = MeasurementUnit(
        singular = "litre",
        plural = "litres",
        symbol = "l",
        symbolPlural = "l",
        unitType = UnitType.VOLUME,
        measuringSystems = setOf(MeasuringSystem.Metric),
        quantity = 1000f,
    )

    val METRIC_TEASPOON = MeasurementUnit(
        singular = "teaspoon",
        plural = "teaspoons",
        symbol = "tsp",
        symbolPlural = "tsp",
        unitType = UnitType.VOLUME,
        measuringSystems = setOf(MeasuringSystem.Metric),
        quantity = 5f
    )

    val METRIC_TABLESPOON = MeasurementUnit(
        singular = "tablespoon",
        plural = "tablespoons",
        symbol = "tbsp",
        symbolPlural = "tbsp",
        unitType = UnitType.VOLUME,
        measuringSystems = setOf(MeasuringSystem.Metric),
        quantity = 15f
    )

    val US_TEASPOON = MeasurementUnit(
        singular = "teaspoon",
        plural = "teaspoons",
        symbol = "tsp",
        symbolPlural = "tsp",
        unitType = UnitType.VOLUME,
        measuringSystems = setOf(MeasuringSystem.Imperial, MeasuringSystem.USCustomary),
        quantity = 4.9289f
    )

    val US_TABLESPOON = MeasurementUnit(
        singular = "tablespoon",
        plural = "tablespoons",
        symbol = "tbsp",
        symbolPlural = "tbsp",
        unitType = UnitType.VOLUME,
        measuringSystems = setOf(MeasuringSystem.Imperial, MeasuringSystem.USCustomary),
        quantity = 3 * US_TEASPOON.quantity,
    )

    val FLUID_OUNCE = MeasurementUnit(
        singular = "fluid ounce",
        plural = "fluid ounces",
        symbol = "fl oz",
        symbolPlural = "fl oz",
        unitType = UnitType.VOLUME,
        measuringSystems = setOf(MeasuringSystem.Imperial),
        quantity = 6 * US_TEASPOON.quantity,
    )

    val US_CUP = MeasurementUnit(
        singular = "cup",
        plural = "cups",
        symbol = "cup",
        symbolPlural = "cups",
        unitType = UnitType.VOLUME,
        measuringSystems = setOf(MeasuringSystem.USCustomary),
        quantity = 48 * US_TEASPOON.quantity,
    )

    val METRIC_CUP = MeasurementUnit(
        singular = "cup",
        plural = "cups",
        symbol = "cup",
        symbolPlural = "cups",
        unitType = UnitType.VOLUME,
        measuringSystems = setOf(MeasuringSystem.Metric),
        quantity = 250f,
    )

    val US_PINT = MeasurementUnit(
        singular = "pint",
        plural = "pints",
        symbol = "pt",
        symbolPlural = "pts",
        unitType = UnitType.VOLUME,
        measuringSystems = setOf(MeasuringSystem.Imperial, MeasuringSystem.USCustomary),
        quantity = 2 * US_CUP.quantity,
    )

    val US_QUART = MeasurementUnit(
        singular = "quart",
        plural = "quarts",
        symbol = "qt",
        symbolPlural = "qts",
        unitType = UnitType.VOLUME,
        measuringSystems = setOf(MeasuringSystem.Imperial, MeasuringSystem.USCustomary),
        quantity = 2 * US_PINT.quantity,
    )

    val US_GALLON = MeasurementUnit(
        singular = "gallon",
        plural = "gallons",
        symbol = "gal",
        symbolPlural = "gals",
        unitType = UnitType.VOLUME,
        measuringSystems = setOf(MeasuringSystem.Imperial, MeasuringSystem.USCustomary),
        quantity = 4 * US_QUART.quantity,
    )

    val MILLIMETRE = MeasurementUnit(
        singular = "millimetre",
        plural = "millimetres",
        symbol = "mm",
        symbolPlural = "mm",
        unitType = UnitType.LENGTH,
        measuringSystems = setOf(MeasuringSystem.Metric),
        quantity = 1f,
    )

    val CENTIMETRE = MeasurementUnit(
        singular = "centimetre",
        plural = "centimetres",
        symbol = "cm",
        symbolPlural = "cm",
        unitType = UnitType.LENGTH,
        measuringSystems = setOf(MeasuringSystem.Metric),
        quantity = 10f,
    )

    val INCH = MeasurementUnit(
        singular = "inch",
        plural = "inches",
        symbol = "in",
        symbolPlural = "in",
        unitType = UnitType.LENGTH,
        measuringSystems = setOf(MeasuringSystem.Imperial),
        quantity = 25.4f,
    )

    val ALL_UNITS = listOf(
        GRAM,
        KILOGRAM,
        OUNCE,
        POUND,
        MILLILITRE,
        CENTILITRE,
        LITRE,
        METRIC_TEASPOON,
        METRIC_TABLESPOON,
        US_TEASPOON,
        US_TABLESPOON,
        FLUID_OUNCE,
        US_CUP,
        METRIC_CUP,
        US_PINT,
        US_QUART,
        US_GALLON,
        MILLIMETRE,
        CENTIMETRE,
        INCH,
    )

    val METRIC_UNIT_FROM_SYMBOL: Map<String, MeasurementUnit> =
        ALL_UNITS.filter { it.measuringSystems.contains(MeasuringSystem.Metric) }
            .let {
                it.associateBy { it.symbol } +
                        it.associateBy { it.symbolPlural } +
                        it.associateBy { it.singular } +
                        it.associateBy { it.plural }
            }

    val IMPERIAL_UNIT_FROM_SYMBOL: Map<String, MeasurementUnit> =
        ALL_UNITS.filter { it.measuringSystems.contains(MeasuringSystem.Imperial) }
            .let {
                it.associateBy { it.symbol } +
                        it.associateBy { it.symbolPlural } +
                        it.associateBy { it.singular } +
                        it.associateBy { it.plural }
            }

    val US_CUSTOMARY_UNIT_FROM_SYMBOL: Map<String, MeasurementUnit> =
        ALL_UNITS.filter { it.measuringSystems.contains(MeasuringSystem.USCustomary) }
            .let {
                it.associateBy { it.symbol } +
                        it.associateBy { it.symbolPlural } +
                        it.associateBy { it.singular } +
                        it.associateBy { it.plural }
            }

    /**
     * Find a unit coming from a recipe.
     * Because we know it's coming from a recipe we bias it towards metric units first
     */
    fun findRecipeUnit(name: String): MeasurementUnit {
        val unit = METRIC_UNIT_FROM_SYMBOL[name]
            ?: IMPERIAL_UNIT_FROM_SYMBOL[name]
            ?: US_CUSTOMARY_UNIT_FROM_SYMBOL[name]

        if (unit != null) {
            return unit
        } else {
            return MeasurementUnit(
                singular = name,
                plural = name,
                symbol = name,
                symbolPlural = name,
                unitType = null,
                measuringSystems = setOf<MeasuringSystem>(),
                quantity = 1f
            )
        }
    }
}
