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

//commented out for now, as we changed to direct decoding to terminology table
// so we don't need this data class for the current implementation but may be for future use
//@Serializable
//data class TerminologyEntry(val id: Int, val ukTerm: String, val usTerm: String)

class TerminologyTable(
    terminologyMap: Map<String, String>
) {
    // Expose the keys so the Trie knows what words to build structures for
    private val replacementMap = terminologyMap.mapKeys { (key, _) -> key.lowercase() }
    private val replacementRegex = terminologyMap.keys
        .sortedByDescending { it.length }
        .takeIf { it.isNotEmpty() }
        ?.joinToString(separator = "|", prefix = "\\b(?:", postfix = ")\\b") { Regex.escape(it) }
        ?.let { Regex(it, RegexOption.IGNORE_CASE) }

    internal fun convertTerm(text: String?): String? {
        if (replacementRegex == null) return text
        val regex = replacementRegex

        return text?.replace(regex) { match ->
            val replacement = replacementMap[match.value.lowercase()] ?: match.value
            if (match.value.firstOrNull()?.isUpperCase() == true) {
                replacement.replaceFirstChar { it.uppercase() }
            } else {
                replacement
            }
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
            val ukTerm = row[1].jsonPrimitive.content
            val usTerm = row[2].jsonPrimitive.content
            ukTerm to usTerm
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