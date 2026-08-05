package com.gu.recipe.terminology

import com.gu.recipe.generated.Highlight
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import com.gu.recipe.generated.internalTerminologyData

@Serializable
data class TerminologySchema(
    @SerialName("prepared_at") val preparedAt: String,
    val key: List<String>,
    val values: List<List<JsonElement>>
)

@Serializable
data class TerminologyEntry(
    val id: Int,
    val ukTerm: String,
    val usTerm: String,
    val block: List<String>,
    val ukGuidance: String?,
    val usGuidance: String?,
)

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
     * Finds highlights for the given text and terminology mapping.
     * Collecting "highlights" (matches) of specific terms within a given text, this is to used for underlining these terms in APP UI.
     */

    private val regexCache = mutableMapOf<String, Regex>()

    private fun findHighlights(
        text: String,
        mapping: TerminologyEntry
    ): List<Highlight> {
        val highlights = mutableListOf<Highlight>()

        val termsToCheck = listOf(mapping.usTerm)

        for (term in termsToCheck) {
            val regex = regexCache.getOrPut(term) {
                Regex("\\b${Regex.escape(term)}\\b", RegexOption.IGNORE_CASE)
            }
            regex.findAll(text).forEach { match ->
                highlights.add(
                    Highlight(
                        matchedTerm = match.value,
                        startIndex = match.range.first,
                        endIndex = match.range.last + 1, // Include the last character
                    )
                )
            }
        }
        return highlights
    }

    /**
     * Converts UK terminology to US terminology and provides extra notes and highlights for the conversion.
     *
     * Each [TerminologyEntry.block] value is treated as a protected phrase span: a matched UK term is
     * not replaced only when that exact match sits inside one of its blocked phrases. For example,
     * `pepper` may be replaced generally, while the `pepper` in `red pepper` remains unchanged.
     *
     * The [convertTerm] function returns a [ConversionResult] containing the modified string, the
     * last matched terminology entry, and any associated highlights( indexes for usTerms in the ingredeint sentence), or null if no conversion occurred.
     */
    data class ConversionResult(
        val replacedString: String,
        val terminologyEntry: TerminologyEntry?,
        val highlights: List<Highlight>? // Include highlights in the result
    )

    internal fun convertTerm(text: String?): ConversionResult? {
        val source = text ?: return null
        val regex = replacementRegex ?: return null
        val blockedRangesByTerm = mutableMapOf<String, List<IntRange>>()

        val replacements = mutableListOf<Pair<IntRange, String>>()
        var highlights: List<Highlight>? = mutableListOf() // Collect highlights
        var lastMatchedEntry: TerminologyEntry? = null

        regex.findAll(source).forEach { match ->
            val matchedTerm = match.value.lowercase()
            val replacementEntry = replacementMap[matchedTerm]
            if (replacementEntry != null) {
                val blockedRanges = blockedRangesByTerm.getOrPut(matchedTerm) {
                    findBlockedRanges(source, replacementEntry)
                }
                if (!isBlocked(blockedRanges, match.range)) {
                    val replacement = replacementFor(match.value, replacementEntry)
                    replacements.add(match.range to replacement)
                    lastMatchedEntry = replacementEntry
                }
            }
        }

        // Apply replacements in reverse order to avoid invalidating ranges
        var replacedString = source
        for ((range, replacement) in replacements.asReversed()) {
            replacedString = replacedString.replaceRange(range, replacement)
        }

        // Add highlight for the matched term - only if their associated guidance notes are available otherwise skip
        highlights = if (lastMatchedEntry != null && (lastMatchedEntry.ukGuidance?.isNotEmpty() == true || lastMatchedEntry.usGuidance?.isNotEmpty() == true)) {
            findHighlights(replacedString, lastMatchedEntry)
        } else {
            null
        }
        return ConversionResult(replacedString, lastMatchedEntry, highlights)
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
            val ukGuidance = row[4].jsonPrimitive.content
            val usGuidance = row[5].jsonPrimitive.content
            ukTerm to TerminologyEntry(id = id, ukTerm = ukTerm, usTerm = usTerm, block = block, ukGuidance = ukGuidance, usGuidance = usGuidance)
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