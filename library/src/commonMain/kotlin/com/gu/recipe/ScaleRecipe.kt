package com.gu.recipe

import kotlinx.serialization.json.*
import kotlinx.serialization.Serializable
import com.gu.recipe.generated.RecipeV2
import com.gu.recipe.generated.RecipeV3
import com.gu.recipe.generated.StringTemplate
import com.gu.recipe.generated.IngredientElement
import com.gu.recipe.generated.IngredientsListIngredientsList
import com.gu.recipe.generated.InstructionElement

sealed interface TemplateElement {
    data class TemplateConst(
        val value: String
    ) : TemplateElement

    @Serializable
    data class QuantityPlaceholder(
        val min: Float,
        val max: Float? = null,
        val unit: String? = null,
        val scale: Boolean = false,
    ) : TemplateElement

    @Serializable
    data class OvenTemperaturePlaceholder(
        val temperatureC: Int,
        val temperatureFanC: Int? = null,
        val temperatureF: Int? = null,
        val gasMark: Float? = null,
    ) : TemplateElement
}

data class ParsedTemplate(
    val elements: List<TemplateElement>
)

fun parseTemplate(template: StringTemplate): ParsedTemplate {
    val pattern = Regex("""\{(?:[^{}"]|"(?:[^"\\\\]|\\\\.)*")*\}""")
    val parts = mutableListOf<TemplateElement>()
    var lastEnd = 0

    pattern.findAll(template).forEach { match ->
        // Add text before JSON
        if (match.range.first > lastEnd) {
            val textPart = template.substring(lastEnd, match.range.first)
            if (textPart.isNotBlank()) {
                parts.add(TemplateElement.TemplateConst(textPart))
            }
        }

        // Try to parse JSON
        try {
            val jsonObj = Json.parseToJsonElement(match.value).jsonObject
            if (jsonObj.keys.contains("min")) {
                Json.decodeFromJsonElement<TemplateElement.QuantityPlaceholder>(jsonObj).let {
                    parts.add(it)
                }
            } else {
                Json.decodeFromJsonElement<TemplateElement.OvenTemperaturePlaceholder>(jsonObj).let {
                    parts.add(it)
                }
            }
        } catch (e: Exception) {
            print("Failed to parse JSON: ${match.value}, error: ${e.message}")
            parts.add(TemplateElement.TemplateConst(match.value))
        }

        lastEnd = match.range.last + 1
    }

    // Add remaining text
    if (lastEnd < template.length) {
        val remaining = template.substring(lastEnd)
        if (remaining.isNotBlank()) {
            parts.add(TemplateElement.TemplateConst(remaining))
        }
    }

    return ParsedTemplate(parts)
}

fun scaleTemplate(template: ParsedTemplate, factor: Float): String {
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
typealias SeverSideRecipe = RecipeV3
fun scaleRecipe(recipe: SeverSideRecipe, factor: Float): ClientSideRecipe {
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
        bookCredit= recipe.bookCredit,
        byline= recipe.byline,
        canonicalArticle= recipe.canonicalArticle,
        celebrationIDS= recipe.celebrationIDS,
        composerID= recipe.composerID,
        contributors= recipe.contributors,
        cuisineIDS= recipe.cuisineIDS,
        description= recipe.description,
        difficultyLevel= recipe.difficultyLevel,
        featuredImage= recipe.featuredImage,
        id= recipe.id,
        ingredients= scaledIngredients,
        instructions= scaledInstructions,
        isAppReady= recipe.isAppReady,
        mealTypeIDS= recipe.mealTypeIDS,
        serves= recipe.serves,
        suitableForDietIDS= recipe.suitableForDietIDS,
        techniquesUsedIDS= recipe.techniquesUsedIDS,
        timings= recipe.timings,
        title= recipe.title,
        utensilsAndApplianceIDS= recipe.utensilsAndApplianceIDS,
        webPublicationDate= recipe.webPublicationDate,
    )
}