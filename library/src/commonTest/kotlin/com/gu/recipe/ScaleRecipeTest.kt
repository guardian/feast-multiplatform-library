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
                        ),
                        IngredientsTemplateIngredientsList(
                            template = """{"min": 1.2, "unit": "kg", "scale": true} of potatoes"""
                        ),
                        IngredientsTemplateIngredientsList(
                            template = """{"min": 0.25, "unit": "tbsp", "scale": true} of salt"""
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
                        ),
                        IngredientsListIngredientsList(
                            text = "2.4 kg of potatoes"
                        ),
                        IngredientsListIngredientsList(
                            text = "½ tbsp of salt"
                        )
                    )
                )
            ),
            instructions = listOf(InstructionElement(description = "pre-warm the oven to 180C (160C fan)"))
        )
        val scaledRecipe = scaleRecipe(recipeTemplate, 2.0f, unit = IngredientUnit.Metric)
        assertEquals(
            expectedRecipe,
            scaledRecipe
        )
    }
}