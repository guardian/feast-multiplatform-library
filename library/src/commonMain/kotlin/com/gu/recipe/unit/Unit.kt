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

data class Unit(
    val singular: String,
    val plural: String,
    val symbol: String,
    val symbolPlural: String,
    val unitType: UnitType?,
    val measuringSystem: MeasuringSystem?,
)

object Units {
    val GRAM = Unit(
        singular = "gram",
        plural = "grams",
        symbol = "g",
        symbolPlural = "g",
        unitType = UnitType.WEIGHT,
        measuringSystem = MeasuringSystem.Metric,
    )

    val KILOGRAM = Unit(
        singular = "kilogram",
        plural = "kilograms",
        symbol = "kg",
        symbolPlural = "kg",
        unitType = UnitType.WEIGHT,
        measuringSystem = MeasuringSystem.Metric,
    )

    val OUNCE = Unit(
        singular = "ounce",
        plural = "ounces",
        symbol = "oz",
        symbolPlural = "oz",
        unitType = UnitType.WEIGHT,
        measuringSystem = MeasuringSystem.Imperial,
    )

    val POUND = Unit(
        singular = "pound",
        plural = "pounds",
        symbol = "lb",
        symbolPlural = "lbs",
        unitType = UnitType.WEIGHT,
        measuringSystem = MeasuringSystem.Imperial,
    )

    val MILLILITRE = Unit(
        singular = "millilitre",
        plural = "millilitres",
        symbol = "ml",
        symbolPlural = "ml",
        unitType = UnitType.VOLUME,
        measuringSystem = MeasuringSystem.Metric,
    )

    val CENTILITRE = Unit(
        singular = "centilitre",
        plural = "centilitres",
        symbol = "cl",
        symbolPlural = "cl",
        unitType = UnitType.VOLUME,
        measuringSystem = MeasuringSystem.Metric,
    )

    val LITRE = Unit(
        singular = "litre",
        plural = "litres",
        symbol = "l",
        symbolPlural = "l",
        unitType = UnitType.VOLUME,
        measuringSystem = MeasuringSystem.Metric,
    )

    val FLUID_OUNCE = Unit(
        singular = "fluid ounce",
        plural = "fluid ounces",
        symbol = "fl oz",
        symbolPlural = "fl oz",
        unitType = UnitType.VOLUME,
        measuringSystem = MeasuringSystem.Imperial,
    )

    val CUP = Unit(
        singular = "cup",
        plural = "cups",
        symbol = "cup",
        symbolPlural = "cups",
        unitType = UnitType.VOLUME,
        measuringSystem = MeasuringSystem.Imperial,
    )

    val TEASPOON = Unit(
        singular = "teaspoon",
        plural = "teaspoons",
        symbol = "tsp",
        symbolPlural = "tsp",
        unitType = UnitType.VOLUME,
        measuringSystem = MeasuringSystem.Imperial,
    )

    val TABLESPOON = Unit(
        singular = "tablespoon",
        plural = "tablespoons",
        symbol = "tbsp",
        symbolPlural = "tbsp",
        unitType = UnitType.VOLUME,
        measuringSystem = MeasuringSystem.Imperial,
    )

    val PINT = Unit(
        singular = "pint",
        plural = "pints",
        symbol = "pt",
        symbolPlural = "pts",
        unitType = UnitType.VOLUME,
        measuringSystem = MeasuringSystem.Imperial,
    )

    val QUART = Unit(
        singular = "quart",
        plural = "quarts",
        symbol = "qt",
        symbolPlural = "qts",
        unitType = UnitType.VOLUME,
        measuringSystem = MeasuringSystem.Imperial,
    )

    val GALLON = Unit(
        singular = "gallon",
        plural = "gallons",
        symbol = "gal",
        symbolPlural = "gals",
        unitType = UnitType.VOLUME,
        measuringSystem = MeasuringSystem.Imperial,
    )

    val MILLIMETRE = Unit(
        singular = "millimetre",
        plural = "millimetres",
        symbol = "mm",
        symbolPlural = "mm",
        unitType = UnitType.LENGTH,
        measuringSystem = MeasuringSystem.Metric,
    )

    val CENTIMETRE = Unit(
        singular = "centimetre",
        plural = "centimetres",
        symbol = "cm",
        symbolPlural = "cm",
        unitType = UnitType.LENGTH,
        measuringSystem = MeasuringSystem.Metric,
    )

    val INCH = Unit(
        singular = "inch",
        plural = "inches",
        symbol = "in",
        symbolPlural = "in",
        unitType = UnitType.LENGTH,
        measuringSystem = MeasuringSystem.Imperial,
    )

    val ALL_UNITS = listOf(
        GRAM,
        KILOGRAM,
        OUNCE,
        POUND,
        MILLILITRE,
        LITRE,
        FLUID_OUNCE,
        CUP,
        TEASPOON,
        TABLESPOON,
        MILLIMETRE,
        CENTIMETRE,
        INCH,
        PINT,
        QUART,
        GALLON,
    )

    val UNIT_FROM_SYMBOL: Map<String, Unit> =
        ALL_UNITS.associateBy { it.symbol} +
                ALL_UNITS.associateBy { it.symbolPlural} +
                ALL_UNITS.associateBy { it.singular} +
                ALL_UNITS.associateBy { it.plural}

    fun findUnit(name: String): Unit {
        val unit = UNIT_FROM_SYMBOL[name]
        if (unit != null) {
            return unit
        } else {
            return Unit(
                singular = name,
                plural = name,
                symbol = name,
                symbolPlural = name,
                unitType = null,
                measuringSystem = null,
            )
        }
    }
}
