package io.github.kotlin.fibonacci

import com.gu.recipe.Recipe
import com.gu.recipe.RecipeTemplate
import com.gu.recipe.Template
import com.gu.recipe.TemplateElement
import com.gu.recipe.parseTemplate
import com.gu.recipe.scaleRecipe
import kotlin.test.Test
import kotlin.test.assertEquals

class ScaleRecipeTest {

    @Test
    fun `parse template`() {
        val template =
            Template("Bake at {\"temperatureC\": 180, \"temperatureFanC\": 160} for {\"min\": 30, \"max\": 40, \"unit\": \"minutes\"}.")
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
    fun `scale a recipe`() {
        val recipeTemplate = RecipeTemplate(
            id = "test-recipe",
            title = "Test Recipe",
            ingredients = listOf(Template("""{"min": 100, "max": 120, "unit": "g", "scale": true} of flour""")),
            instructions = listOf(Template("""pre-warm the oven to {"temperatureC": 180, "temperatureFanC": 160}""")),
        )
        val scaledRecipe = scaleRecipe(recipeTemplate, 2.0f)
        assertEquals(
            Recipe(
                id = "test-recipe",
                title = "Test Recipe",
                ingredients = listOf("200-240 g of flour"),
                instructions = listOf("pre-warm the oven to 180°C (160°C fan)")
            ),
            scaledRecipe
        )
    }
}