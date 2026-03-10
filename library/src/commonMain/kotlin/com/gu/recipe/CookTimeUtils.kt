package com.gu.recipe

import com.gu.recipe.generated.Range
import com.gu.recipe.generated.Timing
import kotlin.math.roundToInt

/**
 * Formats recipe timing metadata into Feast cook-time display strings.
 *
 * Rules are applied in priority order:
 * - primary from prep/cook (combined),
 * - otherwise total-time,
 * - otherwise ready-in,
 * - otherwise no display.
 *
 * Passive timings are appended with " + " only when a prep/cook primary exists.
 * Output uses fixed `hr`/`min` units and supports Unicode day fractions for passive quarter-day values.
 *
 * `structured()` returns the display model with combined primary timing.
 * `structuredIndividual()` returns individual prep/cook entries without combining.
 */
object CookTimeUtils {
    private const val MINUTES_PER_HOUR = 60
    private const val MINUTES_PER_DAY = 1440
    private const val MINUTES_PER_QUARTER_DAY = 360
    private val PRIMARY_QUALIFIERS = setOf("prep-time", "cook-time")
    private val TOTAL_QUALIFIERS = setOf("total-time")
    private val READY_IN_QUALIFIERS = setOf("ready-in", "ready-in-time")
    private val PASSIVE_LABELS = mapOf(
        "chill-time" to "chill",
        "marinate-time" to "marinate",
        "freeze-time" to "freeze",
        "prove-time" to "prove",
        "rest-time" to "rest",
        "soak-time" to "soak",
        "set-time" to "set",
        "cool-time" to "cool"
    )

    /* ----------------------------- */
    /* DOMAIN TYPES                  */
    /* ----------------------------- */

    private data class ParsedTiming(
        val qualifier: String,
        val passiveLabel: String?,
        val minutes: Int
    )

    data class CookDuration(val minutes: Int) {
        fun format(): String {
            if (minutes < MINUTES_PER_HOUR) return "$minutes min"
            val hours = minutes / MINUTES_PER_HOUR
            val remainder = minutes % MINUTES_PER_HOUR
            return if (remainder == 0) "$hours hr" else "$hours hr $remainder min"
        }
    }

    /**
     * Combined display model containing primary cook time and passive secondary timings.
     */
    data class CookTimeInfo(
        val primary: CookDuration?,
        val secondary: List<Pair<String, CookDuration>>
    )

    /**
     * A labeled [CookDuration] entry used for a single timing.
     */
    data class LabeledCookDuration(
        val label: String,
        val duration: CookDuration
    )

    /**
     * Uncombined timing model with primary entries, fallback entry, and passive entries.
     * Useful when apps need to render individual timing entries while preserving formatter output (i.e. in recipe
     * page).
     */
    data class StructuredIndividualInfo(
        val primary: List<LabeledCookDuration>,
        val fallback: LabeledCookDuration?,
        val secondary: List<LabeledCookDuration>
    )

    /* ----------------------------- */
    /* PUBLIC API                    */
    /* ----------------------------- */

    /**
     * Formats a list of [Timing] entries into a human-readable cook-time display string.
     *
     * The primary duration is resolved by priority: prep + cook (combined) → total-time → ready-in.
     * Passive timings (e.g. chill, marinate) are appended with " + " only when a prep/cook primary exists.
     *
     * @param timings the raw timing metadata from a recipe.
     * @param withStyle when `true`, wraps the primary duration in `<strong>` tags for styled rendering.
     * @return the formatted cook-time string, or `null` if no displayable primary timing is available.
     */
    fun format(
        timings: List<Timing>,
        withStyle: Boolean = false,
    ): String? {
        val info = structured(timings)

        val primary = info.primary ?: return null
        val primaryString = if (withStyle) {
            "<strong>${primary.format()}</strong>"
        } else {
            primary.format()
        }

        if (info.secondary.isEmpty()) return primaryString

        val secondaryString = info.secondary.joinToString(" + ") {
            "${it.first} ${formatPassiveDuration(it.second.minutes)}"
        }

        return "$primaryString + $secondaryString"
    }

    /**
     * Formats a list of [Timing] entries into individual timing items, each represented as a map.
     *
     * Each timing entry is kept separate (prep, cook, passive) rather than being combined. The returned list contains
     * primary or fallback entries first, followed by secondary (passive) entries.
     *
     * Each map is a single entry where the key is the capitalised label and the value
     * is the formatted duration, e.g. `mapOf("Prep" to "20 min")`.
     *
     * @param timings the raw timing metadata from a recipe.
     * @return a list of single-entry maps, or an empty list if no displayable timings are available.
     */
    fun formatItems(timings: List<Timing>): List<Map<String, String>> {
        val individual = structuredIndividual(timings)

        val items = mutableListOf<Map<String, String>>()

        if (individual.primary.isNotEmpty()) {
            individual.primary.forEach {
                items.add(mapOf(it.label.replaceFirstChar { c -> c.uppercaseChar() } to it.duration.format()))
            }
        } else if (individual.fallback != null) {
            items.add(mapOf(individual.fallback.label.replaceFirstChar { it.uppercaseChar() } to individual.fallback.duration.format()))
        }

        individual.secondary.forEach {
            items.add(mapOf(it.label.replaceFirstChar { c -> c.uppercaseChar() } to formatPassiveDuration(it.duration.minutes)))
        }

        return items
    }

