package com.gu.recipe

import com.gu.recipe.generated.*
import com.gu.recipe.template.OvenTemperaturePlaceholder
import com.gu.recipe.template.ParsedTemplate
import com.gu.recipe.template.QuantityPlaceholder
import com.gu.recipe.template.TemplateConst
import com.gu.recipe.template.TemplateElement
import com.gu.recipe.template.parseTemplate
import kotlin.math.round

sealed interface IngredientUnit {
    object Imperial : IngredientUnit
    object Metric : IngredientUnit
}

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

internal fun renderTemplateElement(element: TemplateElement, factor: Float): String {
    return when (element) {
        is TemplateConst -> element.value
        is QuantityPlaceholder -> {
            val max = if (element.min != element.max) element.max else null
            val (scaledMin, scaledMax) = if (element.scale) {
                val scaledMin = (element.min * factor)
                val scaledMax = max?.let { (it * factor) }

                Pair(scaledMin, scaledMax)
            } else {
                Pair(element.min, max)
            }
            val unit = if (element.unit != null) " ${element.unit}" else ""

            val decimals = when (unit) {
                "g", "ml" -> 0
                else -> 2
            }

            val fraction = when (element.unit) {
                "tsp", "tbsp", "cup", "cups" -> true
                null -> true
                else -> false
            }

            if (scaledMax != null) {
                "${formatAmount(scaledMin, decimals, fraction)}-${formatAmount(scaledMax, decimals, fraction)}$unit"
            } else {
                "${formatAmount(scaledMin, decimals, fraction)}$unit"
            }
        }

        is OvenTemperaturePlaceholder -> {
            val fanTempC = element.temperatureFanC?.let {
                if (element.temperatureC == null) {
                    "${element.temperatureFanC}C fan"
                } else {
                    " (${element.temperatureFanC}C fan)"
                }
            }
            listOfNotNull(
                element.temperatureC?.let { "${element.temperatureC}C" },
                fanTempC,
                element.temperatureF?.let { "/${it}F" },
                element.gasMark?.let { "/gas mark ${formatFraction(it)}" }
            ).joinToString("")
        }
    }
}

internal fun renderTemplate(template: ParsedTemplate, factor: Float): String {
    val scaledParts = template.elements.map { element ->
        renderTemplateElement(element, factor)
    }

    return scaledParts.joinToString("")
}

internal fun wrapWithStrongTag(value: String): String {
    val separators = charArrayOf(',', ';', '(')
    val index = value.indexOfAny(separators)

    if (index != -1) {
        val before = value.substring(0, index)
        val after = value.substring(index)
        return """<strong>$before</strong>$after"""
    } else {
        return """<strong>$value</strong>"""
    }
}

/**
 * scaleAndConvertUnitRecipe used to convert units and scale recipe
 *
 * @param recipe The recipe as provided by the server (RecipeV3)
 * @param factor The factor applied to change the proportions of the recipe.
 *  For instance 0.5 halves the recipe and 2 doubles it.
 *  To calculate the factor, take the number of desired servings and divide it by the original servings.
 * @param unit The target unit system for ingredient measurements (e.g., Metric or Imperial)
*/
fun scaleAndConvertUnitRecipe(recipe: RecipeV3, factor: Float, unit: IngredientUnit): RecipeV3 {
    val scaledIngredients = recipe.ingredients?.map { ingredientSection ->
        IngredientsList(
            ingredientsList = ingredientSection.ingredientsList?.map { templateIngredient ->
                val scaledText = templateIngredient.template?.let { template ->
                    wrapWithStrongTag(renderTemplate(parseTemplate(template), factor))
                } ?: templateIngredient.text

                templateIngredient.copy(text = scaledText)
            },
            recipeSection = ingredientSection.recipeSection
        )
    }
    val scaledInstructions = recipe.instructions?.map { instruction ->
        val description = instruction.descriptionTemplate?.let { template -> renderTemplate(parseTemplate(template), factor)}
        instruction.copy(description = description?: instruction.description)
    }

    return recipe.copy(ingredients = scaledIngredients, instructions = scaledInstructions)
}