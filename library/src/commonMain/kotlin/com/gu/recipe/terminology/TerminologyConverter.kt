package com.gu.recipe.terminology

import com.gu.recipe.generated.RecipeV3
import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.serialization.json.jsonObject


@OptIn(ExperimentalJsExport::class)
@JsExport
class TerminologyConverter(private val terminologyTable: TerminologyTable) {

private class JsonTrieNode {
        val children = mutableMapOf<Char, JsonTrieNode>()
        var word: String? = null // Stores the matching lookup keyword
    }

    /**
     * Accepts a JSON String and a terminologyTable object, executes a fast
     * text-replacement pass, and returns the updated JSON String.
     */
    fun replaceWordsInJson(inputJson: String): String {
        // 1. Validate the terminologyTable keys and values
        val validKeys = terminologyTable.keys.orEmpty().filter { key ->
            key.all { it.isLetterOrDigit() || it.isWhitespace() } // Allow only alphanumeric and spaces
        }
        val validTerminologyTable = validKeys.associateWith { key ->
            terminologyTable.convertTerm(key)?.replace(Regex("[\"\\\\]")) { "\\${it.value}" } // Escape special characters
        }

        // 2. Parse the JSON into a tree structure
        val jsonElement = Json.parseToJsonElement(inputJson)

        // 3. Replace words only in string values
        fun replaceInJsonElement(element: kotlinx.serialization.json.JsonElement): kotlinx.serialization.json.JsonElement {
            return when (element) {
                is kotlinx.serialization.json.JsonPrimitive -> {
                    if (element.isString) {
                        val content = element.content
                        val replacedContent = validTerminologyTable.entries.fold(content) { acc, (key, value) ->
                            acc.replace(key, value ?: key)
                        }
                        kotlinx.serialization.json.JsonPrimitive(replacedContent)
                    } else {
                        element
                    }
                }
                is kotlinx.serialization.json.JsonObject -> {
                    kotlinx.serialization.json.JsonObject(element.mapValues { (_, value) -> replaceInJsonElement(value) })
                }
                is kotlinx.serialization.json.JsonArray -> {
                    kotlinx.serialization.json.JsonArray(element.map { replaceInJsonElement(it) })
                }
            }
        }

        val modifiedJsonElement = replaceInJsonElement(jsonElement)

        // 4. Return the modified JSON as a string
        return Json.encodeToString(modifiedJsonElement)
    }

    fun replaceWordsInRecipeObject(recipe: RecipeV3): RecipeV3 {
        //println("---replaceWordsInRecipeObject called with recipe: $recipe")

        // 1. Configure Json parser (IgnoreUnknownKeys helps keep things fast/flexible)
        val jsonFormat = Json { ignoreUnknownKeys = true }

        // 2. Turn the Recipe data object into a raw JSON string
        val originalJsonString = jsonFormat.encodeToString(recipe)

        // 3. Run the fast Trie replacer we built earlier
        val modifiedJsonString = replaceWordsInJson(originalJsonString)

        // 4. Parse the modified JSON string back into a structural Recipe object
        return jsonFormat.decodeFromString<RecipeV3>(modifiedJsonString)
    }
}

/**
* Loads terminologies from S3 bucket.
* Returns the TerminologyConverter to further use the converting terminologies methods.
*/
fun setUpTerminologyTable(rawTerminologyData: String? = null):Result<TerminologyConverter> {
    val terminologyTable =
        (if (rawTerminologyData != null) loadTerminologyTable(rawTerminologyData) else loadInternalTerminologyTable()).map { it }
    return terminologyTable.map { TerminologyConverter(it) }
}