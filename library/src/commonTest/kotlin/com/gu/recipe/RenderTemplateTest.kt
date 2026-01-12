package com.gu.recipe

import com.gu.recipe.template.OvenTemperaturePlaceholder
import com.gu.recipe.template.ParsedTemplate
import com.gu.recipe.template.QuantityPlaceholder
import com.gu.recipe.template.TemplateConst
import com.gu.recipe.unit.MeasuringSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class RenderTemplateTest {
    @Test
    fun `scale template with simple quantity placeholder`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 100f,
                    unit = "g",
                    scale = true
                )
            )
        )
        val result = renderTemplate(template, 2f, MeasuringSystem.Metric)
        assertEquals("200 g", result)
    }

    @Test
    fun `scale template with quantity range`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 100f,
                    max = 150f,
                    unit = "g",
                    scale = true
                )
            )
        )
        val result = renderTemplate(template, 2f, MeasuringSystem.Metric)
        assertEquals("200-300 g", result)
    }

    @Test
    fun `scale template with fraction for tbsp`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 0.5f,
                    unit = "tbsp",
                    scale = true
                )
            )
        )
        val result = renderTemplate(template, 2f, MeasuringSystem.Metric)
        assertEquals("1 tbsp", result)
    }

    @Test
    fun `scale template with fraction for tsp`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 0.25f,
                    unit = "tsp",
                    scale = true
                )
            )
        )
        val result = renderTemplate(template, 2f, MeasuringSystem.Metric)
        assertEquals("½ tsp", result)
    }

    @Test
    fun `scale template with fraction for cups`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 1.5f,
                    unit = "cups",
                    scale = true
                )
            )
        )
        val result = renderTemplate(template, 0.5f, MeasuringSystem.Metric)
        assertEquals("¾ cup", result)
    }

    @Test
    fun `scale template without unit uses fractions`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 2f,
                    scale = true
                )
            )
        )
        val result = renderTemplate(template, 0.25f, MeasuringSystem.Metric)
        assertEquals("½", result)
    }

    @Test
    fun `scale template with ml uses no decimals`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 250f,
                    unit = "ml",
                    scale = true
                )
            )
        )
        val result = renderTemplate(template, 1.234567f, MeasuringSystem.Metric)
        assertEquals("309 ml", result)
    }

    @Test
    fun `scale template with non-scalable quantity`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 100f,
                    unit = "g",
                    scale = false
                )
            )
        )
        val result = renderTemplate(template, 2f, MeasuringSystem.Metric)
        assertEquals("100 g", result)
    }

    @Test
    fun `scale template with oven temperature`() {
        val template = ParsedTemplate(
            listOf(
                OvenTemperaturePlaceholder(
                    temperatureC = 180,
                    temperatureFanC = 160
                )
            )
        )
        val result = renderTemplate(template, 2f, MeasuringSystem.Metric)
        assertEquals("180C (160C fan)", result)
    }

    @Test
    fun `scale template with oven temperature without fan`() {
        val template = ParsedTemplate(
            listOf(
                OvenTemperaturePlaceholder(
                    temperatureC = 200
                )
            )
        )
        val result = renderTemplate(template, 2f, MeasuringSystem.Metric)
        assertEquals("200C", result)
    }

    @Test
    fun `scale template with const text`() {
        val template = ParsedTemplate(
            listOf(
                TemplateConst("Add ")
            )
        )
        val result = renderTemplate(template, 2f, MeasuringSystem.Metric)
        assertEquals("Add ", result)
    }

    @Test
    fun `scale template with mixed elements`() {
        val template = ParsedTemplate(
            listOf(
                TemplateConst("Add "),
                QuantityPlaceholder(
                    min = 100f,
                    max = 120f,
                    unit = "g",
                    scale = true
                ),
                TemplateConst(" of flour")
            )
        )
        val result = renderTemplate(template, 2f, MeasuringSystem.Metric)
        assertEquals("Add 200-240 g of flour", result)
    }

    @Test
    fun `scale template with complex recipe instruction`() {
        val template = ParsedTemplate(
            listOf(
                TemplateConst("Bake at "),
                OvenTemperaturePlaceholder(
                    temperatureC = 180,
                    temperatureFanC = 160
                ),
                TemplateConst(" for "),
                QuantityPlaceholder(
                    min = 30f,
                    max = 40f,
                    unit = "minutes",
                    scale = false
                ),
                TemplateConst(".")
            )
        )
        val result = renderTemplate(template, 2f, MeasuringSystem.Metric)
        assertEquals("Bake at 180C (160C fan) for 30-40 minutes.", result)
    }

    @Test
    fun `scale template with factor less than 1`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 200f,
                    unit = "g",
                    scale = true
                )
            )
        )
        val result = renderTemplate(template, 0.5f, MeasuringSystem.Metric)
        assertEquals("100 g", result)
    }

    @Test
    fun `scale template with non-standard unit uses decimals`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 1f,
                    unit = "kg",
                    scale = true
                )
            )
        )
        val result = renderTemplate(template, 1.5f, MeasuringSystem.Metric)
        assertEquals("1.5 kg", result)
    }

    @Test
    fun `scale template with quantity without unit showing fraction`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 1f,
                    scale = true
                )
            )
        )
        val result = renderTemplate(template, 0.75f, MeasuringSystem.Metric)
        assertEquals("¾", result)
    }

    @Test
    fun `scale template with range and fractions`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 0.5f,
                    max = 1f,
                    unit = "tsp",
                    scale = true
                )
            )
        )
        val result = renderTemplate(template, 2f, MeasuringSystem.Metric)
        assertEquals("1-2 tsp", result)
    }

    @Test
    fun `scale template preserves integer values without decimals`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 2f,
                    unit = "kg",
                    scale = true
                )
            )
        )
        val result = renderTemplate(template, 2f, MeasuringSystem.Metric)
        assertEquals("4 kg", result)
    }

    @Test
    fun `scale template should ignore max value if identical to min`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 2f,
                    max = 2f,
                    unit = "kg",
                    scale = true
                ),
                TemplateConst(" "),
                QuantityPlaceholder(
                    min = 2f,
                    max = 2f,
                    unit = "kg",
                    scale = false
                ),
            )
        )
        val result = renderTemplate(template, 2f, MeasuringSystem.Metric)
        assertEquals("4 kg 2 kg", result)
    }

    @Test
    fun `use plural for units that support it`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 1.5f,
                    unit = "cups",
                    scale = true
                ),
                TemplateConst(" "),
                QuantityPlaceholder(
                    min = 1f,
                    unit = "lb",
                    scale = true
                ),
            )
        )
        val result = renderTemplate(template, 10f, MeasuringSystem.Metric)
        assertEquals("15 cups 10 lbs", result)
    }

    @Test
    fun `convert units when asking for imperial values`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 1.5f,
                    unit = "kg",
                    scale = true
                ),
                TemplateConst(" "),
                QuantityPlaceholder(
                    min = 100f,
                    unit = "g",
                    scale = true
                ),
                TemplateConst(" "),
                QuantityPlaceholder(
                    min = 1f,
                    unit = null,
                    scale = true
                ),
            )
        )
        val result = renderTemplate(template, 1f, MeasuringSystem.Imperial)
        assertEquals("3.31 lbs 3.53 oz 1", result)
    }

    @Test
    fun `Round to nearest fraction when converting to cups`() {
        val template = ParsedTemplate(
            listOf(
                QuantityPlaceholder(
                    min = 100f,
                    unit = "ml",
                    scale = true,
                    usCust = true
                ),
                TemplateConst(" of water, "),
                QuantityPlaceholder(
                    min = 120f,
                    unit = "ml",
                    scale = true,
                    usCust = true
                ),
                TemplateConst(" of oil"),
            )
        )
        val result = renderTemplate(template, 1f, MeasuringSystem.USCustomary)
        assertEquals("⅜ cup of water, ½ cup of oil", result)
    }

    @Test
    fun `replace simple punctuation with Guardian style punctuation`() {
        val template = ParsedTemplate(
            listOf(
                TemplateConst("Use \"00\" flour and you'll get - I think - the best results."),
            )
        )
        val result = renderTemplate(template, 1f, MeasuringSystem.Metric)
        assertEquals("Use “00” flour and you’ll get – I think – the best results.", result)
    }
}

