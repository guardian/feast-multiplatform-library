package com.gu.recipe

import com.gu.recipe.unit.MeasuringSystem
import com.gu.recipe.generated.*
import com.gu.recipe.density.DensityTable
import com.gu.recipe.template.QuantityPlaceholder
import kotlin.test.Test
import kotlin.test.assertEquals

class RenderRecipeTest {
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
                            text = "<strong>1½ tsp of salt</strong>"
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
        val session = RenderSession(densityTable)
        val scaledRecipe = session.renderRecipe(recipeTemplate, 2.0f, measuringSystem = MeasuringSystem.Metric)
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
    }
        @Test
        fun `test tbsp conversion for 1_5 input`() {
            // Arrange
            val densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap())
            val renderSession = RenderSession(densityTable)
            val placeholder = QuantityPlaceholder(
                min = 1.5f,
                max = 1.5f,
                unit = "tbsp",
                scale = true,
                ingredient = "lemon juice",
                usCust = true
            )
            val factor = 1.0f
            val measuringSystem = MeasuringSystem.USCustomary

            // Act
            val result = renderSession.renderQuantity(placeholder, factor, measuringSystem)

            // Assert
            assertEquals("1½ tbsp", result)
    }

    @Test
    fun `test conversion for chicken thigh for grams conversion to nearly 1 pound`() {
        // Arrange
        val densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap())
        val renderSession = RenderSession(densityTable)

        val placeholder = QuantityPlaceholder(
            min = 500f,
            max = 500f,
            unit = "g",
            scale = true,
            ingredient = "chicken or vegetable stock",
            usCust = true
        )
        val factor = 1.0f
        val measuringSystem = MeasuringSystem.USCustomary

        // Act
        val result = renderSession.renderQuantity(placeholder, factor, measuringSystem)

        // Assert
        assertEquals("1 lb", result)
    }

    @Test
    fun `test conversion for chicken thigh for grams conversion to 1 with a quarter more`() {
        // Arrange
        val densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap())
        val renderSession = RenderSession(densityTable)

        val placeholder = QuantityPlaceholder(
            min = 580f,
            max = 580f,
            unit = "g",
            scale = true,
            ingredient = "chicken or vegetable stock",
            usCust = true
        )
        val factor = 1.0f
        val measuringSystem = MeasuringSystem.USCustomary

        // Act
        val result = renderSession.renderQuantity(placeholder, factor, measuringSystem)

        // Assert
        assertEquals("1¼ lbs", result)
    }

    @Test
    fun `test conversion for chicken thigh for grams conversion to 2`() {
        // Arrange
        val densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap())
        val renderSession = RenderSession(densityTable)

        val placeholder = QuantityPlaceholder(
            min = 900f,
            max = 900f,
            unit = "g",
            scale = true,
            ingredient = "chicken or vegetable stock",
            usCust = true
        )
        val factor = 1.0f
        val measuringSystem = MeasuringSystem.USCustomary

        // Act
        val result = renderSession.renderQuantity(placeholder, factor, measuringSystem)

        // Assert
        assertEquals("2 lbs", result)
    }

    @Test
    fun `renderRecipeForTerminology converts only the requested enum section`() {
        val session = RenderSession(
            densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap()),
            terminologyTable = com.gu.recipe.terminology.TerminologyTable(
                preparedAt = "none",
                terminologyMap = mapOf(
                    "aubergine" to "eggplant",
                    "icing sugar" to "powdered sugar"
                )
            )
        )
        val recipe = RecipeV3(
            id = "test-recipe",
            title = "aubergine tart",
            description = "aubergine with icing sugar",
            ingredients = listOf(
                IngredientsList(
                    recipeSection = "aubergine topping",
                    ingredientsList = listOf(
                        IngredientItem(
                            text = "1 aubergine",
                            template = "aubergine with icing sugar"
                        )
                    )
                )
            ),
            instructions = listOf(
                Instruction(
                    description = "Roast the aubergine",
                    descriptionTemplate = "aubergine"
                )
            )
        )

        val rendered = session.renderRecipeForTerminology(recipe, TerminologySection.INGREDIENTS)

        assertEquals("aubergine tart", rendered.title)
        assertEquals("aubergine with icing sugar", rendered.description)
        assertEquals("1 eggplant", rendered.ingredients?.first()?.ingredientsList?.first()?.text)
        assertEquals("eggplant topping", rendered.ingredients?.first()?.recipeSection)
        assertEquals("eggplant with powdered sugar", rendered.ingredients?.first()?.ingredientsList?.first()?.template)
        assertEquals("Roast the aubergine", rendered.instructions?.first()?.description)
        assertEquals("aubergine", rendered.instructions?.first()?.descriptionTemplate)
    }

    @Test
    fun `renderRecipeForTerminology ALL converts every section`() {
        val session = RenderSession(
            densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap()),
            terminologyTable = com.gu.recipe.terminology.TerminologyTable(
                preparedAt = "none",
                terminologyMap = mapOf("aubergine" to "eggplant")
            )
        )
        val recipe = RecipeV3(
            id = "test-recipe",
            title = "aubergine tart",
            description = "aubergine filling",
            ingredients = listOf(
                IngredientsList(
                    recipeSection = "aubergine topping",
                    ingredientsList = listOf(IngredientItem(text = "1 aubergine", template = "aubergine"))
                )
            ),
            instructions = listOf(Instruction(description = "Roast the aubergine", descriptionTemplate = "aubergine"))
        )

        val rendered = session.renderRecipeForTerminology(recipe, TerminologySection.ALL)

        assertEquals("eggplant tart", rendered.title)
        assertEquals("eggplant filling", rendered.description)
        assertEquals("eggplant topping", rendered.ingredients?.first()?.recipeSection)
        assertEquals("1 eggplant", rendered.ingredients?.first()?.ingredientsList?.first()?.text)
        assertEquals("eggplant", rendered.ingredients?.first()?.ingredientsList?.first()?.template)
        assertEquals("Roast the eggplant", rendered.instructions?.first()?.description)
        assertEquals("eggplant", rendered.instructions?.first()?.descriptionTemplate)
    }

    @Test
    fun `renderRecipeForTerminology default section converts every section`() {
        val session = RenderSession(
            densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap()),
            terminologyTable = com.gu.recipe.terminology.TerminologyTable(
                preparedAt = "none",
                terminologyMap = mapOf("aubergine" to "eggplant")
            )
        )
        val recipe = RecipeV3(
            id = "test-recipe",
            title = "aubergine tart",
            description = "aubergine filling",
            ingredients = listOf(
                IngredientsList(
                    recipeSection = "aubergine topping",
                    ingredientsList = listOf(IngredientItem(text = "1 aubergine", template = "aubergine"))
                )
            ),
            instructions = listOf(Instruction(description = "Roast the aubergine", descriptionTemplate = "aubergine"))
        )

        val rendered = session.renderRecipeForTerminology(recipe)

        assertEquals("eggplant tart", rendered.title)
        assertEquals("eggplant filling", rendered.description)
        assertEquals("eggplant topping", rendered.ingredients?.first()?.recipeSection)
        assertEquals("1 eggplant", rendered.ingredients?.first()?.ingredientsList?.first()?.text)
        assertEquals("eggplant", rendered.ingredients?.first()?.ingredientsList?.first()?.template)
        assertEquals("Roast the eggplant", rendered.instructions?.first()?.description)
        assertEquals("eggplant", rendered.instructions?.first()?.descriptionTemplate)
    }

    @Test
    fun `replaceInText is case insensitive and only replaces whole terms`() {
        val session = RenderSession(
            densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap()),
            terminologyTable = com.gu.recipe.terminology.TerminologyTable(
                preparedAt = "none",
                terminologyMap = mapOf("aubergine" to "eggplant")
            )
        )

        assertEquals("Roast the eggplant", session.replaceInText("Roast the AUBERGINE"))
        assertEquals("aubergines are great", session.replaceInText("aubergines are great"))
    }

    @Test
    fun `replaceInText prefers longer terminology matches before shorter ones`() {
        val session = RenderSession(
            densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap()),
            terminologyTable = com.gu.recipe.terminology.TerminologyTable(
                preparedAt = "none",
                terminologyMap = mapOf(
                    "sugar" to "sweetener",
                    "icing sugar" to "powdered sugar"
                )
            )
        )

        assertEquals("powdered sugar", session.replaceInText("icing sugar"))
    }


}