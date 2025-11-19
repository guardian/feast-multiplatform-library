package com.gu.recipe.template

import kotlin.test.Test
import kotlin.test.assertEquals

class TemplateParserTest {
    @Test
    fun `parse template`() {
        val template =
            "Bake at {\"temperatureC\": 180, \"temperatureFanC\": 160} for {\"min\": 30, \"max\": 40, \"unit\": \"minutes\"}."
        val parsed = parseTemplate(template)

        assertEquals(
            listOf(
                TemplateConst("Bake at "),
                OvenTemperaturePlaceholder(
                    temperatureC = 180,
                    temperatureFanC = 160
                ),
                TemplateConst(" for "),
                QuantityPlaceholder(min = 30f, max = 40f, unit = "minutes"),
                TemplateConst(".")
            ),
            parsed.elements
        )
    }

    @Test
    fun `parse template with a single space`() {
        val template =
            "{\"min\":1, \"scale\":true} {\"min\":400, \"unit\":\"g\", \"scale\":false} tin chopped tomatoes"
        val parsed = parseTemplate(template)

        assertEquals(
            listOf(
                QuantityPlaceholder(min = 1f, scale = true),
                TemplateConst(" "),
                QuantityPlaceholder(min = 400f, scale = false, unit = "g"),
                TemplateConst(" tin chopped tomatoes")
            ),
            parsed.elements
        )
    }

    @Test
    fun `parse a template - ignoring extra properties`() {
        val template =
            "Bake at {\"temperatureC\": 180, \"temperatureFanC\": 160, \"newProperty\": true} for {\"min\": 30, \"max\": 40, \"unit\": \"minutes\", \"newProperty\": true}."
        val parsed = parseTemplate(template)

        assertEquals(
            listOf(
                TemplateConst("Bake at "),
                OvenTemperaturePlaceholder(
                    temperatureC = 180,
                    temperatureFanC = 160
                ),
                TemplateConst(" for "),
                QuantityPlaceholder(min = 30f, max = 40f, unit = "minutes"),
                TemplateConst(".")
            ),
            parsed.elements
        )
    }
}