package io.github.kotlin.fibonacci.com.gu.recipe

import com.gu.recipe.unit.MeasuringSystem
import com.gu.recipe.generated.*
import com.gu.recipe.scaleAndConvertUnitRecipe
import com.gu.recipe.wrapWithStrongTag
import kotlin.test.Test
import kotlin.test.assertEquals

class ScaleRecipeTest {
    @Test
    fun `scale a recipe`() {
        val recipeTemplate = RecipeV3(
            id = "test-recipe",
            ingredients = listOf(
                IngredientsList(
                    ingredientsList = listOf(
                        IngredientItem(
                            template = """{"min": 100, "max": 120, "unit": "g", "scale": true} of flour"""
                        ),
                        IngredientItem(
                            template = """{"min": 1.2, "unit": "kg", "scale": true} of potatoes"""
                        ),
                        IngredientItem(
                            template = """{"min": 0.25, "unit": "tbsp", "scale": true} of salt"""
                        ),
                        IngredientItem(
                            template = """{"min":1, "scale":true} x {"min":400, "unit":"g", "scale":false} tin chopped tomatoes"""
                        ),
                    )
                )
            ),
            instructions = listOf(
                Instruction(
                    descriptionTemplate = """pre-warm the oven to {"temperatureC": 180, "temperatureFanC": 160}""",
                    description = "should be replaced"
                ),
                Instruction(
                    descriptionTemplate = """pre-warm the oven to {"temperatureFanC": 160, "temperatureF": 325}""",
                    description = "should be replaced too"
                )
            )
        )
        val expectedRecipe = RecipeV3(
            id = "test-recipe",
            ingredients = listOf(
                IngredientsList(
                    ingredientsList = listOf(
                        IngredientItem(
                            template = """{"min": 100, "max": 120, "unit": "g", "scale": true} of flour""",
                            text = "<strong>200-240 g of flour</strong>"
                        ),
                        IngredientItem(
                            template = """{"min": 1.2, "unit": "kg", "scale": true} of potatoes""",
                            text = "<strong>2.4 kg of potatoes</strong>"
                        ),
                        IngredientItem(
                            template = """{"min": 0.25, "unit": "tbsp", "scale": true} of salt""",
                            text = "<strong>½ tbsp of salt</strong>"
                        ),
                        IngredientItem(
                            template = """{"min":1, "scale":true} x {"min":400, "unit":"g", "scale":false} tin chopped tomatoes""",
                            text = "<strong>2 x 400 g tin chopped tomatoes</strong>"
                        ),
                    )
                )
            ),
            instructions = listOf(
                Instruction(
                    descriptionTemplate = """pre-warm the oven to {"temperatureC": 180, "temperatureFanC": 160}""",
                    description = "pre-warm the oven to 180C (160C fan)"
                ),
                Instruction(
                    descriptionTemplate = """pre-warm the oven to {"temperatureFanC": 160, "temperatureF": 325}""",
                    description = "pre-warm the oven to 160C fan/325F"
                )
            )
        )
        val scaledRecipe = scaleAndConvertUnitRecipe(recipeTemplate, 2.0f, measuringSystem = MeasuringSystem.Metric)
        assertEquals(
            expectedRecipe,
            scaledRecipe
        )
    }

    @Test
    fun `wrapWithStrongTag should wrap text before first punctuation`() {
        assertEquals("<strong>1-2 of something</strong>", wrapWithStrongTag("1-2 of something"))
        assertEquals("<strong>1-2 kg oranges</strong>, organic", wrapWithStrongTag("1-2 kg oranges, organic"))
    }

    @Test
    fun `textWithoutSuffix returns first part of the ingredient`() {
        val ingredient = IngredientItem(text = "1 potato, (100g) thinly chopped")
        assertEquals("1 potato", ingredient.textWithoutSuffix())
    }
}