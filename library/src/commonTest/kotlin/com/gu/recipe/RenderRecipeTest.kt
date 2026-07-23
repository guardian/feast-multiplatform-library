package com.gu.recipe

import com.gu.recipe.unit.MeasuringSystem
import com.gu.recipe.generated.*
import com.gu.recipe.density.DensityTable
import com.gu.recipe.template.QuantityPlaceholder
import com.gu.recipe.terminology.TerminologyEntry
import kotlinx.serialization.json.Json
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
                            text = "<strong>200-240 g of flour</strong>",
                            ingredientWithoutSuffix = "<strong>200-240 g of flour</strong>"
                        ),
                        IngredientItem(
                            template = """{"min": 1.2, "unit": "kg", "scale": true} of potatoes""",
                            text = "<strong>2.4 kg of potatoes</strong>",
                            ingredientWithoutSuffix = "<strong>2.4 kg of potatoes</strong>"
                        ),
                        IngredientItem(
                            template = """{"min": 0.25, "unit": "tbsp", "scale": true} of salt""",
                            text = "<strong>1½ tsp of salt</strong>",
                            ingredientWithoutSuffix = "<strong>1½ tsp of salt</strong>"
                        ),
                        IngredientItem(
                            template = """{"min":1, "scale":true} x {"min":400, "unit":"g", "scale":false} tin chopped tomatoes""",
                            text = "<strong>2 x 400 g tin chopped tomatoes</strong>",
                            ingredientWithoutSuffix = "<strong>2 x 400 g tin chopped tomatoes</strong>"
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

    val htmlTagRegex = "<[^>]+>".toRegex()

    fun stripHTMLFromString(str: String?): String? {
        if(str == null) {
            return null
        } else {
            return str.replace(htmlTagRegex, "")
        }
    }
    fun stripExtraHTML(recipe: RecipeV3): RecipeV3 {
        return recipe.copy(
            ingredients = recipe.ingredients?.map { it.copy(
                ingredientsList = it.ingredientsList?.map {
                    it.copy(
                        text = stripHTMLFromString(it.text),
                        ingredientWithoutSuffix = null
                    )
                }
            ) }
        )
    }

    val usCustomaryRecipeFixture = """{
"appExclusiveBranding": true,
"originalMeasuringSystem": "us",
"bookCredit": "",
"canonicalArticle": "food/2026/jun/22/ali-slagle",
"celebrationIds": [],
"commerceCtas": [],
"composerId": "6a0f12fe8f089865cf41a3ab",
"byline": ["Ali Slagle"],
"cuisineIds": [
"thai",
"american"
],
"description": "This rendition of fried chicken larb, inspired by one from Thai Diner in New York City, trades ground meat for, excitingly, frozen chicken nuggets; the result is all at once crispy and crunchy, rich and refreshing. Ali Slagle lives in New York and has been developing low-effort, high-reward recipes for home cooks for over a decade",
"difficultyLevel": "easy",
"featuredImage": {
"url": "https://media.guim.co.uk/58504a676bc886d8e98d08b8a07590afb9f4d932/0_0_4388_5483/1601.jpg",
"mediaId": "58504a676bc886d8e98d08b8a07590afb9f4d932",
"cropId": "0_0_4388_5483",
"source": "The Guardian",
"photographer": "Phoebe Pearson",
"caption": "Ali Slagle's fried chicken larb.",
"imageType": "Photograph"
},
"id": "9328597b93864c6db9c25d073f19822e",
"ingredients": [
{
"recipeSection": "",
"ingredientsList": [
{
"amount": {
"min": 1.5,
"max": 1.5
},
"name": "frozen chicken fingers or chicken tenders",
"template": "{\"min\": 1.5, \"unit\": \"lbs\", \"scale\": true, \"ingredient\": \"frozen chicken finger\", \"usCust\": false} frozen chicken fingers or chicken tenders",
"text": "1½ lbs frozen chicken fingers or chicken tenders"
},
{
"amount": {
"min": 3,
"max": 3
},
"name": "lime juice",
"suffix": "(from 2 limes), plus more as needed",
"template": "{\"min\": 3, \"unit\": \"tbsp\", \"scale\": true, \"ingredient\": \"lime juice\", \"usCust\": true} lime juice (from {\"min\": 2, \"scale\": true, \"ingredient\": \"lime\"} limes), plus more as needed",
"text": "3 tbsp lime juice (from 2 limes), plus more as needed",
"unit": "tbsp"
},
{
"amount": {
"min": 1,
"max": 1
},
"name": "fish sauce",
"suffix": ", plus more as needed",
"template": "{\"min\": 1, \"unit\": \"tbsp\", \"scale\": true, \"ingredient\": \"fish sauce\", \"usCust\": true} fish sauce, plus more as needed",
"text": "1 tbsp fish sauce, plus more as needed",
"unit": "tbsp"
},
{
"amount": {
"min": 2,
"max": 2
},
"name": "red pepper flakes",
"suffix": "(chilli flakes), plus more as needed",
"template": "{\"min\": 2, \"unit\": \"tsp\", \"scale\": true, \"ingredient\": \"red pepper flake\", \"usCust\": true} red pepper flakes (chilli flakes), plus more as needed",
"text": "2 tsp red pepper flakes (chilli flakes), plus more as needed",
"unit": "tsp"
},
{
"name": "shallot",
"suffix": ", peeled and thinly sliced",
"template": "{\"min\": 1, \"scale\": true, \"ingredient\": \"shallot\"} shallot, peeled and thinly sliced",
"text": "1 shallot, peeled and thinly sliced"
},
{
"amount": {
"min": 1.5,
"max": 1.5
},
"name": "herb leaves",
"suffix": "(cilantro, mint, dill and/or basil), torn if large",
"template": "{\"min\": 1.5, \"unit\": \"cups\", \"scale\": true, \"ingredient\": \"herb leaf\", \"usCust\": true} herb leaves (cilantro, mint, dill and/or basil), torn if large",
"text": "1½ cups herb leaves (cilantro, mint, dill and/or basil), torn if large",
"unit": "cups"
},
{
"name": "Cooked rice",
"suffix": ", lettuce leaves or cucumber, to serve",
"template": "Cooked rice, lettuce leaves or cucumber, to serve",
"text": "Cooked rice, lettuce leaves or cucumber, to serve"
}
]
}
],
"instructions": [
{
"descriptionTemplate": "Heat the oven to {\"temperatureC\": 220, \"temperatureFanC\": 200, \"temperatureF\": 425, \"gasMark\": 7} and place a sheet pan in the oven to heat.",
"description": "Heat the oven to 220C (200C fan)/425F/gas mark 7 and place a sheet pan in the oven to heat."
},
{
"descriptionTemplate": "Add the chicken fingers and roast, flipping halfway through, for 20 to 25 minutes, until very crisp.",
"description": "Add the chicken fingers and roast, flipping halfway through, for 20 to 25 minutes, until very crisp."
},
{
"descriptionTemplate": "Let the chicken cool slightly, then roughly chop into bite-size pieces. Transfer to a large bowl.",
"description": "Let the chicken cool slightly, then roughly chop into bite-size pieces. Transfer to a large bowl."
},
{
"descriptionTemplate": "Add the lime juice, fish sauce, crushed red pepper and shallot and stir to coat.",
"description": "Add the lime juice, fish sauce, crushed red pepper and shallot and stir to coat."
},
{
"descriptionTemplate": "Add the herbs and toss to combine.",
"description": "Add the herbs and toss to combine."
},
{
"descriptionTemplate": "Taste and add more fish sauce, lime juice or red pepper flakes until savory, tart and a little spicy.",
"description": "Taste and add more fish sauce, lime juice or red pepper flakes until savory, tart and a little spicy."
},
{
"descriptionTemplate": "Eat immediately with any combination of rice, lettuce leaves and cucumbers.",
"description": "Eat immediately with any combination of rice, lettuce leaves and cucumbers."
}
],
"isAppReady": false,
"mealTypeIds": [
"dinner",
"quick",
"easy",
"midweek"
],
"serves": [
{
"amount": {
"min": 4,
"max": 4
},
"text": "Serves 4",
"unit": "people"
}
],
"suitableForDietIds": [],
"techniquesUsedIds": [],
"timings": [
{
"durationInMins": {
"min": 5,
"max": 5
},
"qualifier": "prep-time",
"text": "Prep 5 min"
},
{
"durationInMins": {
"min": 30,
"max": 30
},
"qualifier": "cook-time",
"text": "Cook 30 min"
}
],
"title": "Fried chicken larb",
"utensilsAndApplianceIds": []
}"""

    @Test
    fun `should render a US customary recipe`() {
        val densityTable = DensityTable("test", HashMap(), HashMap())
        val session = RenderSession(densityTable)

        val recipe: RecipeV3 = Json.decodeFromString(usCustomaryRecipeFixture)

        val scaledRecipe = session.renderRecipe(recipe, 1.0f, MeasuringSystem.USCustomary)
        //The data we got back has some extra HTML tags for formatting. Remove these so we can compare the values (which is what matters)
        assertEquals(recipe, stripExtraHTML(scaledRecipe))

        val metricRecipe = session.renderRecipe(recipe, 1.0f, MeasuringSystem.Metric)
        val jsonStr = Json.encodeToString(metricRecipe)
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
    fun `renderRecipe populates styled ingredientWithoutSuffix without suffix text`() {
        val recipeTemplate = RecipeV3(
            id = "test-recipe",
            ingredients = listOf(
                IngredientsList(
                    ingredientsList = listOf(
                        IngredientItem(
                            template = """{"min": 2, "unit": "tsp", "scale": true} (heaped) red miso paste (white will work, too)""",
                            suffix = "(white will work, too)"
                        ),
                        IngredientItem(
                            template = """{"min": 150, "unit": "ml", "scale": true} single cream""",
                            suffix = ""
                        ),
                        IngredientItem(
                            template = """{"min": 2, "scale": true} onions, peeled and roughly sliced""",
                            suffix = "peeled and roughly sliced"
                        ),
                        IngredientItem(
                            template = """{"min": 150, "unit": "g", "scale": true} egg; small""",
                            suffix = "small"
                        ),
                        IngredientItem(
                            template = """{"min": 150, "unit": "g", "scale": true} mayonnaise (homemade or shop-bought)""",
                            suffix = "homemade or shop-bought)"
                        )
                    )
                )
            )
        )
        val session = RenderSession(DensityTable("test", HashMap(), HashMap()))

        val ingredients = session.renderRecipe(recipeTemplate, 1.0f, MeasuringSystem.Metric)
            .ingredients
            ?.first()
            ?.ingredientsList

        assertEquals("<strong>2 tsp </strong>(heaped)<strong> red miso paste</strong>", ingredients?.get(0)?.ingredientWithoutSuffix)
        assertEquals("<strong>150 ml single cream</strong>", ingredients?.get(1)?.ingredientWithoutSuffix)
        assertEquals("<strong>2 onions</strong>", ingredients?.get(2)?.ingredientWithoutSuffix)
        assertEquals("<strong>150 g egg</strong>", ingredients?.get(3)?.ingredientWithoutSuffix)
        assertEquals("<strong>150 g mayonnaise</strong>", ingredients?.get(4)?.ingredientWithoutSuffix)
    }

    @Suppress("DEPRECATION")
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
            val result = renderSession.renderQuantity(placeholder, factor, measuringSystem, MeasuringSystem.Metric)

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
        val result = renderSession.renderQuantity(placeholder, factor, measuringSystem, MeasuringSystem.Metric)

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
        val result = renderSession.renderQuantity(placeholder, factor, measuringSystem, MeasuringSystem.Metric)

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
        val result = renderSession.renderQuantity(placeholder, factor, measuringSystem, MeasuringSystem.Metric)

        // Assert
        assertEquals("2 lbs", result)
    }

    @Test
    fun `should convert metric cups into imperial cups`() {
        val densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap())
        val templateSession = RenderSession(densityTable)

        val placeholder = QuantityPlaceholder(
            min = 3f,
            max = 4f,
            unit = "cups",
            scale = true,
            ingredient = "milk",
            usCust = true
        )

        val result = templateSession.renderQuantity(
            placeholder,
            1.0f,
            MeasuringSystem.USCustomary,
            MeasuringSystem.Metric
        )

        assertEquals("3¼-4¼ cups", result)
    }

    @Test
    fun `should convert imperial cups into imperial cups - passthrough`() {
        val densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap())
        val templateSession = RenderSession(densityTable)

        val placeholder = QuantityPlaceholder(
            min = 3f,
            max = 4f,
            unit = "cups",
            scale = true,
            ingredient = "milk",
            usCust = true
        )

        val result = templateSession.renderQuantity(
            placeholder,
            1.0f,
            MeasuringSystem.USCustomary,
            MeasuringSystem.USCustomary
        )

        assertEquals("3-4 cups", result)
    }

    @Test
    fun `should convert imperial cups into metric cups`() {
        val densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap())
        val templateSession = RenderSession(densityTable)

        val placeholder = QuantityPlaceholder(
            min = 3f,
            max = 4f,
            unit = "cups",
            scale = true,
            ingredient = "milk",
            usCust = true
        )

        val result = templateSession.renderQuantity(
            placeholder,
            1.0f,
            MeasuringSystem.Metric,
            MeasuringSystem.USCustomary
        )

        assertEquals("710-946 ml", result)
    }

    @Test
    fun `test scale and terminology conversion for eggplant to aubergine with density`() {
        // Arrange
        val densityTable = DensityTable(preparedAt = "test", HashMap(), HashMap())
        val session = RenderSession(densityTable)
        val recipeTemplate = RecipeV3(
            id = "test-recipe",
            ingredients = listOf(
                IngredientsList(
                    ingredientsList = listOf(
                        IngredientItem(
                            template = """{"min": 1, "unit": "piece", "scale": true} of aubergine""",
                        )
                    )
                )
            )
        )
        val expectedRecipe = RecipeV3(
            id = "test-recipe",
            ingredients = listOf(
                IngredientsList(
                    ingredientsList = listOf(
                        IngredientItem(
                            template = """{"min": 1, "unit": "piece", "scale": true} of eggplant""",
                            text = "<strong>1 piece of eggplant</strong>"
                        )
                    )
                )
            )
        )

        // Act
        val scaledRecipe = session.renderRecipe(recipeTemplate, 1.0f, MeasuringSystem.Metric)

        val expectedRecipesTexts = expectedRecipe.ingredients?.forEach { ingredientsList -> ingredientsList.ingredientsList?.forEach { ingredientItem -> ingredientItem.text } }
        val scaledRecipesTexts = scaledRecipe.ingredients?.forEach { ingredientsList -> ingredientsList.ingredientsList?.forEach { ingredientItem -> ingredientItem.text } }

        // Assert
        assertEquals(expectedRecipesTexts, scaledRecipesTexts)
    }

    @Test
    fun `renderRecipeForTerminology converts only the requested enum section`() {
        val session = RenderSession(
            densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap()),
            terminologyTable = com.gu.recipe.terminology.TerminologyTable(
                terminologyMap = mapOf(
                    "aubergine" to TerminologyEntry(id = 1, ukTerm = "aubergine", usTerm = "eggplant", block = emptyList()),
                    "icing sugar" to TerminologyEntry(id = 2, ukTerm = "icing sugar",usTerm = "powdered sugar", block = emptyList())
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
                terminologyMap = mapOf("aubergine" to TerminologyEntry(id = 1, ukTerm = "aubergine", usTerm = "eggplant", block = emptyList()))
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
                terminologyMap = mapOf("aubergine" to TerminologyEntry(id = 1, ukTerm = "aubergine", usTerm = "eggplant", block = emptyList()))
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
    fun `applyTerminologyToRecipeTitle converts title when target measuring system is US and terminology conversion is enabled`() {
        val session = RenderSession(
            densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap()),
            terminologyTable = com.gu.recipe.terminology.TerminologyTable(
                terminologyMap = mapOf("aubergine" to TerminologyEntry(id = 1, ukTerm = "aubergine", usTerm = "eggplant", block = emptyList()))
            ),
            convertTerminologies = true
        )

        assertEquals("eggplant tart", session.applyTerminologyToRecipeTitle("aubergine tart", MeasuringSystem.USCombined))
    }

    @Test
    fun `applyTerminologyToRecipeTitle returns original title when target measuring system is not US`() {
        val session = RenderSession(
            densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap()),
            terminologyTable = com.gu.recipe.terminology.TerminologyTable(
                terminologyMap = mapOf("aubergine" to TerminologyEntry(id = 1, ukTerm = "aubergine", usTerm = "eggplant", block = emptyList()))
            ),
            convertTerminologies = true
        )

        assertEquals("aubergine tart", session.applyTerminologyToRecipeTitle("aubergine tart", MeasuringSystem.Metric))
    }

    @Test
    fun `applyTerminologyToRecipeTitle returns original title when terminology conversion is disabled for US measuring system`() {
        val session = RenderSession(
            densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap()),
            terminologyTable = com.gu.recipe.terminology.TerminologyTable(
                terminologyMap = mapOf("aubergine" to TerminologyEntry(id = 1, ukTerm = "aubergine", usTerm = "eggplant", block = emptyList()))
            ),
            convertTerminologies = false
        )

        assertEquals("aubergine tart", session.applyTerminologyToRecipeTitle("aubergine tart", MeasuringSystem.USCustomary))
    }

    @Test
    fun `applyTerminologyToRecipeTitle returns original title when conversion does not convert`() {
        val session = RenderSession(
            densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap()),
            terminologyTable = com.gu.recipe.terminology.TerminologyTable(
                terminologyMap = mapOf("aubergine" to TerminologyEntry(id = 1, ukTerm = "aubergine", usTerm = "eggplant", block = emptyList()))
            ),
            convertTerminologies = true
        )

        assertEquals("eggplant tart", session.applyTerminologyToRecipeTitle("eggplant tart", MeasuringSystem.USCustomary))
    }

    @Test
    fun `applyTerminology is case now sensitive and replaces whole terms with captilization of 1st letter if found so`() {
        val session = RenderSession(
            densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap()),
            terminologyTable = com.gu.recipe.terminology.TerminologyTable(
                terminologyMap = mapOf("aubergine" to TerminologyEntry(id = 1, ukTerm = "aubergine", usTerm = "eggplant", block = emptyList()))
            )
        )

        assertEquals("Roast the Eggplant", session.applyTerminology("Roast the AUBERGINE"))
        assertEquals("aubergines are great", session.applyTerminology("aubergines are great"))
    }

    @Test
    fun `applyTerminology prefers longer terminology matches before shorter ones`() {
        val session = RenderSession(
            densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap()),
            terminologyTable = com.gu.recipe.terminology.TerminologyTable(
                terminologyMap = mapOf(
                    "sugar" to TerminologyEntry(id = 1, ukTerm = "sugar", usTerm = "sweetener", block = emptyList()),
                    "icing sugar" to TerminologyEntry(id = 2, ukTerm = "icing sugar", usTerm = "powdered sugar", block = emptyList())
                )
            )
        )

        assertEquals("powdered sugar", session.applyTerminology("icing sugar"))
    }

    @Test
    fun `should return scaledRecipe when measuringSystem equals sourceMeasuringSystem`() {
        val session = RenderSession(
            densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap()),
            terminologyTable = com.gu.recipe.terminology.TerminologyTable(
                terminologyMap = mapOf("aubergine" to TerminologyEntry(id = 1, ukTerm = "aubergine", usTerm = "eggplant", block = emptyList()))
            )
        )
        val scaledRecipe = RecipeV3(
            id = "test-recipe",
            ingredients = listOf(
                IngredientsList(
                    ingredientsList = listOf(
                        IngredientItem(text = "1 aubergine")
                    )
                )
            )
        )
        val result = session.renderRecipe(
            scaledRecipe,
            1.0f,
            MeasuringSystem.Metric
        )
        assertEquals(scaledRecipe, result)
    }

    @Test
    fun `should convert terminology when measuringSystem does not equal sourceMeasuringSystem`() {
        val session = RenderSession(
            densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap()),
            terminologyTable = com.gu.recipe.terminology.TerminologyTable(
                terminologyMap = mapOf("aubergine" to TerminologyEntry(id = 1, ukTerm = "aubergine", usTerm = "eggplant", block = emptyList()))
            )
        )
        val scaledRecipe = RecipeV3(
            id = "test-recipe",
            ingredients = listOf(
                IngredientsList(
                    ingredientsList = listOf(
                        IngredientItem(text = "1 aubergine")
                    )
                )
            )
        )
        val expectedRecipe = RecipeV3(
            id = "test-recipe",
            ingredients = listOf(
                IngredientsList(
                    ingredientsList = listOf(
                        IngredientItem(text = "1 eggplant")
                    )
                )
            )
        )
        val result = session.renderRecipe(
            scaledRecipe,
            1.0f,
            MeasuringSystem.USCustomary
        )
        assertEquals(expectedRecipe, result)
    }

    @Test
    fun `renderRecipeForTerminology ALL converts every section with block list into consideration`() {
        val session = RenderSession(
            densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap()),
            terminologyTable = com.gu.recipe.terminology.TerminologyTable(
                terminologyMap = mapOf(
                    "sponge" to TerminologyEntry(
                        id = 1,
                        ukTerm = "sponge",
                        usTerm = "cake",
                        block = listOf(
                            "victoria sponge",
                            "sponge cake",
                            "bake sponge",
                            "baked sponge",
                            "sponge fingers"
                        )
                    )
                )
            )
        )
        val recipe = RecipeV3(
            id = "test-recipe",
            title = "Passionfruit sponge cake",
            description = "There are so many uses for a great sponge cake. Browning the butter before you mix gives this sponge a lovely nutty, buttery quality. Serve with sponge cake and extra sponge on the side.",
            ingredients = listOf(),
            instructions = listOf(
                Instruction(description = "Put sponge in a plate"),
                Instruction(description = "Get slice of Victoria sponge in a serving plate")
            )
        )

        val rendered = session.renderRecipeForTerminology(recipe, TerminologySection.ALL)

        assertEquals("Passionfruit sponge cake", rendered.title)
        assertEquals("There are so many uses for a great sponge cake. Browning the butter before you mix gives this cake a lovely nutty, buttery quality. Serve with sponge cake and extra cake on the side.", rendered.description)
        assertEquals("Put cake in a plate", rendered.instructions?.get(0)?.description)
        assertEquals("Get slice of Victoria sponge in a serving plate", rendered.instructions?.get(1)?.description)
    }
}