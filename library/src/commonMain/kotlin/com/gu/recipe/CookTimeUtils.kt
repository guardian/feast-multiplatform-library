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
 * Output uses fixed `hr`/`min` units (never pluralised) and `days` (always plural, since threshold ≥ 4).
 * Durations ≥ 5760 min (4 days / 96 hr) are converted to the `day` unit with
 * Unicode fractions (¼ ½ ¾) for quarter-day remainders.
 *
 * When min ≠ max, durations are formatted as ranges: `"20 - 30 min"`.
 *
 * Two display modes:
 * - `format()` — concatenated: primary duration + passive labels only (no passive values).
 * - `formatToItems()` — individually formatted: each entry with full label and duration (including ranges and day fractions).
 *
 * `structured()` returns the combined display model.
 * `structuredIndividual()` returns individual prep/cook entries without combining.
 */
object CookTimeUtils {
    private const val MINUTES_PER_HOUR = 60
    private const val MINUTES_PER_DAY = 1440
    private const val MINUTES_PER_QUARTER_DAY = 360
    // 4 days (96 hr) — output switches to the "days" unit only above this value (exclusive).
    private const val DAY_THRESHOLD = 5760
    private val PRIMARY_QUALIFIERS = setOf("prep-time", "cook-time", "prep", "cook")
    private val PREP_QUALIFIERS = setOf("prep-time", "prep")
    private val TOTAL_QUALIFIERS = setOf("total-time")
    private val READY_IN_QUALIFIERS = setOf("ready-in", "ready-in-time")

    /* ----------------------------- */
    /* DOMAIN TYPES                  */
    /* ----------------------------- */

    private data class ParsedTiming(
        val qualifier: String,
        val passiveLabel: String?,
        val duration: CookDurationRange
    )

    /**
     * A duration that may represent a range (min–max) or a fixed value (min == max).
     */
    data class CookDurationRange(val minMinutes: Int, val maxMinutes: Int) {
        /** `true` when the duration represents an exact value, not a range. */
        val isFixed: Boolean get() = minMinutes == maxMinutes

        fun format(): String {
            if (isFixed) return formatMinutes(minMinutes)
            val minText = formatMinutes(minMinutes)
            val maxText = formatMinutes(maxMinutes)
            // When both ends resolve to the same simple unit, strip the unit from the
            // first value so the range reads e.g. "20 - 30 min" instead of "20 min - 30 min".
            val minUnit = unitOf(minMinutes)
            val maxUnit = unitOf(maxMinutes)
            if (minUnit != null && minUnit == maxUnit) {
                return "${minText.removeSuffix(" $minUnit").trimEnd()} - $maxText"
            }
            return "$minText - $maxText"
        }
    }

    /**
     * Combined display model containing primary cook time and passive secondary timings.
     */
    data class CookTimeInfo(
        val primary: CookDurationRange?,
        val secondary: List<Pair<String, CookDurationRange>>
    )

    /**
     * A labeled [CookDurationRange] entry used for a single timing.
     */
    data class LabeledCookDuration(
        val label: String,
        val duration: CookDurationRange,
        val isPassive: Boolean = false
    ) {
        /** Label with first character uppercased, e.g. "prep" → "Prep". */
        val capitalizedLabel: String get() = label.replaceFirstChar { it.uppercaseChar() }
    }

    /**
     * Uncombined timing model with primary entries, fallback entry, and passive entries.
     * Useful when apps need to render individual timing entries while preserving formatter output (i.e. in recipe
     * page).
     *
     * [all] contains primary and secondary entries in the original input order.
     */
    data class StructuredIndividualInfo(
        val primary: List<LabeledCookDuration>,
        val fallback: LabeledCookDuration?,
        val secondary: List<LabeledCookDuration>,
        val all: List<LabeledCookDuration> = emptyList()
    )

    /* ----------------------------- */
    /* PUBLIC API                    */
    /* ----------------------------- */

