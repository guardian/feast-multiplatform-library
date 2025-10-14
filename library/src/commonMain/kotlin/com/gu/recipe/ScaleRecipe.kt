package com.gu.recipe

import com.gu.recipe.generated.*
import com.gu.recipe.template.ParsedTemplate
import com.gu.recipe.template.TemplateElement
import com.gu.recipe.template.parseTemplate

sealed interface IngredientUnit {
    object Imperial : IngredientUnit
    object Metric : IngredientUnit
}

private fun scaleTemplate(template: ParsedTemplate, factor: Float): String {
    val scaledParts = template.elements.map { element ->
        when (element) {
            is TemplateElement.TemplateConst -> element.value
            is TemplateElement.QuantityPlaceholder -> {
                val (scaledMin, scaledMax) = if (element.scale) {
                    val scaledMin = (element.min * factor)
                    val scaledMax = element.max?.let { (it * factor) }
                    Pair(scaledMin, scaledMax)
                } else {
                    Pair(element.min, element.max)
                }
                val unit = element.unit ?: ""
                // TODO, we'll probably need to decide if we want to round the values depending on the unit
                if (scaledMax != null) {
                    "${scaledMin.toInt()}-${scaledMax.toInt()} $unit"
                } else {
                    "${scaledMin.toInt()} $unit"
                }
            }

            is TemplateElement.OvenTemperaturePlaceholder -> {
                val tempC = element.temperatureC
                val tempFanC = element.temperatureFanC
                "${tempC}°C${tempFanC?.let { " (${it}°C fan)" } ?: ""}"
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