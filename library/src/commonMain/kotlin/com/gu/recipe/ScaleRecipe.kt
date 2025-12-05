package com.gu.recipe

import com.gu.recipe.generated.*
import com.gu.recipe.template.OvenTemperaturePlaceholder
import com.gu.recipe.template.ParsedTemplate
import com.gu.recipe.template.QuantityPlaceholder
import com.gu.recipe.template.TemplateConst
import com.gu.recipe.template.TemplateElement
import com.gu.recipe.template.parseTemplate
import kotlin.math.max
import kotlin.math.round



internal fun formatFraction(number: Float): String {
    val integerPart = number.toInt()
    val fractionalPart = number - integerPart
    val fractionString = when (fractionalPart) {
        in 0.12f..0.13f -> "⅛"
        in 0.33f..0.34f -> "⅓"
        in 0.66f..0.67f -> "⅔"
        0.25f -> "¼"
        0.5f -> "½"
        0.75f -> "¾"
        else -> null
    }
    return if (fractionString != null) {
        if (integerPart > 0) {
            "$integerPart$fractionString"
        } else {
            fractionString
        }
    } else {
        number.toString()
    }
}

internal fun formatAmount(number: Float, decimals: Int, fraction: Boolean): String {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    val roundedNumber = round(number * multiplier) / multiplier
    // If the number is an integer, don't show decimal places
    if (roundedNumber % 1.0 == 0.0) {
        return roundedNumber.toInt().toString()
    }

    if (fraction) {
        return formatFraction(roundedNumber.toFloat())
    }
    return roundedNumber.toString()
}

internal fun renderOvenTemperature(element: OvenTemperaturePlaceholder): String {
    val fanTempC = element.temperatureFanC?.let {
        if (element.temperatureC == null) {
            "${element.temperatureFanC}C fan"
        } else {
            " (${element.temperatureFanC}C fan)"
        }
    }
    return listOfNotNull(
        element.temperatureC?.let { "${element.temperatureC}C" },
        fanTempC,
        element.temperatureF?.let { "/${it}F" },
        element.gasMark?.let { "/gas mark ${formatFraction(it)}" }
    ).joinToString("")
}

internal fun renderQuantity(element: QuantityPlaceholder, factor: Float, measuringSystem: MeasuringSystem): String {
    val maxValue = if (element.min != element.max) element.max else null

    val minMax: Pair<Float, Float?> = if (element.scale) {
        val scaledMin = (element.min * factor)
        val scaledMax = maxValue?.let { (it * factor) }

        Pair(scaledMin, scaledMax)
    } else {
        Pair(element.min, maxValue)
    }

    var unit = element.unit?.let { Units.findUnit(it) }

    // should we convert units?
    if (measuringSystem == MeasuringSystem.Imperial &&
        unit?.measuringSystem == MeasuringSystem.Metric &&
        unit?.unitType == UnitType.WEIGHT
    ) {
        unit = Units.OUNCE
    }

    val decimals = when (unit) {
        Units.GRAM, Units.MILLILITER, Units.MILLIMETER -> 0
        Units.CENTIMETER, Units.INCH -> 1
        else -> 2
    }

    val fraction = when (unit) {
        Units.TEASPOON, Units.TABLESPOON, Units.CUP -> true
        null -> true
        else -> false
    }

    val unitString = if (unit != null) {
        if (max(minMax.first, minMax.second ?: minMax.first) > 1) {
            " ${unit.symbolPlural}"
        } else {
            " ${unit.symbol}"
        }
    } else ""

    if (minMax.second != null) {
        return "${formatAmount(minMax.first, decimals, fraction)}-${formatAmount(minMax.second?: 0f, decimals, fraction)}$unitString"
    } else {
        return "${formatAmount(minMax.first, decimals, fraction)}$unitString"
    }
}

internal fun renderTemplateElement(element: TemplateElement, factor: Float, measuringSystem: MeasuringSystem): String {
    return when (element) {
        is TemplateConst -> element.value
        is QuantityPlaceholder -> renderQuantity(element, factor, measuringSystem)
        is OvenTemperaturePlaceholder -> renderOvenTemperature(element)
    }
}

internal fun renderTemplate(template: ParsedTemplate, factor: Float, measuringSystem: MeasuringSystem): String {
    val scaledParts = template.elements.map { element ->
        renderTemplateElement(element, factor, measuringSystem)
    }

    return scaledParts.joinToString("")
}

/**
 * scaleAndConvertUnitRecipe used to convert units and scale recipe
 *
 * @param recipe The recipe as provided by the server (RecipeV3)
 * @param factor The factor applied to change the proportions of the recipe.
 *  For instance 0.5 halves the recipe and 2 doubles it.
 *  To calculate the factor, take the number of desired servings and divide it by the original servings.
 * @param measuringSystem The target unit system for ingredient measurements (e.g., Metric or Imperial)
 */
fun scaleAndConvertUnitRecipe(recipe: RecipeV3, factor: Float, measuringSystem: MeasuringSystem): RecipeV3 {
    val scaledIngredients = recipe.ingredients?.map { ingredientSection ->
        IngredientsList(
            ingredientsList = ingredientSection.ingredientsList?.map { templateIngredient ->
                val scaledText = templateIngredient.template?.let { template ->
                    renderTemplate(parseTemplate(template), factor, measuringSystem)
                } ?: templateIngredient.text

                templateIngredient.copy(text = scaledText)
            },
            recipeSection = ingredientSection.recipeSection
        )
    }
    val scaledInstructions = recipe.instructions?.map { instruction ->
        val description = instruction.descriptionTemplate?.let { template ->
            renderTemplate(parseTemplate(template), factor, measuringSystem)
        }
        instruction.copy(description = description ?: instruction.description)
    }

    return recipe.copy(ingredients = scaledIngredients, instructions = scaledInstructions)
}