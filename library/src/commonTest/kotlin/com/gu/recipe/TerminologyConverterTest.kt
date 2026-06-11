package com.gu.recipe

import com.gu.recipe.density.DensityTable
import com.gu.recipe.generated.IngredientItem
import com.gu.recipe.generated.IngredientsList
import com.gu.recipe.generated.RecipeV3
import com.gu.recipe.terminology.TerminologyConverter
import com.gu.recipe.terminology.TerminologyTable
import com.gu.recipe.terminology.loadInternalTerminologyTable
import com.gu.recipe.terminology.setUpTerminologyTable
import com.gu.recipe.unit.MeasuringSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class TerminologyConverterTest {
    @Test
    fun `test scale and terminology conversion for eggplant to aubergine with density`() {
        // Arrange
        val densityTable = DensityTable(preparedAt = "test", HashMap(), HashMap())
        val session = TemplateSession(densityTable)
        val terminologyTableResult = loadInternalTerminologyTable()
        val terminologyTable = terminologyTableResult.getOrNull()
        val terminologyConverter = TerminologyConverter(terminologyTable ?: error("TerminologyTable is null"))

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
                            template = """{"min": 1, "unit": "piece", "scale": true} of eggplant-TEST""",//Intentionally keeping "TEST" word to check it is coming from internal fixture
                            text = "<strong>1 piece of eggplant-TEST</strong>"
                        )
                    )
                )
            )
        )

        // Act
        val scaledRecipe = session.scaleAndConvertUnitRecipe(recipeTemplate, 1.0f, MeasuringSystem.Metric)
        val convertedRecipeJson = terminologyConverter.replaceWordsInRecipeObject(scaledRecipe)

        // Assert
        assertEquals<RecipeV3>(expectedRecipe, convertedRecipeJson)
    }

    @Test
    fun `test scale and terminology conversion for aubergine with suffix`() {
        // Arrange
        val densityTable = DensityTable(preparedAt = "test", HashMap(), HashMap())
        val session = TemplateSession(densityTable)
        val terminologyTableResult = loadInternalTerminologyTable()
        val terminologyTable = terminologyTableResult.getOrNull()
        val terminologyConverter = TerminologyConverter(terminologyTable ?: error("TerminologyTable is null"))
        val recipeTemplate = RecipeV3(
            id = "test-recipe",
            ingredients = listOf(
                IngredientsList(
                    ingredientsList = listOf(
                        IngredientItem(
                            template = """{"min": 1, "max": 1, "unit": "piece", "scale": true} of aubergine""",
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
                            template = """{"min": 1, "max": 1, "unit": "piece", "scale": true} of eggplant-TEST""",
                            text = "<strong>1 piece of eggplant-TEST</strong>"
                        )
                    )
                )
            )
        )

        // Act
        val scaledRecipe = session.scaleAndConvertUnitRecipe(recipeTemplate, 1.0f, MeasuringSystem.Metric)
        val convertedRecipeJson = terminologyConverter.replaceWordsInRecipeObject(scaledRecipe)

        // Assert
        assertEquals<RecipeV3>(expectedRecipe, convertedRecipeJson)

    }
}