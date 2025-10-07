package io.github.kotlin.fibonacci.com.gu.recipe

import com.gu.recipe.ClientSideRecipe
import com.gu.recipe.SeverSideRecipe
import com.gu.recipe.generated.StringTemplate
import com.gu.recipe.TemplateElement
import com.gu.recipe.generated.IngredientElement
import com.gu.recipe.generated.IngredientsListIngredientsList
import com.gu.recipe.generated.IngredientsTemplateElement
import com.gu.recipe.generated.IngredientsTemplateIngredientsList
import com.gu.recipe.generated.InstructionElement
import com.gu.recipe.generated.InstructionsTemplateElement
import com.gu.recipe.parseTemplate
import com.gu.recipe.scaleRecipe
import kotlin.test.Test
import kotlin.test.assertEquals

class ScaleRecipeTest {

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
    fun `scale a recipe`() {
        val recipeTemplate = SeverSideRecipe(
            id = "test-recipe",
            ingredientsTemplate = listOf(
                IngredientsTemplateElement(
                    ingredientsList = listOf(
                        IngredientsTemplateIngredientsList(
                            template = """{"min": 100, "max": 120, "unit": "g", "scale": true} of flour"""
                        )
                    )
                )
            ),
            instructionsTemplate = listOf(
                InstructionsTemplateElement(
                    descriptionTemplate = """pre-warm the oven to {"temperatureC": 180, "temperatureFanC": 160}"""
                )
            )
        )
        val expectedRecipe = ClientSideRecipe(
            id = "test-recipe",
            ingredients = listOf(IngredientElement(ingredientsList = listOf(IngredientsListIngredientsList(
                text = "200-240 g of flour"
            )))),
            instructions = listOf(InstructionElement(description = "pre-warm the oven to 180°C (160°C fan)"))
        )
        val scaledRecipe = scaleRecipe(recipeTemplate, 2.0f)
        assertEquals(
            expectedRecipe,
            scaledRecipe
        )
    }
}