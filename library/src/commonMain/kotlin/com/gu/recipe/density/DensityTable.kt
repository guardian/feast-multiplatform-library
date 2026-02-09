package com.gu.recipe.density

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import com.gu.recipe.generated.internalDensityData

@Serializable
data class TableSchema constructor(
    @SerialName("prepared_at") val preparedAt: String,
    val key: List<String>,
    val values: List<List<JsonElement>>
)

@Serializable
data class Ingredient(val id: Int, val name: String, val normalised_name: String, val density: Float)

class DensityTable(val preparedAt: String, private val normalised_map: Map<String, Ingredient>, private val name_map:Map<String, Ingredient>) {
    fun densityFor(name: String): Float? {
        return name_map[name]?.let { return it.density }
    }

    fun densityForNorm(name: String): Float? {
        return normalised_map[name]?.let { return it.density }
    }

    fun dataFor(name: String): Ingredient? {
        return name_map[name]?.let { return it }
    }

    fun dataForNorm(name: String): Ingredient? {
        return normalised_map[name]?.let { return it }
    }
}

fun loadInternalDensityTable(): Result<DensityTable> {
    return loadDensityTable(internalDensityData)
}

fun loadDensityTable(raw:String): Result<DensityTable> {
    try {
        val data = Json.decodeFromString<TableSchema>(raw)

        val ingredients = data.values.map {
            Ingredient(it[0].jsonPrimitive.int, //throws if the json isn't a number, caught below
                it[1].jsonPrimitive.content,
                it[2].jsonPrimitive.content,
                it[3].jsonPrimitive.float)
        }

        val normalised_map = ingredients.associate {
            it.normalised_name to it
        }

        val name_map = ingredients.associate {
            it.name to it
        }

        val table = DensityTable(data.preparedAt, normalised_map, name_map)

        return Result.success(table)
    } catch (e: SerializationException) {
        return Result.failure(e)
    } catch (e: IllegalArgumentException) {
        return Result.failure(Exception("density fixture was valid json in an unknown shape"))
    } catch(e: ClassCastException) {
        return Result.failure(Exception("there was an invalid data type in the data fixture"))
    } catch(e: IndexOutOfBoundsException) {
        return Result.failure(Exception("there was a short row in the data fixture"))
    }
}