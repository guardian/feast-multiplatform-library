package com.gu.recipe.terminology

import kotlinx.serialization.*
import kotlinx.serialization.json.*
//import com.gu.recipe.generated.TerminologyFixture
import com.gu.recipe.generated.internalTerminologyData

@Serializable
data class TerminologySchema(
    @SerialName("prepared_at") val preparedAt: String,
    val key: List<String>,
    val values: List<List<JsonElement>>
)


@Serializable
data class TerminologyEntry(val id: Int, val ukTerm: String, val usTerm: String, val block: List<String>)

/**
 * Converts UK terminology to US terminology.
 *
 * Each [TerminologyEntry.block] value is treated as a protected phrase span: a matched UK term is
 * not replaced only when that exact match sits inside one of its blocked phrases. For example,
 * `pepper` may be replaced generally, while the `pepper` in `red pepper` remains unchanged.
 */
class TerminologyTable(
    val terminologyMap: Map<String, TerminologyEntry>
) {
    private val replacementMap = terminologyMap.mapKeys { (key, _) -> key.lowercase() }
    private val replacementRegex = terminologyMap.keys
        .sortedByDescending { it.length }
        .takeIf { it.isNotEmpty() }
        ?.joinToString(separator = "|", prefix = "\\b(?:", postfix = ")\\b") { Regex.escape(it) }
        ?.let { Regex(it, RegexOption.IGNORE_CASE) }

    /**
     * Returns true when [index] is at the start/end of [text], or where a word character and a
     * non-word character meet, using letters and digits as word characters.
     */
    private fun hasWordBoundary(text: String, index: Int): Boolean {
        return index == 0 || index == text.length || text[index - 1].isLetterOrDigit() != text[index].isLetterOrDigit()
    }

    /**
     * Finds every whole-phrase occurrence of [blockPhrase] in [text].
     *
     * Matching is case-insensitive and boundary-aware, so a block phrase such as `red pepper` does
     * not match the substring `red pepper` inside `tired pepper`.
     */
    private fun findBlockedPhraseRanges(text: String, blockPhrase: String): List<IntRange> {
        if (blockPhrase.isEmpty()) return emptyList()

        val ranges = mutableListOf<IntRange>()
        var blockStart = text.indexOf(blockPhrase, ignoreCase = true)
        while (blockStart >= 0) {
            val blockEndExclusive = blockStart + blockPhrase.length
            if (hasWordBoundary(text, blockStart) && hasWordBoundary(text, blockEndExclusive)) {
                ranges += blockStart..<blockEndExclusive
            }
            blockStart = text.indexOf(blockPhrase, startIndex = blockStart + 1, ignoreCase = true)
        }
        return ranges
    }

    /**
     * Finds all protected phrase ranges for a terminology [entry] within one input [text].
     *
     * The result is computed lazily per matched term during [convertTerm] and cached only for that
     * conversion call, avoiding long-lived block regex/range state in singleton instances.
     */
    private fun findBlockedRanges(text: String, entry: TerminologyEntry): List<IntRange> {
        return entry.block.flatMap { blockPhrase ->
            findBlockedPhraseRanges(text, blockPhrase)
        }
    }

    /**
     * Returns true when [termRange] is fully contained inside any protected blocked phrase range.
     */
    private fun isBlocked(blockedRanges: List<IntRange>?, termRange: IntRange): Boolean {
        return blockedRanges.orEmpty().any { blockRange ->
            termRange.first >= blockRange.first && termRange.last <= blockRange.last
        }
    }

    /**
     * Returns the US replacement, preserving uppercase first-letter style from [matchValue].
     */
    private fun replacementFor(matchValue: String, entry: TerminologyEntry): String {
        val replacement = entry.usTerm
        return if (matchValue.firstOrNull()?.isUpperCase() == true) {
            replacement.replaceFirstChar { it.uppercase() }
        } else {
            replacement
        }
    }

    /**
     * Converts terminology in [text], returning null when [text] is null.
     *
     * Blocked phrase ranges are cached per matched term for this invocation only. This avoids
     * repeated scans for the same term in long strings while keeping singleton memory usage low.
     */
    internal fun convertTerm(text: String?): String? {
        val source = text ?: return null
        val regex = replacementRegex ?: return source
        val blockedRangesByTerm = mutableMapOf<String, List<IntRange>>()

        return regex.replace(source) { match ->
            val matchedTerm = match.value.lowercase()
            val replacementEntry = replacementMap[matchedTerm]
            if (replacementEntry != null) {
                val blockedRanges = blockedRangesByTerm.getOrPut(matchedTerm) {
                    findBlockedRanges(source, replacementEntry)
                }
                if (!isBlocked(blockedRanges, match.range)) {
                    replacementFor(match.value, replacementEntry)
                } else {
                    match.value
                }
            } else {
                match.value
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