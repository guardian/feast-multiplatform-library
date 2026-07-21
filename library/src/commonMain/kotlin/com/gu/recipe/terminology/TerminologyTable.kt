package com.gu.recipe.terminology

import kotlinx.serialization.*
import kotlinx.serialization.json.*
//import com.gu.recipe.generated.TerminologyFixture
import com.gu.recipe.generated.internalTerminologyData

@Serializable
data class TerminologySchema constructor(
    @SerialName("prepared_at") val preparedAt: String,
    val key: List<String>,
    val values: List<List<JsonElement>>
)


@Serializable
data class TerminologyEntry(val id: Int, val ukTerm: String, val usTerm: String, val block: List<String>)
class TerminologyTable(
    val terminologyMap: Map<String, TerminologyEntry>
) {
    // Expose the keys so the Trie knows what words to build structures for
    private val replacementMap = terminologyMap.mapKeys { (key, _) -> key.lowercase() }
    private val replacementRegex = terminologyMap.keys
        .sortedByDescending { it.length }
        .takeIf { it.isNotEmpty() }
        ?.joinToString(separator = "|", prefix = "\\b(?:", postfix = ")\\b") { Regex.escape(it) }
        ?.let { Regex(it, RegexOption.IGNORE_CASE) }

    private fun extractLocalContext(text: String, range: IntRange): String {
        val contextRange = 20 //We can adjust the context range if needed. it is working good so far with 20 characters before and after the match
        val matchStart = range.first
        val matchEnd = range.last
        val contextStart = maxOf(0, matchStart - contextRange)
        val contextEnd = minOf(text.length, matchEnd + contextRange)
        return text.substring(contextStart, contextEnd)
    }

    internal fun convertTerm(text: String?): String? {
        if (replacementRegex == null) return text
        val regex = replacementRegex

        return text?.replace(regex) { match ->
            val replacementEntry = replacementMap[match.value.lowercase()]
            if (replacementEntry != null) {
                // Extract the local context around the match
                val localContext = extractLocalContext(text, match.range)

                // Check if the match is part of any blocked phrase in the local context
                val isBlocked = replacementEntry.block.any { blockWord ->
                    Regex("\\b${Regex.escape(blockWord)}\\b", RegexOption.IGNORE_CASE).containsMatchIn(localContext)
                }

                if (!isBlocked) {
                    val replacement = replacementEntry.usTerm
                    val finalReplacement = if (match.value.firstOrNull()?.isUpperCase() == true) {
                        replacement.replaceFirstChar { it.uppercase() }
                    } else {
                        replacement
                    }
                    finalReplacement
                } else {
                    match.value
                }
            } else {
                match.value
            }
        }?.also { result ->
            println("Final result after replacement: $result")
        }
    }


}

fun loadInternalTerminologyTable(): Result<TerminologyTable> {
    return loadTerminologyTable(internalTerminologyData)
}

fun loadTerminologyTable(raw: String): Result<TerminologyTable> {
    return try {
        val data = Json.decodeFromString<TerminologySchema>(raw)

        val terminologyMap = data.values.associate { row ->
            val id = row[0].jsonPrimitive.int
            val ukTerm = row[1].jsonPrimitive.content
            val usTerm = row[2].jsonPrimitive.content
            val block = row[3].jsonArray.map { it.jsonPrimitive.content }
            ukTerm to TerminologyEntry(id = id, ukTerm = ukTerm, usTerm = usTerm, block = block)
        }

        val table = TerminologyTable(terminologyMap)
        Result.success(table)

    } catch (e: SerializationException) {
        Result.failure(e)
    } catch (e: IllegalArgumentException) {
        Result.failure(Exception("Terminology fixture was valid JSON in an unknown shape"))
    } catch (e: ClassCastException) {
        Result.failure(Exception("There was an invalid data type in the terminology fixture"))
    } catch (e: IndexOutOfBoundsException) {
        Result.failure(Exception("There was a short row in the terminology fixture"))
    }
}