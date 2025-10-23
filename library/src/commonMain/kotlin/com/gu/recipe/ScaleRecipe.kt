package com.gu.recipe

import com.gu.recipe.generated.*
import com.gu.recipe.template.ParsedTemplate
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

internal fun scaleTemplate(template: ParsedTemplate, factor: Float): String {
    val scaledParts = template.elements.map { element ->
        when (element) {
            is TemplateElement.TemplateConst -> element.value
            is TemplateElement.QuantityPlaceholder -> {
                val max = if (element.min != element.max) element.max else null
                val (scaledMin, scaledMax) = if (element.scale) {
                    val scaledMin = (element.min * factor)
                    val scaledMax = max?.let { (it * factor) }

                    Pair(scaledMin, scaledMax)
                } else {
                    Pair(element.min, max)
                }
                val unit = if (element.unit != null) " ${element.unit}" else ""

                val decimals = when(unit) {
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

            is TemplateElement.OvenTemperaturePlaceholder -> {
                var temp = "${element.temperatureC}C"
                if (element.temperatureFanC != null) {
                    temp += " (${element.temperatureFanC}C fan)"
                }
                if (element.temperatureF != null) {
                    temp += "/${element.temperatureF}F"
                }
                if (element.gasMark != null) {
                    temp += "/gas mark ${formatFraction(element.gasMark)}"
                }
                temp
            }
        }
    }

    return scaledParts.joinToString("")
}

typealias ClientSideRecipe = RecipeV2
typealias ServerSideRecipe = RecipeV3

fun scaleRecipe(recipe: ServerSideRecipe, factor: Float, unit: IngredientUnit): ClientSideRecipe {
    val scaledIngredients = recipe.ingredientsTemplate?.map { ingredientSection ->
        IngredientElement(
            ingredientsList = ingredientSection.ingredientsList?.map { templateIngredient ->
                val scaledText = templateIngredient.template?.let { template ->
                    scaleTemplate(parseTemplate(template), factor)
                } ?: templateIngredient.text

                IngredientsListIngredientsList(
                    amount = templateIngredient.amount,
                    ingredientID = templateIngredient.ingredientID,
                    name = templateIngredient.name,
                    optional = templateIngredient.optional,
                    prefix = templateIngredient.prefix,
                    suffix = templateIngredient.suffix,
                    text = scaledText,
                    unit = templateIngredient.unit
                )
            },
            recipeSection = ingredientSection.recipeSection
        )
    }
    val scaledInstructions = recipe.instructionsTemplate?.map { it ->
        val description = scaleTemplate(parseTemplate(it.descriptionTemplate), factor)
        InstructionElement(
            description = description,
            images = it.images,
            stepNumber = it.stepNumber,
        )
    }

    return ClientSideRecipe(
        bookCredit = recipe.bookCredit,
        byline = recipe.byline,
        canonicalArticle = recipe.canonicalArticle,
        celebrationIDS = recipe.celebrationIDS,
        composerID = recipe.composerID,
        contributors = recipe.contributors,
        cuisineIDS = recipe.cuisineIDS,
        description = recipe.description,
        difficultyLevel = recipe.difficultyLevel,
        featuredImage = recipe.featuredImage,
        id = recipe.id,
        ingredients = scaledIngredients,
        instructions = scaledInstructions,
        isAppReady = recipe.isAppReady,
        mealTypeIDS = recipe.mealTypeIDS,
        serves = recipe.serves,
        suitableForDietIDS = recipe.suitableForDietIDS,
        techniquesUsedIDS = recipe.techniquesUsedIDS,
        timings = recipe.timings,
        title = recipe.title,
        utensilsAndApplianceIDS = recipe.utensilsAndApplianceIDS,
        webPublicationDate = recipe.webPublicationDate,
    )
}