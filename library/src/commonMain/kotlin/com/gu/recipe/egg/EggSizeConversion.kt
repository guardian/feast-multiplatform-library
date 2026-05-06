package com.gu.recipe.egg

/**
 * Egg sizes used in recipe authoring (based on UK/EU standard).
 */
enum class EggSize(val labels: List<String>) {
    SMALL(listOf("small")),
    MEDIUM(listOf("medium")),
    LARGE(listOf("large")),
    EXTRA_LARGE(listOf("extra large", "extra-large", "xl")),
    JUMBO(listOf("jumbo"));

    val displayLabel: String get() = labels.first()

    companion object {
        fun fromLabel(label: String): EggSize? {
            val lower = label.lowercase().trim()
            return entries.firstOrNull { size -> size.labels.any { it == lower } }
        }
    }
}

/**
 * Target regions for egg size conversion. Extensible to AU, NZ etc.
 */
enum class EggRegion {
    US
}

/**
 * Mapping from UK egg sizes to regional equivalents.
 */
private val REGION_MAPPINGS: Map<EggRegion, Map<EggSize, EggSize>> = mapOf(
    EggRegion.US to mapOf(
        EggSize.SMALL to EggSize.SMALL,
        EggSize.MEDIUM to EggSize.LARGE,
        EggSize.LARGE to EggSize.EXTRA_LARGE,
        EggSize.EXTRA_LARGE to EggSize.JUMBO,
    )
)

/**
 * Convert a UK egg size label to the equivalent label for the target region.
 * Returns the converted label, or null if the input doesn't match a known UK size.
 */
fun convertEggSizeLabel(ukLabel: String, region: EggRegion): String? {
    val ukSize = EggSize.fromLabel(ukLabel) ?: return null
    return REGION_MAPPINGS[region]?.get(ukSize)?.displayLabel
}

/**
 * Regex matching a size word adjacent to "egg":
 * - before: "large eggs", "medium egg whites"
 * - after comma: "eggs, large"
 */
private val EGG_SIZE_REGEX = Regex(
    """(?i)\b(extra[ -]large|xl|small|medium|large)\b(?=\s+egg)|(?<=eggs?,\s)(extra[ -]large|xl|small|medium|large)\b"""
)

/**
 * Post-process rendered template text to replace UK egg size words with regional equivalents.
 * Safe to call on any text — only modifies size words adjacent to "egg".
 */
fun convertEggSizesInText(text: String, region: EggRegion): String {
    return EGG_SIZE_REGEX.replace(text) { match ->
        val ukLabel = match.groupValues[1].ifEmpty { match.groupValues[2] }
        convertEggSizeLabel(ukLabel, region) ?: match.value
    }
}