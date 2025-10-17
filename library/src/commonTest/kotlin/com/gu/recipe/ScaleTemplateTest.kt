package com.gu.recipe

import com.gu.recipe.template.ParsedTemplate
import com.gu.recipe.template.TemplateElement
import kotlin.test.Test
import kotlin.test.assertEquals

class ScaleTemplateTest {
    @Test
    fun `scale template with simple quantity placeholder`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.QuantityPlaceholder(
                    min = 100f,
                    unit = "g",
                    scale = true
                )
            )
        )
        val result = scaleTemplate(template, 2f)
        assertEquals("200 g", result)
    }

    @Test
    fun `scale template with quantity range`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.QuantityPlaceholder(
                    min = 100f,
                    max = 150f,
                    unit = "g",
                    scale = true
                )
            )
        )
        val result = scaleTemplate(template, 2f)
        assertEquals("200-300 g", result)
    }

    @Test
    fun `scale template with fraction for tbsp`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.QuantityPlaceholder(
                    min = 0.5f,
                    unit = "tbsp",
                    scale = true
                )
            )
        )
        val result = scaleTemplate(template, 2f)
        assertEquals("1 tbsp", result)
    }

    @Test
    fun `scale template with fraction for tsp`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.QuantityPlaceholder(
                    min = 0.25f,
                    unit = "tsp",
                    scale = true
                )
            )
        )
        val result = scaleTemplate(template, 2f)
        assertEquals("½ tsp", result)
    }

    @Test
    fun `scale template with fraction for cups`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.QuantityPlaceholder(
                    min = 1.5f,
                    unit = "cups",
                    scale = true
                )
            )
        )
        val result = scaleTemplate(template, 0.5f)
        assertEquals("¾ cups", result)
    }

    @Test
    fun `scale template without unit uses fractions`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.QuantityPlaceholder(
                    min = 2f,
                    scale = true
                )
            )
        )
        val result = scaleTemplate(template, 0.5f)
        assertEquals("1", result)
    }

    @Test
    fun `scale template with ml uses no decimals`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.QuantityPlaceholder(
                    min = 250f,
                    unit = "ml",
                    scale = true
                )
            )
        )
        val result = scaleTemplate(template, 1.5f)
        assertEquals("375 ml", result)
    }

    @Test
    fun `scale template with non-scalable quantity`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.QuantityPlaceholder(
                    min = 100f,
                    unit = "g",
                    scale = false
                )
            )
        )
        val result = scaleTemplate(template, 2f)
        assertEquals("100 g", result)
    }

    @Test
    fun `scale template with oven temperature`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.OvenTemperaturePlaceholder(
                    temperatureC = 180,
                    temperatureFanC = 160
                )
            )
        )
        val result = scaleTemplate(template, 2f)
        assertEquals("180C (160C fan)", result)
    }

    @Test
    fun `scale template with oven temperature without fan`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.OvenTemperaturePlaceholder(
                    temperatureC = 200
                )
            )
        )
        val result = scaleTemplate(template, 2f)
        assertEquals("200C", result)
    }

    @Test
    fun `scale template with const text`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.TemplateConst("Add ")
            )
        )
        val result = scaleTemplate(template, 2f)
        assertEquals("Add ", result)
    }

    @Test
    fun `scale template with mixed elements`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.TemplateConst("Add "),
                TemplateElement.QuantityPlaceholder(
                    min = 100f,
                    max = 120f,
                    unit = "g",
                    scale = true
                ),
                TemplateElement.TemplateConst(" of flour")
            )
        )
        val result = scaleTemplate(template, 2f)
        assertEquals("Add 200-240 g of flour", result)
    }

    @Test
    fun `scale template with complex recipe instruction`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.TemplateConst("Bake at "),
                TemplateElement.OvenTemperaturePlaceholder(
                    temperatureC = 180,
                    temperatureFanC = 160
                ),
                TemplateElement.TemplateConst(" for "),
                TemplateElement.QuantityPlaceholder(
                    min = 30f,
                    max = 40f,
                    unit = "minutes",
                    scale = false
                ),
                TemplateElement.TemplateConst(".")
            )
        )
        val result = scaleTemplate(template, 2f)
        assertEquals("Bake at 180C (160C fan) for 30-40 minutes.", result)
    }

    @Test
    fun `scale template with factor less than 1`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.QuantityPlaceholder(
                    min = 200f,
                    unit = "g",
                    scale = true
                )
            )
        )
        val result = scaleTemplate(template, 0.5f)
        assertEquals("100 g", result)
    }

    @Test
    fun `scale template with non-standard unit uses decimals`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.QuantityPlaceholder(
                    min = 1f,
                    unit = "kg",
                    scale = true
                )
            )
        )
        val result = scaleTemplate(template, 1.5f)
        assertEquals("1.5 kg", result)
    }

    @Test
    fun `scale template with quantity without unit showing fraction`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.QuantityPlaceholder(
                    min = 1f,
                    scale = true
                )
            )
        )
        val result = scaleTemplate(template, 0.75f)
        assertEquals("¾", result)
    }

    @Test
    fun `scale template with range and fractions`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.QuantityPlaceholder(
                    min = 0.5f,
                    max = 1f,
                    unit = "tsp",
                    scale = true
                )
            )
        )
        val result = scaleTemplate(template, 2f)
        assertEquals("1-2 tsp", result)
    }

    @Test
    fun `scale template preserves integer values without decimals`() {
        val template = ParsedTemplate(
            listOf(
                TemplateElement.QuantityPlaceholder(
                    min = 2f,
                    unit = "kg",
                    scale = true
                )
            )
        )
        val result = scaleTemplate(template, 2f)
        assertEquals("4 kg", result)
    }
}