    /**
     * Builds a [CookTimeInfo] display model from raw [Timing] entries.
     *
     * Primary duration is resolved by combining prep + cook times when available,
     * falling back to total-time or ready-in otherwise.
     * Passive timings (e.g. chill, marinate) are included as secondary entries only when
     * a prep/cook primary exists.
     *
     * @param timings the raw timing metadata from a recipe.
     * @return a [CookTimeInfo] containing the combined primary duration and any secondary passive timings.
     */
    fun structured(timings: List<Timing>): CookTimeInfo {
        val individual = structuredIndividual(timings)
        val combinedPrimary = when {
            individual.primary.isNotEmpty() -> CookDuration(individual.primary.sumOf { it.duration.minutes })
            individual.fallback != null -> individual.fallback.duration
            else -> null
        }
        val secondary = if (individual.primary.isNotEmpty()) {
            individual.secondary.map { it.label to it.duration }
        } else {
            emptyList()
        }
        return CookTimeInfo(primary = combinedPrimary, secondary = secondary)
    }

    /* ----------------------------- */
    /* MAPPING                       */
    /* ----------------------------- */

    private fun Range.toCookDuration(): CookDuration? {
        // Source may contain ranges; display uses a single value, so we prioritise min.
        val minutes = (min ?: max)?.toRoundedMinutes() ?: return null
        if (minutes <= 0) return null
        return CookDuration(minutes)
    }

    private fun Double.toRoundedMinutes(): Int = roundToInt()

    private fun Timing.toParsedTiming(): ParsedTiming? {
        val duration = durationInMins?.toCookDuration() ?: return null
        val qualifierValue = qualifier ?: return null
        val isPrimary = qualifierValue in PRIMARY_QUALIFIERS
        val isFallback = qualifierValue in TOTAL_QUALIFIERS || qualifierValue in READY_IN_QUALIFIERS
        val passiveLabel = PASSIVE_LABELS[qualifierValue] ?: qualifierValue.toPassiveLabelOrNull()
        if (!isPrimary && !isFallback && passiveLabel == null) return null

        return ParsedTiming(
            qualifier = qualifierValue,
            passiveLabel = passiveLabel,
            minutes = duration.minutes
        )
    }

    private fun String.toPassiveLabelOrNull(): String? {
        if (!endsWith("-time")) return null
        if (this in PRIMARY_QUALIFIERS || this in TOTAL_QUALIFIERS || this in READY_IN_QUALIFIERS) {
            return null
        }
        return removeSuffix("-time").lowercase()
    }

    private fun formatPassiveDuration(minutes: Int): String {
        // Spec requires Unicode vulgar fractions when passive durations are represented in days.
        val fractionText = minutes.toFractionalDayTextOrNull()
        return fractionText ?: CookDuration(minutes).format()
    }

    private fun Int.toFractionalDayTextOrNull(): String? {
        if (this !in 1..<MINUTES_PER_DAY) return null
        if (this % MINUTES_PER_QUARTER_DAY != 0) return null

        return when (this / MINUTES_PER_QUARTER_DAY) {
            1 -> "¼ day"
            2 -> "½ day"
            3 -> "¾ day"
            else -> null
        }
    }

    fun structuredIndividual(timings: List<Timing>): StructuredIndividualInfo {
        val mapped = timings.mapNotNull { it.toParsedTiming() }

        val primary = mapped
            .filter { it.qualifier in PRIMARY_QUALIFIERS }
            .map { timing ->
                val label = if (timing.qualifier == "prep-time") "prep" else "cook"
                LabeledCookDuration(label = label, duration = CookDuration(timing.minutes))
            }

        val fallback = if (primary.isEmpty()) {
            val fallbackTiming = mapped.firstOrNull { it.qualifier in TOTAL_QUALIFIERS }
                ?: mapped.firstOrNull { it.qualifier in READY_IN_QUALIFIERS }
            fallbackTiming?.let {
                val label = if (it.qualifier in TOTAL_QUALIFIERS) "total" else "ready-in"
                LabeledCookDuration(label = label, duration = CookDuration(it.minutes))
            }
        } else {
            null
        }

        val secondary = if (primary.isNotEmpty()) {
            mapped.mapNotNull { timing ->
                timing.passiveLabel?.let { label ->
                    LabeledCookDuration(label = label, duration = CookDuration(timing.minutes))
                }
            }
        } else {
            emptyList()
        }

        return StructuredIndividualInfo(
            primary = primary,
            fallback = fallback,
            secondary = secondary
        )
    }
}