    /**
     * Formats a list of [Timing] entries into a human-readable cook-time display string (concatenated mode).
     *
     * The primary duration is resolved by priority: prep + cook (combined) → total-time → ready-in.
     * When min ≠ max, the primary is formatted as a range (e.g. `"20 - 30 min"`).
     * Passive timings (e.g. chill, marinate) are appended as label-only with " + " when a prep/cook primary exists.
     *
     * @param timings the raw timing metadata from a recipe.
     * @return the formatted cook-time string, or `null` if no displayable primary timing is available.
     */
    fun format(timings: List<Timing>): String? {
        val info = structured(timings)

        val primary = info.primary ?: return null
        val primaryString = primary.format()

        if (info.secondary.isEmpty()) return primaryString

        return "$primaryString + ${info.secondary.joinToString(" + ") { it.first }}"
    }

    /**
     * Formats a list of [Timing] entries into individual timing items, each represented as a map
     * (individually formatted mode).
     *
     * Each timing entry is kept separate (prep, cook, passive) rather than being combined.
     * The original ordering of the input timings is preserved.
     * When only a total-time or ready-in fallback is available, only the fallback entry is
     * returned and passive timings are omitted.
     *
     * Each map is a single entry where the key is the capitalised label and the value
     * is the formatted duration, e.g. `mapOf("Prep" to "20 min")`.
     * Ranges are formatted as `"20 - 30 min"`.
     * Durations ≥ 4 days use fractional-day notation (e.g. `"4½ days"`).
     *
     * @param timings the raw timing metadata from a recipe.
     * @return a list of single-entry maps, or an empty list if no displayable timings are available.
     */
    fun formatToItems(timings: List<Timing>): List<Map<String, String>> {
        val individual = structuredIndividual(timings)

        val items = mutableListOf<Map<String, String>>()

        if (individual.primary.isNotEmpty()) {
            // Walk all entries in original order, emitting primary and secondary items.
            individual.all.forEach { entry ->
                items.add(mapOf(entry.capitalizedLabel to entry.duration.format()))
            }
        } else if (individual.fallback != null) {
            items.add(mapOf(individual.fallback.capitalizedLabel to individual.fallback.duration.format()))
        }

        return items
    }

