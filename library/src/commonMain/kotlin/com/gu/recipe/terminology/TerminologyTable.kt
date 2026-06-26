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
data class TerminologyEntry(val id: Int, val ukTerm: String, val usTerm: String)

class TerminologyTable(
    val preparedAt: String,
    private val terminologyMap: Map<String, String>
) {
    // Expose the keys so the Trie knows what words to build structures for
    val keys: Set<String> get() = terminologyMap.keys
    fun convertTerm(term: String): String? {
        return terminologyMap[term]
    }
}

fun loadInternalTerminologyTable(): Result<TerminologyTable> {
    println("---loadInternalTerminologyTable")
    return loadTerminologyTable(internalTerminologyData)
}

fun loadTerminologyTable(raw: String): Result<TerminologyTable> {
    return try {
        println("Raw input: $raw")
        val data = Json.decodeFromString<TerminologySchema>(raw)
        println("Decoded data: $data")

        val terminologyMap = data.values.associate { row ->
            val ukTerm = row[1].jsonPrimitive.content
            val usTerm = row[2].jsonPrimitive.content
            ukTerm to usTerm
        }

        val table = TerminologyTable(data.preparedAt, terminologyMap)
        println("Created TerminologyTable: $table")
        Result.success(table)
    } catch (e: SerializationException) {
        println("SerializationException: ${e.message}")
        Result.failure(e)
    } catch (e: IllegalArgumentException) {
        println("IllegalArgumentException: ${e.message}")
        Result.failure(Exception("Terminology fixture was valid JSON in an unknown shape"))
    } catch (e: ClassCastException) {
        println("ClassCastException: ${e.message}")
        Result.failure(Exception("There was an invalid data type in the terminology fixture"))
    } catch (e: IndexOutOfBoundsException) {
        println("IndexOutOfBoundsException: ${e.message}")
        Result.failure(Exception("There was a short row in the terminology fixture"))
    }
}