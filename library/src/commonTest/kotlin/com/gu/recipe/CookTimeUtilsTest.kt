package com.gu.recipe

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CookTimeUtilsTest {

    private val utils = CookTimeUtils()

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
    fun `total time overrides prep and cook`() {
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

        assertEquals("45 min + chill 30 min", result)
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
    fun `long duration stays in hr format`() {
        val input = listOf(
            timing("prep-time", 4320), // 3 days
            timing("cook-time", 60)
        )

        val result = utils.format(input)

        assertEquals("73 hr", result)
    }

    @Test
    fun `primary time crossing twenty four hours stays in hr format`() {
        val input = listOf(
            timing("prep-time", 900),  // 15 hr
            timing("cook-time", 600)   // 10 hr
        )

        val result = utils.format(input)

        assertEquals("25 hr", result)
    }

    @Test
    fun `single value displays minutes`() {
        val input = listOf(timing("prep-time", 10))
        assertEquals("10 min", utils.format(input))
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
    fun `hours do not pluralise`() {
        val input = listOf(timing("cook-time", 120))
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
        assertEquals("1 hr + marinate 2 hr + chill 30 min", utils.format(input))
    }

    @Test
    fun `fractional passive day uses unicode fraction`() {
        val input = listOf(
            timing("prep-time", 80),
            timing("marinate-time", 720)
        )
        assertEquals("1 hr 20 min + marinate ½ day", utils.format(input))
    }

    @Test
    fun `fractional passive day supports quarter and three quarters`() {
        val quarter = listOf(
            timing("prep-time", 30),
            timing("rest-time", 360)
        )
        val threeQuarters = listOf(
            timing("prep-time", 30),
            timing("prove-time", 1080)
        )

        assertEquals("30 min + rest ¼ day", utils.format(quarter))
        assertEquals("30 min + prove ¾ day", utils.format(threeQuarters))
    }

    @Test
    fun `unknown passive qualifier follows passive pattern`() {
        val input = listOf(
            timing("prep-time", 30),
            timing("ferment-time", 180)
        )
        assertEquals("30 min + ferment 3 hr", utils.format(input))
    }

    @Test
    fun `combined minutes above fifty nine convert to hour minute format`() {
        val input = listOf(
            timing("prep-time", 30),
            timing("cook-time", 40)
        )

        assertEquals("1 hr 10 min", utils.format(input))
    }

    @Test
    fun `decimal minute values round and do not display decimals`() {
        val input = listOf(
            CookTimeUtils.RecipeTiming(
                qualifier = "total-time",
                durationInMins = CookTimeUtils.DurationRange(min = 89.6, max = null)
            )
        )

        assertEquals("1 hr 30 min", utils.format(input))
    }

    @Test
    fun `fractional hour source values convert to hr and min`() {
        val input = listOf(
            CookTimeUtils.RecipeTiming(
                qualifier = "total-time",
                durationInMins = CookTimeUtils.DurationRange(min = 90.0, max = 90.0)
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
    fun `cook only wins over total fallback`() {
        val input = listOf(
            timing("cook-time", 35),
            timing("total-time", 90)
        )

        assertEquals("35 min", utils.format(input))
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
            CookTimeUtils.RecipeTiming(
                qualifier = "unknown-time",
                durationInMins = CookTimeUtils.DurationRange(min = 30.0, max = 30.0)
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
            CookTimeUtils.RecipeTiming(
                qualifier = "prep-time",
                durationInMins = CookTimeUtils.DurationRange(min = null, max = null)
            ),
            CookTimeUtils.RecipeTiming(
                qualifier = "cook-time",
                durationInMins = CookTimeUtils.DurationRange(min = 0.0, max = 0.0)
            ),
            CookTimeUtils.RecipeTiming(
                qualifier = "total-time",
                durationInMins = CookTimeUtils.DurationRange(min = -10.0, max = null)
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
        assertEquals(10, structured.primary[0].duration.minutes)
        assertEquals("cook", structured.primary[1].label)
        assertEquals(20, structured.primary[1].duration.minutes)
        assertEquals(1, structured.secondary.size)
        assertEquals("chill", structured.secondary[0].label)
        assertEquals(30, structured.secondary[0].duration.minutes)
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
        assertEquals(90, structured.fallback?.duration?.minutes)
        assertEquals(0, structured.secondary.size)
    }

    @Test
    fun `structured individual uses ready in fallback when no prep cook and no total`() {
        val structured = utils.structuredIndividual(listOf(timing("ready-in-time", 45)))

        assertEquals(0, structured.primary.size)
        assertEquals("ready-in", structured.fallback?.label)
        assertEquals(45, structured.fallback?.duration?.minutes)
        assertEquals(0, structured.secondary.size)
    }

    private fun timing(
        qualifier: String,
        min: Int,
        max: Int = min
    ) = CookTimeUtils.RecipeTiming(
        qualifier = qualifier,
        durationInMins = CookTimeUtils.DurationRange(
            min = min.toDouble(),
            max = max.toDouble()
        )
    )
}