    /**
     * Builds a [CookTimeInfo] display model from raw [Timing] entries.
     *
     * Primary duration is resolved by combining prep + cook times when available,
     * falling back to total-time or ready-in otherwise.
     * When combining, min values are summed and max values are summed independently to preserve range semantics.
     * Passive timings (e.g. chill, marinate) are included as secondary entries only when
     * a prep/cook primary exists.
     *
     * @param timings the raw timing metadata from a recipe.
     * @return a [CookTimeInfo] containing the combined primary duration and any secondary passive timings.
     */
    fun structured(timings: List<Timing>): CookTimeInfo {
        val individual = structuredIndividual(timings)
        val combinedPrimary: CookDurationRange? = when {
            individual.primary.isNotEmpty() -> CookDurationRange(
                minMinutes = individual.primary.sumOf { it.duration.minMinutes },
                maxMinutes = individual.primary.sumOf { it.duration.maxMinutes }
            )
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

    private fun Range.toCookDurationRange(): CookDurationRange? {
        val minVal = (min ?: max)?.roundToInt() ?: return null
        val maxVal = (max ?: min)?.roundToInt() ?: return null
        if (minVal <= 0 && maxVal <= 0) return null
        // Normalise so minMinutes is always ≤ maxMinutes regardless of source ordering.
        return CookDurationRange(minMinutes = minOf(minVal, maxVal), maxMinutes = maxOf(minVal, maxVal))
    }

    private fun Timing.toParsedTiming(): ParsedTiming? {
        val duration = durationInMins?.toCookDurationRange() ?: return null
        val qualifierValue = qualifier ?: return null
        val isPrimary = qualifierValue in PRIMARY_QUALIFIERS
        val isFallback = qualifierValue in TOTAL_QUALIFIERS || qualifierValue in READY_IN_QUALIFIERS
        val passiveLabel = qualifierValue.toPassiveLabelOrNull()
        if (!isPrimary && !isFallback && passiveLabel == null) return null

        return ParsedTiming(
            qualifier = qualifierValue,
            passiveLabel = passiveLabel,
            duration = duration
        )
    }

    private fun String.toPassiveLabelOrNull(): String? {
        if (this in PRIMARY_QUALIFIERS || this in TOTAL_QUALIFIERS || this in READY_IN_QUALIFIERS) {
            return null
        }
        // Qualifiers may omit the "-time" suffix (e.g. "ferment"), so treat any unknown qualifier as passive.
        return removeSuffix("-time").lowercase()
    }

    /**
     * Returns the single display unit a minute value would format into,
     * or `null` when the output is compound (e.g. "1 hr 30 min").
     * Used by [CookDurationRange.format] to decide whether a range can be consolidated.
     */
    private fun unitOf(minutes: Int): String? = when {
        minutes > DAY_THRESHOLD && isDayFormattable(minutes) -> "days"
        minutes < MINUTES_PER_HOUR -> "min"
        minutes % MINUTES_PER_HOUR == 0 -> "hr"
        else -> null // compound "N hr M min"
    }

    /** `true` when the value formats as whole or fractional days (not hr/min fallback). */
    private fun isDayFormattable(minutes: Int): Boolean {
        val remainder = minutes % MINUTES_PER_DAY
        return remainder == 0 || remainder.toQuarterDayFraction() != null
    }

    /**
     * Formats a minute value into `hr`/`min`/`day` text.
     * > [DAY_THRESHOLD] (5760 min / 96 hr / 4 days) → days unit.
     * ≥ 60 min → hr/min unit.
     * Otherwise → min unit.
     */
    private fun formatMinutes(minutes: Int): String {
        if (minutes > DAY_THRESHOLD) {
            val days = minutes / MINUTES_PER_DAY
            val remainder = minutes % MINUTES_PER_DAY
            if (remainder == 0) return "$days days"
            val fraction = remainder.toQuarterDayFraction()
            if (fraction != null) return "$days$fraction days"
            // Non-quarter-day remainder — fall through to hr/min formatting below.
        }
        if (minutes < MINUTES_PER_HOUR) return "$minutes min"
        val hours = minutes / MINUTES_PER_HOUR
        val remainder = minutes % MINUTES_PER_HOUR
        return if (remainder == 0) "$hours hr" else "$hours hr $remainder min"
    }

    private fun Int.toQuarterDayFraction(): String? {
        if (this % MINUTES_PER_QUARTER_DAY != 0) return null
        return when (this / MINUTES_PER_QUARTER_DAY) {
            1 -> "¼"
            2 -> "½"
            3 -> "¾"
            else -> null
        }
    }


    fun structuredIndividual(timings: List<Timing>): StructuredIndividualInfo {
        val mapped = timings.mapNotNull { it.toParsedTiming() }
        val hasPrimary = mapped.any { it.qualifier in PRIMARY_QUALIFIERS }

        // Build the ordered list of all displayable entries, tagging each as primary or passive.
        val all = if (hasPrimary) {
            mapped.mapNotNull { it.toLabeledCookDuration() }
        } else {
            emptyList()
        }

        val primary = all.filter { !it.isPassive }

        val fallback = if (!hasPrimary) {
            val fallbackTiming = mapped.firstOrNull { it.qualifier in TOTAL_QUALIFIERS }
                ?: mapped.firstOrNull { it.qualifier in READY_IN_QUALIFIERS }
            fallbackTiming?.let {
                val label = if (it.qualifier in TOTAL_QUALIFIERS) "total" else "ready-in"
                LabeledCookDuration(label = label, duration = it.duration)
            }
        } else {
            null
        }

        val secondary = all.filter { it.isPassive }

        return StructuredIndividualInfo(
            primary = primary,
            fallback = fallback,
            secondary = secondary,
            all = all
        )
    }

    /**
     * Converts a [ParsedTiming] into a [LabeledCookDuration], resolving the display label
     * and tagging passive entries. Returns `null` for fallback-only qualifiers (total / ready-in).
     */
    private fun ParsedTiming.toLabeledCookDuration(): LabeledCookDuration? {
        return when {
            qualifier in PRIMARY_QUALIFIERS -> LabeledCookDuration(
                label = if (qualifier in PREP_QUALIFIERS) "prep" else "cook",
                duration = duration
            )
            passiveLabel != null -> LabeledCookDuration(
                label = passiveLabel,
                duration = duration,
                isPassive = true
            )
            else -> null
        }
    }
}
