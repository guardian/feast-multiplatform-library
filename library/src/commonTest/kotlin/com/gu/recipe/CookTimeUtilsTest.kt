package com.gu.recipe

import com.gu.recipe.generated.Range
import com.gu.recipe.generated.Timing
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CookTimeUtilsTest {

    private val utils = CookTimeUtils

    @Test
    fun `prep + cook sums correctly`() {
        val input = listOf(
            timing("prep-time", 10),
            timing("cook-time", 20)
        )

        val result = utils.format(input)

        assertEquals("30 min", result)
    }

    @Test
    fun `prep and cook take priority over total time`() {
        val input = listOf(
            timing("prep-time", 10),
            timing("cook-time", 20),
            timing("total-time", 25)
        )

        val result = utils.format(input)

        assertEquals("30 min", result)
    }

    @Test
    fun `passive timing appended correctly`() {
        val input = listOf(
            timing("prep-time", 45),
            timing("chill-time", 30)
        )

        val result = utils.format(input)

        assertEquals("45 min + chill", result)
    }

    @Test
    fun `passive only returns null`() {
        val input = listOf(
            timing("chill-time", 60)
        )

        val result = utils.format(input)

        assertNull(result)
    }

    @Test
    fun `long duration below day threshold stays in hr format`() {
        val input = listOf(
            timing("prep-time", 4320), // 72 hr (3 days, below 4-day threshold)
            timing("cook-time", 60)
        )

        val result = utils.format(input)

        assertEquals("73 hr", result)
    }

    @Test
    fun `hours and minutes displays with fixed labels`() {
        val input = listOf(
            timing("prep-time", 15),
            timing("cook-time", 90)
        )
        assertEquals("1 hr 45 min", utils.format(input))
    }

    @Test
    fun `hours only combination displays in hr`() {
        val input = listOf(
            timing("prep-time", 30),
            timing("cook-time", 90)
        )
        assertEquals("2 hr", utils.format(input))
    }

    @Test
    fun `total fallback works when no prep cook`() {
        val input = listOf(
            timing("total-time", 90),
            timing("chill-time", 30)
        )
        assertEquals("1 hr 30 min", utils.format(input))
    }

    @Test
    fun `ready in fallback works when no prep cook and no total`() {
        val input = listOf(timing("ready-in", 30))
        assertEquals("30 min", utils.format(input))
    }

    @Test
    fun `multiple passive timings append in sequence`() {
        val input = listOf(
            timing("prep-time", 20),
            timing("cook-time", 40),
            timing("marinate-time", 120),
            timing("chill-time", 30)
        )
        assertEquals("1 hr + marinate + chill", utils.format(input))
    }

    @Test
    fun `unknown passive qualifier follows passive pattern`() {
        val input = listOf(
            timing("prep-time", 30),
            timing("ferment-time", 180)
        )
        assertEquals("30 min + ferment", utils.format(input))
    }

    @Test
    fun `decimal minute values round and do not display decimals`() {
        val input = listOf(
            Timing(
                qualifier = "total-time",
                durationInMins = Range(min = 89.6, max = null)
            )
        )

        assertEquals("1 hr 30 min", utils.format(input))
    }

    @Test
    fun `prep only wins over total fallback`() {
        val input = listOf(
            timing("prep-time", 25),
            timing("total-time", 90)
        )

        assertEquals("25 min", utils.format(input))
    }

    @Test
    fun `total fallback wins over ready in fallback`() {
        val input = listOf(
            timing("total-time", 90),
            timing("ready-in", 30)
        )

        assertEquals("1 hr 30 min", utils.format(input))
    }

    @Test
    fun `fallback primary does not append passive timings`() {
        val withTotal = listOf(
            timing("total-time", 90),
            timing("chill-time", 30)
        )
        val withReadyIn = listOf(
            timing("ready-in", 30),
            timing("chill-time", 30)
        )

        assertEquals("1 hr 30 min", utils.format(withTotal))
        assertEquals("30 min", utils.format(withReadyIn))
    }

    @Test
    fun `missing valid time data returns null`() {
        val input = listOf(
            Timing(
                qualifier = "unknown-time",
                durationInMins = Range(min = 30.0, max = 30.0)
            )
        )
        assertNull(utils.format(input))
    }

    @Test
    fun `no time data returns null`() {
        assertNull(utils.format(emptyList()))
    }

    @Test
    fun `known qualifiers with no usable duration return null`() {
        val input = listOf(
            Timing(
                qualifier = "prep-time",
                durationInMins = Range(min = null, max = null)
            ),
            Timing(
                qualifier = "cook-time",
                durationInMins = Range(min = 0.0, max = 0.0)
            ),
            Timing(
                qualifier = "total-time",
                durationInMins = Range(min = -10.0, max = null)
            )
        )
        assertNull(utils.format(input))
    }

    @Test
    fun `structured individual keeps prep cook separate`() {
        val input = listOf(
            timing("prep-time", 10),
            timing("cook-time", 20),
            timing("chill-time", 30)
        )

        val structured = utils.structuredIndividual(input)

        assertEquals(2, structured.primary.size)
        assertEquals("prep", structured.primary[0].label)
        assertEquals(10, structured.primary[0].duration.minMinutes)
        assertEquals("cook", structured.primary[1].label)
        assertEquals(20, structured.primary[1].duration.minMinutes)
        assertEquals(1, structured.secondary.size)
        assertEquals("chill", structured.secondary[0].label)
        assertEquals(30, structured.secondary[0].duration.minMinutes)
        assertNull(structured.fallback)
    }

    @Test
    fun `structured individual uses total fallback when no prep cook`() {
        val structured = utils.structuredIndividual(
            listOf(
                timing("total-time", 90),
                timing("chill-time", 20)
            )
        )

        assertEquals(0, structured.primary.size)
        assertEquals("total", structured.fallback?.label)
        assertEquals(90, structured.fallback?.duration?.minMinutes)
        assertEquals(0, structured.secondary.size)
    }

    @Test
    fun `structured individual uses ready in fallback when no prep cook and no total`() {
        val structured = utils.structuredIndividual(listOf(timing("ready-in-time", 45)))

        assertEquals(0, structured.primary.size)
        assertEquals("ready-in", structured.fallback?.label)
        assertEquals(45, structured.fallback?.duration?.minMinutes)
        assertEquals(0, structured.secondary.size)
    }

    @Test
    fun `formatToItems returns prep and cook as separate items`() {
        val input = listOf(
            timing("prep-time", 20),
            timing("cook-time", 10)
        )

        val result = utils.formatToItems(input)

        assertEquals(
            listOf(
                mapOf("Prep" to "20 min"),
                mapOf("Cook" to "10 min")
            ),
            result
        )
    }

    @Test
    fun `formatToItems should maintain incoming ordering of times`() {
        val input = listOf(
            timing("prep-time", 10),
            timing("marinate-time", 30),
            timing("cook-time", 50)
        )

        val result = utils.formatToItems(input)

        assertEquals(
            listOf(
                mapOf("Prep" to "10 min"),
                mapOf("Marinate" to "30 min"),
                mapOf("Cook" to "50 min")
            ),
            result
        )
    }

    @Test
    fun `formatToItems uses fallback when no primary timings`() {
        val input = listOf(timing("total-time", 90))

        val result = utils.formatToItems(input)

        assertEquals(
            listOf(mapOf("Total" to "1 hr 30 min")),
            result
        )
    }

    @Test
    fun `formatToItems returns empty list when no displayable timings`() {
        val result = utils.formatToItems(emptyList())
        assertEquals(emptyList(), result)
    }

    @Test
    fun `formatToItems uses unicode fractions for passive day durations`() {
        val input = listOf(
            timing("prep-time", 30),
            timing("marinate-time", 6480) // 4.5 days = 4 days + 720 min (½ day)
        )

        val result = utils.formatToItems(input)

        assertEquals(
            listOf(
                mapOf("Prep" to "30 min"),
                mapOf("Marinate" to "4½ days")
            ),
            result
        )
    }

    @Test
    fun `formatToItems does not include passive when only fallback`() {
        val input = listOf(
            timing("total-time", 90),
            timing("chill-time", 30)
        )

        val result = utils.formatToItems(input)

        assertEquals(
            listOf(mapOf("Total" to "1 hr 30 min")),
            result
        )
    }

    @Test
    fun `formatToItems includes passive with qualifier missing time suffix`() {
        val input = listOf(
            Timing(qualifier = "prep-time", durationInMins = Range(min = 15.0, max = 15.0)),
            Timing(qualifier = "ferment", durationInMins = Range(min = 10080.0, max = 14400.0))
        )

        val result = utils.formatToItems(input)

        assertEquals(
            listOf(
                mapOf("Prep" to "15 min"),
                mapOf("Ferment" to "7 - 10 days")
            ),
            result
        )
    }

    @Test
    fun `formatToItems handles primary qualifier without time suffix`() {
        val input = listOf(
            Timing(qualifier = "prep", durationInMins = Range(min = 3.0, max = 3.0))
        )

        val result = utils.formatToItems(input)

        assertEquals(
            listOf(mapOf("Prep" to "3 min")),
            result
        )
    }

    /* ----------------------------- */
    /* RANGE FORMATTING              */
    /* ----------------------------- */

    @Test
    fun `format displays range when min and max differ`() {
        val input = listOf(
            Timing(qualifier = "prep-time", durationInMins = Range(min = 20.0, max = 30.0))
        )

        assertEquals("20 - 30 min", utils.format(input))
    }

    @Test
    fun `format displays mixed unit range`() {
        val input = listOf(
            Timing(qualifier = "total-time", durationInMins = Range(min = 70.0, max = 90.0))
        )

        assertEquals("1 hr 10 min - 1 hr 30 min", utils.format(input))
    }

    @Test
    fun `format combines prep and cook ranges`() {
        val input = listOf(
            Timing(qualifier = "prep-time", durationInMins = Range(min = 10.0, max = 15.0)),
            Timing(qualifier = "cook-time", durationInMins = Range(min = 20.0, max = 25.0))
        )

        assertEquals("30 - 40 min", utils.format(input))
    }

    @Test
    fun `format range with passive shows label only`() {
        val input = listOf(
            Timing(qualifier = "prep-time", durationInMins = Range(min = 20.0, max = 30.0)),
            timing("rest-time", 10)
        )

        assertEquals("20 - 30 min + rest", utils.format(input))
    }

    @Test
    fun `formatToItems displays range for individual items`() {
        val input = listOf(
            Timing(qualifier = "prep-time", durationInMins = Range(min = 20.0, max = 30.0)),
            Timing(qualifier = "cook-time", durationInMins = Range(min = 40.0, max = 50.0))
        )

        val result = utils.formatToItems(input)

        assertEquals(
            listOf(
                mapOf("Prep" to "20 - 30 min"),
                mapOf("Cook" to "40 - 50 min")
            ),
            result
        )
    }

    /* ----------------------------- */
    /* DAY CONVERSION                */
    /* ----------------------------- */

    @Test
    fun `format converts to days unit at threshold`() {
        val input = listOf(timing("total-time", 5760)) // exactly 4 days

        assertEquals("4 days", utils.format(input))
    }

    @Test
    fun `format stays in hr below day threshold`() {
        val input = listOf(timing("total-time", 5759)) // just below 4 days

        assertEquals("95 hr 59 min", utils.format(input))
    }

    @Test
    fun `format displays fractional day with unicode fraction`() {
        val input = listOf(timing("total-time", 6480)) // 4.5 days

        assertEquals("4½ days", utils.format(input))
    }

    @Test
    fun `format displays quarter day fraction`() {
        val input = listOf(timing("total-time", 6120)) // 4.25 days

        assertEquals("4¼ days", utils.format(input))
    }

    @Test
    fun `format falls back to hr for non quarter day remainder`() {
        val input = listOf(timing("total-time", 6000)) // 4 days + 240 min (not a quarter day)

        assertEquals("100 hr", utils.format(input))
    }


    @Test
    fun `formatToItems passive whole days`() {
        val input = listOf(
            timing("prep-time", 20),
            timing("soak-time", 8640) // 6 days
        )

        val result = utils.formatToItems(input)

        assertEquals(
            listOf(
                mapOf("Prep" to "20 min"),
                mapOf("Soak" to "6 days")
            ),
            result
        )
    }

    @Test
    fun `formatToItems passive below day threshold uses hr`() {
        val input = listOf(
            timing("prep-time", 30),
            timing("marinate-time", 720) // 12 hr, below 4-day threshold
        )

        val result = utils.formatToItems(input)

        assertEquals(
            listOf(
                mapOf("Prep" to "30 min"),
                mapOf("Marinate" to "12 hr")
            ),
            result
        )
    }

    @Test
    fun `formatToItems orders range as min to max even when source is swapped`() {
        val input = listOf(
            Timing(qualifier = "cook-time", durationInMins = Range(min = 30.0, max = 25.0))
        )

        val result = utils.formatToItems(input)

        assertEquals(
            listOf(mapOf("Cook" to "25 - 30 min")),
            result
        )
    }

    private fun timing(
        qualifier: String,
        min: Int,
        max: Int = min
    ) = Timing(
        qualifier = qualifier,
        durationInMins = Range(
            min = min.toDouble(),
            max = max.toDouble()
        )
    )
}
