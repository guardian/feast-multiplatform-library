package com.gu.recipe

import com.gu.recipe.FormatUtils.applySmartPunctuation
import com.gu.recipe.generated.*
import com.gu.recipe.template.OvenTemperaturePlaceholder
import com.gu.recipe.template.ParsedTemplate
import com.gu.recipe.template.QuantityPlaceholder
import com.gu.recipe.template.TemplateConst
import com.gu.recipe.template.TemplateElement
import com.gu.recipe.template.parseTemplate
import com.gu.recipe.unit.MeasuringSystem
import com.gu.recipe.unit.UnitConversion
import com.gu.recipe.unit.Units
import kotlin.math.max


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
        element.gasMark?.let { "/gas mark ${FormatUtils.formatToNearestFraction(it)}" }
    ).joinToString("")
}

internal fun scaleAmount(amount: Amount, factor: Float, shouldScale: Boolean): Amount {
    if (!shouldScale) {
        return amount
    }
    return amount.copy(
        min = amount.min * factor,
        max = amount.max?.let { it * factor }
    )
}

internal fun renderQuantity(element: QuantityPlaceholder, factor: Float, measuringSystem: MeasuringSystem): String {
    var amount = Amount(
        min = element.min,
        max = if (element.min != element.max) element.max else null,
        unit = element.unit?.let { Units.findUnit(it) },
        usCust = element.usCust,
    )

    amount = scaleAmount(amount, factor, element.scale)
    amount = UnitConversion.convertUnitSystem(amount, measuringSystem)

    val decimals = when (amount.unit) {
        Units.GRAM, Units.MILLILITER, Units.MILLIMETER -> 0
        Units.CENTIMETER, Units.INCH -> 1
        else -> 2
    }

    val fraction = when (amount.unit) {
        Units.TEASPOON, Units.TABLESPOON, Units.CUP -> true
        null -> true
        else -> false
    }

    val unitString = if (amount.unit != null) {
        if (max(amount.min, amount.max ?: amount.min) > 1) {
            " ${amount.unit.symbolPlural}"
        } else {
            " ${amount.unit.symbol}"
        }
    } else ""

    return listOfNotNull(
        FormatUtils.formatAmount(amount.min, decimals, fraction),
        amount.max?.let { "-" + FormatUtils.formatAmount(it, decimals, fraction) },
        unitString,
    ).joinToString("")
}

internal fun renderTemplateElement(element: TemplateElement, factor: Float, measuringSystem: MeasuringSystem): String {
    return when (element) {
        is TemplateConst -> element.value
        is QuantityPlaceholder -> renderQuantity(element, factor, measuringSystem)
        is OvenTemperaturePlaceholder -> renderOvenTemperature(element)
    }
}

internal fun renderTemplate(template: ParsedTemplate, factor: Float, measuringSystem: MeasuringSystem): String {
    val renderedParts = template.elements.map { element ->
        renderTemplateElement(element, factor, measuringSystem)
    }

    return applySmartPunctuation(renderedParts.joinToString(""))
}

internal fun wrapWithStrongTag(value: String): String {
    val before = ingredientWithoutSuffix(value)
    val after = value.substring(before.length)
    return """<strong>$before</strong>$after"""
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
                    wrapWithStrongTag(renderTemplate(parseTemplate(template), factor, measuringSystem))
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

fun ingredientWithoutSuffix(renderedTemplate: String): String {
    return renderedTemplate.substringBefore(",")
}