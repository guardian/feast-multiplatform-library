package com.gu.recipe.terminology

import com.gu.recipe.generated.RecipeV3
import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


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

        // 1. Build the search Trie using the keys from terminologyTable
        val root = JsonTrieNode()
        for (word in terminologyTable.keys.orEmpty()) {
            var current = root
            for (char in word) {
                current = current.children.getOrPut(char) { JsonTrieNode() }
            }
            current.word = word
        }

        // 2. Pre-allocate StringBuilder capacity
        val result = StringBuilder(inputJson.length)
        var i = 0
        val length = inputJson.length

        // 3. Scan through the JSON string
        while (i < length) {
            var current = root
            var longestMatchLength = 0
            var bestWord: String? = null

            var j = i
            while (j < length) {
                val nextNode = current.children[inputJson[j]]
                if (nextNode != null) {
                    current = nextNode
                    j++
                    if (current.word != null) {
                        longestMatchLength = j - i
                        bestWord = current.word
                    }
                } else {
                    break
                }
            }

            // If a word matches, pass it to your terminologyTable lookup system
            if (bestWord != null) {
                // Dynamic execution triggers your class's internal println logs!
                val replacement = terminologyTable.convertTerm(bestWord) ?: bestWord
                result.append(replacement)
                i += longestMatchLength
            } else {
                // No match found, preserve the structural JSON character
                result.append(inputJson[i])
                i++
            }
        }

        return result.toString()
    }

    fun replaceWordsInRecipeObject(recipe: RecipeV3): RecipeV3 {
        println("---replaceWordsInRecipeObject called with recipe: $recipe")

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