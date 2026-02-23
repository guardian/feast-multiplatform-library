package com.gu.recipe

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
class CookTimeUtils {
    private companion object {
        const val MINUTES_PER_HOUR = 60
        const val MINUTES_PER_DAY = 1440
        const val MINUTES_PER_QUARTER_DAY = 360
        val PRIMARY_QUALIFIERS = setOf("prep-time", "cook-time")
        val TOTAL_QUALIFIERS = setOf("total-time")
        val READY_IN_QUALIFIERS = setOf("ready-in", "ready-in-time")
        val PASSIVE_LABELS = mapOf(
            "chill-time" to "chill",
            "marinate-time" to "marinate",
            "freeze-time" to "freeze",
            "prove-time" to "prove",
            "rest-time" to "rest",
            "soak-time" to "soak",
            "set-time" to "set",
            "cool-time" to "cool"
        )
    }

    /* ----------------------------- */
    /* API MODELS                    */
    /* ----------------------------- */

    data class RecipeTiming(
        val qualifier: String,
        val durationInMins: DurationRange?
    )

    data class DurationRange(
        val min: Double?,
        val max: Double?
    )

    /* ----------------------------- */
    /* DOMAIN TYPES                  */
    /* ----------------------------- */

    private data class Timing(
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

    data class CooktimeInfo(
        val primary: CookDuration?,
        val secondary: List<Pair<String, CookDuration>>
    )

    data class LabeledCookDuration(
        val label: String,
        val duration: CookDuration
    )

    data class StructuredIndividualInfo(
        val primary: List<LabeledCookDuration>,
        val fallback: LabeledCookDuration?,
        val secondary: List<LabeledCookDuration>
    )


}
