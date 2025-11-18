package io.github.kotlin.fibonacci.com.gu.recipe

import com.gu.recipe.IngredientUnit
import com.gu.recipe.generated.*
import com.gu.recipe.scaleAndConvertUnitRecipe
import kotlin.test.Test
import kotlin.test.assertEquals

class ScaleRecipeTest {
    @Test
    fun `scale a recipe`() {
        val recipeTemplate = RecipeV3(
            id = "test-recipe",
            ingredients = listOf(
                IngredientElement(
                    ingredientsList = listOf(
                        IngredientsListElement(
                            template = """{"min": 100, "max": 120, "unit": "g", "scale": true} of flour"""
                        ),
                        IngredientsListElement(
                            template = """{"min": 1.2, "unit": "kg", "scale": true} of potatoes"""
                        ),
                        IngredientsListElement(
                            template = """{"min": 0.25, "unit": "tbsp", "scale": true} of salt"""
                        ),
                        IngredientsListElement(
                            template = """{"min":1, "scale":true} {"min":400, "unit":"g", "scale":false} tin chopped tomatoes"""
                        ),
                    )
                )
            ),
            instructions = listOf(
                InstructionElement(
                    descriptionTemplate = """pre-warm the oven to {"temperatureC": 180, "temperatureFanC": 160}""",
                    description = "should be replaced"
                ),
                InstructionElement(
                    descriptionTemplate = """pre-warm the oven to {"temperatureFanC": 160, "temperatureF": 325}""",
                    description = "should be replaced too"
                )
            )
        )
        val expectedRecipe = RecipeV3(
            id = "test-recipe",
            ingredients = listOf(
                IngredientElement(
                    ingredientsList = listOf(
                        IngredientsListElement(
                            template = """{"min": 100, "max": 120, "unit": "g", "scale": true} of flour""",
                            text = "200-240 g of flour"
                        ),
                        IngredientsListElement(
                            template = """{"min": 1.2, "unit": "kg", "scale": true} of potatoes""",
                            text = "2.4 kg of potatoes"
                        ),
                        IngredientsListElement(
                            template = """{"min": 0.25, "unit": "tbsp", "scale": true} of salt""",
                            text = "½ tbsp of salt"
                        ),
                        IngredientsListElement(
                            template = """{"min":1, "scale":true} {"min":400, "unit":"g", "scale":false} tin chopped tomatoes""",
                            text = "2 400 g tin chopped tomatoes"
                        ),
                    )
                )
            ),
            instructions = listOf(
                InstructionElement(
                    descriptionTemplate = """pre-warm the oven to {"temperatureC": 180, "temperatureFanC": 160}""",
                    description = "pre-warm the oven to 180C (160C fan)"
                ),
                InstructionElement(
                    descriptionTemplate = """pre-warm the oven to {"temperatureFanC": 160, "temperatureF": 325}""",
                    description = "pre-warm the oven to 160C fan/325F"
                )
            )
        )
        val scaledRecipe = scaleAndConvertUnitRecipe(recipeTemplate, 2.0f, unit = IngredientUnit.Metric)
        assertEquals(
            expectedRecipe,
            scaledRecipe
        )
    }
}