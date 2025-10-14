package io.github.kotlin.fibonacci.com.gu.recipe

import com.gu.recipe.ClientSideRecipe
import com.gu.recipe.IngredientUnit
import com.gu.recipe.ServerSideRecipe
import com.gu.recipe.generated.*
import com.gu.recipe.scaleRecipe
import kotlin.test.Test
import kotlin.test.assertEquals

class ScaleRecipeTest {
    @Test
    fun `scale a recipe`() {
        val recipeTemplate = ServerSideRecipe(
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
            ingredients = listOf(
                IngredientElement(
                    ingredientsList = listOf(
                        IngredientsListIngredientsList(
                            text = "200-240 g of flour"
                        )
                    )
                )
            ),
            instructions = listOf(InstructionElement(description = "pre-warm the oven to 180°C (160°C fan)"))
        )
        val scaledRecipe = scaleRecipe(recipeTemplate, 2.0f, unit = IngredientUnit.Metric)
        assertEquals(
            expectedRecipe,
            scaledRecipe
        )
    }
}