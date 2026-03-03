package com.gu.recipe

import com.gu.recipe.density.DensityTable
import com.gu.recipe.generated.IngredientItem
import com.gu.recipe.generated.IngredientsList
import com.gu.recipe.generated.RecipeV3
import com.gu.recipe.unit.MeasuringSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class CombineIngredientsTest {
    private fun recipeWithIngredients(vararg templates: String): RecipeV3 {
        return RecipeV3(
            id = "test-recipe",
            ingredients = listOf(
                IngredientsList(
                    ingredientsList = templates.map { template ->
                        IngredientItem(template = template)
                    }
                )
            )
        )
    }

    @Test
    fun `combine ingredients with same unit`() {
        val recipe1 = recipeWithIngredients(
            """{"min": 2, "scale": true, "ingredient": "egg"} egg"""
        )
        val recipe2 = recipeWithIngredients(
            """{"min": 6, "scale": true, "ingredient": "egg"} egg"""
        )

        val session = TemplateSession(DensityTable("test", HashMap(), HashMap()))
        val combined = session.combineIngredients(
            recipes = listOf(recipe1, recipe2),
            measuringSystem = MeasuringSystem.Metric
        )

        assertEquals(listOf("8 egg"), combined)
    }

    @Test
    fun `keep different units separate`() {
        val recipe1 = recipeWithIngredients(
            """{"min": 100, "unit": "g", "scale": true, "ingredient": "flour"} flour"""
        )
        val recipe2 = recipeWithIngredients(
            """{"min": 1, "unit": "cup", "scale": true, "ingredient": "flour"} flour"""
        )

        val session = TemplateSession(DensityTable("test", HashMap(), HashMap()))
        val combined = session.combineIngredients(
            recipes = listOf(recipe1, recipe2),
            measuringSystem = MeasuringSystem.Metric
        )

        assertEquals(listOf("1 cup flour", "100 g flour"), combined)
    }

    @Test
    fun `combine ranges by summing min and max separately`() {
        val recipe1 = recipeWithIngredients(
            """{"min": 1, "max": 2, "unit": "tbsp", "scale": true, "ingredient": "sugar"} sugar"""
        )
        val recipe2 = recipeWithIngredients(
            """{"min": 2, "max": 3, "unit": "tbsp", "scale": true, "ingredient": "sugar"} sugar"""
        )

        val session = TemplateSession(DensityTable("test", HashMap(), HashMap()))
        val combined = session.combineIngredients(
            recipes = listOf(recipe1, recipe2),
            measuringSystem = MeasuringSystem.Metric
        )

        assertEquals(listOf("3-5 tbsp sugar"), combined)
    }

    @Test
    fun `include count when requested and more than one item`() {
        val recipe1 = recipeWithIngredients(
            """{"min": 2, "scale": true, "ingredient": "egg"} egg"""
        )
        val recipe2 = recipeWithIngredients(
            """{"min": 4, "scale": true, "ingredient": "egg"} egg"""
        )
        val recipe3 = recipeWithIngredients(
            """{"min": 100, "unit": "g", "scale": true, "ingredient": "flour"} flour"""
        )

        val session = TemplateSession(DensityTable("test", HashMap(), HashMap()))
        val combined = session.combineIngredients(
            recipes = listOf(recipe1, recipe2, recipe3),
            measuringSystem = MeasuringSystem.Metric,
            includeCount = true
        )

        assertEquals(listOf("6 egg (2)", "100 g flour"), combined)
    }

    @Test
    fun `combine unitless ingredients`() {
        val recipe1 = recipeWithIngredients(
            """{"min": 3, "scale": true, "ingredient": "egg"} egg"""
        )
        val recipe2 = recipeWithIngredients(
            """{"min": 5, "scale": true, "ingredient": "egg"} egg"""
        )

        val session = TemplateSession(DensityTable("test", HashMap(), HashMap()))
        val combined = session.combineIngredients(
            recipes = listOf(recipe1, recipe2),
            measuringSystem = MeasuringSystem.Metric
        )

        assertEquals(listOf("8 egg"), combined)
    }

    @Test
    fun `skip ingredients without template`() {
        val recipeWithoutTemplate = RecipeV3(
            id = "test",
            ingredients = listOf(
                IngredientsList(
                    ingredientsList = listOf(
                        IngredientItem(text = "2 eggs", template = null)
                    )
                )
            )
        )
        val recipeWithTemplate = recipeWithIngredients(
            """{"min": 3, "scale": true, "ingredient": "egg"} egg"""
        )

        val session = TemplateSession(DensityTable("test", HashMap(), HashMap()))
        val combined = session.combineIngredients(
            recipes = listOf(recipeWithoutTemplate, recipeWithTemplate),
            measuringSystem = MeasuringSystem.Metric
        )

        // Only the one with template is included
        assertEquals(listOf("3 egg"), combined)
    }

    @Test
    fun `skip ingredients without normalized name`() {
        val recipe = recipeWithIngredients(
            """{"min": 100, "unit": "g", "scale": true} some flour"""
        )

        val session = TemplateSession(DensityTable("test", HashMap(), HashMap()))
        val combined = session.combineIngredients(
            recipes = listOf(recipe),
            measuringSystem = MeasuringSystem.Metric
        )

        // Skipped because no "ingredient" field
        assertEquals(emptyList(), combined)
    }

    @Test
    fun `empty input returns empty list`() {
        val session = TemplateSession(DensityTable("test", HashMap(), HashMap()))
        val combined = session.combineIngredients(
            recipes = emptyList(),
            measuringSystem = MeasuringSystem.Metric
        )

        assertEquals(emptyList(), combined)
    }
}
