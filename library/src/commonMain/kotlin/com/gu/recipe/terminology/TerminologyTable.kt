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

        val entries = data.values.map {
            val entry = TerminologyEntry(
                it[0].jsonPrimitive.int, // ID
                it[1].jsonPrimitive.content, // UK Term
                it[2].jsonPrimitive.content // US Term
            )
            println("Processed entry: $entry")
            entry
        }

        val terminologyMap = entries.associate { it.ukTerm to it.usTerm }
        println("Terminology map: $terminologyMap")

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