package com.gu.recipe.template

import com.gu.recipe.generated.StringTemplate
import kotlin.test.Test
import kotlin.test.assertEquals

class TemplateParserTest {
    @Test
    fun `parse template`() {
        val template: StringTemplate =
            "Bake at {\"temperatureC\": 180, \"temperatureFanC\": 160} for {\"min\": 30, \"max\": 40, \"unit\": \"minutes\"}."
        val parsed = parseTemplate(template)

        assertEquals(
            listOf(
                TemplateElement.TemplateConst("Bake at "),
                TemplateElement.OvenTemperaturePlaceholder(
                    temperatureC = 180,
                    temperatureFanC = 160
                ),
                TemplateElement.TemplateConst(" for "),
                TemplateElement.QuantityPlaceholder(min = 30f, max = 40f, unit = "minutes"),
                TemplateElement.TemplateConst(".")
            ),
            parsed.elements
        )
    }
    @Test
    fun `parse template with a single space`() {
        val template: StringTemplate =
            "{\"min\":1, \"scale\":true} {\"min\":400, \"unit\":\"g\", \"scale\":false} tin chopped tomatoes"
        val parsed = parseTemplate(template)

        assertEquals(
            listOf(
                TemplateElement.QuantityPlaceholder(min = 1f, scale = true),
                TemplateElement.TemplateConst(" "),
                TemplateElement.QuantityPlaceholder(min = 400f, scale = false, unit = "g"),
                TemplateElement.TemplateConst(" tin chopped tomatoes")
            ),
            parsed.elements
        )
    }
    @Test
    fun `parse a template - ignoring extra properties`() {
        val template: StringTemplate =
            "Bake at {\"temperatureC\": 180, \"temperatureFanC\": 160, \"newProperty\": true} for {\"min\": 30, \"max\": 40, \"unit\": \"minutes\", \"newProperty\": true}."
        val parsed = parseTemplate(template)

        assertEquals(
            listOf(
                TemplateElement.TemplateConst("Bake at "),
                TemplateElement.OvenTemperaturePlaceholder(
                    temperatureC = 180,
                    temperatureFanC = 160
                ),
                TemplateElement.TemplateConst(" for "),
                TemplateElement.QuantityPlaceholder(min = 30f, max = 40f, unit = "minutes"),
                TemplateElement.TemplateConst(".")
            ),
            parsed.elements
        )
    }
}