package com.gu.recipe

import com.gu.recipe.unit.MeasuringSystem
import com.gu.recipe.generated.*
import com.gu.recipe.density.DensityTable
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
                            text = "<strong>2 tsp of salt</strong>"
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
        val densityTable = DensityTable("test", HashMap(), HashMap())
        val session = TemplateSession(densityTable)
        val scaledRecipe = session.scaleAndConvertUnitRecipe(recipeTemplate, 2.0f, measuringSystem = MeasuringSystem.Metric)
        assertEquals(
            expectedRecipe,
            scaledRecipe
        )
    }

    @Test
    fun `wrapWithStrongTag should wrap everything except bracket groups and comma or semicolon suffix`() {
        assertEquals("<strong>14 oz</strong> • <strong>3 cups </strong>(400g)<strong> banana shallots </strong>(about 6), peeled, halved lengthways and finely sliced", wrapWithStrongTag("14 oz • 3 cups (400g) banana shallots (about 6), peeled, halved lengthways and finely sliced"))
        assertEquals("<strong>100 ml kefir</strong>", wrapWithStrongTag("100 ml kefir"))
        assertEquals("<strong>1/2 cup </strong>(100 ml)<strong> kefir</strong>", wrapWithStrongTag("1/2 cup (100 ml) kefir"))
        assertEquals("<strong>1-2 of something</strong>", wrapWithStrongTag("1-2 of something"))
        assertEquals("<strong>1-2 kg oranges</strong>, organic", wrapWithStrongTag("1-2 kg oranges, organic"))
        assertEquals("<strong>150 g mayonnaise </strong>(homemade or shop-bought)", wrapWithStrongTag("150 g mayonnaise (homemade or shop-bought)"))
    }

    @Test
    fun `textWithoutSuffix returns first part of the ingredient`() {
        val ingredient = "1 potato, (100g) thinly chopped"
        assertEquals("1 potato", ingredientWithoutSuffix(ingredient))

        val ingredient2 = "150 g mayonnaise (homemade or shop-bought)"
        assertEquals("150 g mayonnaise", ingredientWithoutSuffix(ingredient2))

        val ingredient3 = "150 g egg; (small)"
        assertEquals("150 g egg", ingredientWithoutSuffix(ingredient3))

        val ingredient4 = "5¾ oz • ¾ cup (165 g) short-grain white rice, or pasta if you're inclined"
        assertEquals("5¾ oz • ¾ cup (165 g) short-grain white rice", ingredientWithoutSuffix(ingredient4))

        val ingredient5 = "2 onions (large), finely chopped"
        assertEquals("2 onions (large)", ingredientWithoutSuffix(ingredient5))

        // No suffix at all
        val ingredient6 = "3 cloves garlic"
        assertEquals("3 cloves garlic", ingredientWithoutSuffix(ingredient6))

        // Metric conversion in parens with no suffix after ingredient
        val ingredient7 = "1 cup (240 ml) whole milk"
        assertEquals("1 cup (240 ml) whole milk", ingredientWithoutSuffix(ingredient7))

        // Multiple unit systems with bullet separator and comma suffix
        val ingredient8 = "7 oz • 1½ cups (200 g) plain flour, plus extra for dusting"
        assertEquals("7 oz • 1½ cups (200 g) plain flour", ingredientWithoutSuffix(ingredient8))

        // Semicolon suffix with no parens
        val ingredient9 = "500 g chicken thighs; boneless and skinless"
        assertEquals("500 g chicken thighs", ingredientWithoutSuffix(ingredient9))

        // Trailing paren qualifier with no comma
        val ingredient10 = "2 lemons (unwaxed)"
        assertEquals("2 lemons", ingredientWithoutSuffix(ingredient10))

        // Paren descriptor followed by semicolon
        val ingredient11 = "3 peppers (mixed colours); deseeded"
        assertEquals("3 peppers (mixed colours)", ingredientWithoutSuffix(ingredient11))

        // Just a simple ingredient with parens in the middle followed by more text and comma
        val ingredient12 = "14 oz • 3 cups (400g) banana shallots (about 6), peeled and sliced"
        assertEquals("14 oz • 3 cups (400g) banana shallots (about 6)", ingredientWithoutSuffix(ingredient12))
    }
}