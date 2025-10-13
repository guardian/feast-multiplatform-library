package com.gu.recipe

import kotlinx.serialization.json.Json

@OptIn(ExperimentalJsExport::class)
@JsExport
fun scaleRecipe(recipe: String, factor: Float, unit: String): String {
    val parsedRecipe = Json.decodeFromString<SeverSideRecipe>(recipe)
    val ingredientUnit = when (unit) {
        "Imperial" -> IngredientUnit.Imperial
        "Metric" -> IngredientUnit.Metric
        else -> throw IllegalArgumentException("Unknown unit: $unit")
    }
    val scaledRecipe = scaleRecipe(parsedRecipe, factor, ingredientUnit)
    return Json.encodeToString(scaledRecipe)
}