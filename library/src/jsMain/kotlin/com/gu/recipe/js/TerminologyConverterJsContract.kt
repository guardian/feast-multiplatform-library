package com.gu.recipe.js

import com.gu.recipe.generated.RecipeV3
//import com.gu.recipe.loadTerminologyTable
//import com.gu.recipe.replaceWordsInRecipeObject
import com.gu.recipe.terminology.TerminologyConverter
//import com.gu.recipe.terminology.TerminologyTable
import com.gu.recipe.terminology.setUpTerminologyTable
import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

private val tolerantJson = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalJsExport::class)
    @JsExport
    /**
     * Sets up a TerminologyConverter using the provided raw terminology data.
     * If the data is null, it uses the internal terminology table.
     */
    fun createTerminologyConverter(rawTerminologyData: String?): TerminologyConverter {
        return setUpTerminologyTable(rawTerminologyData).getOrThrow()
    }

    @OptIn(ExperimentalJsExport::class)
    @JsExport
    /**
     * Converts terminologies in the given JSON recipe string using the provided TerminologyConverter.
     * Returns the converted recipe as a JSON string.
     */
    fun convertTerminologiesInRecipe(scaledRecipe: RecipeV3, terminologyConverter: TerminologyConverter): String {
        val convertedRecipe = terminologyConverter.replaceWordsInRecipeObject(scaledRecipe)
        return tolerantJson.encodeToString(convertedRecipe)
    }

