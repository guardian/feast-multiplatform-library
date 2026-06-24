package com.gu.recipe.core.graphql.model

data class GraphQlRecipe(
    val bookCredit: String? = null,
    val byline: List<String>? = null,
    val canonicalArticle: String? = null,
    val celebrationIds: List<String>? = null,
    val composerId: String? = null,
    val contributors: List<String>? = null,
    val cuisineIds: List<String>? = null,
    val description: String? = null,
    val difficultyLevel: String? = null,
    val featuredImage: GraphQlImage? = null,
    val id: String,
    val ingredients: List<GraphQlIngredientsList>? = null,
    val instructions: List<GraphQlInstruction>? = null,
    val isAppReady: Boolean? = null,
    val mealTypeIds: List<String>? = null,
    val serves: List<GraphQlServes>? = null,
    val suitableForDietIds: List<String>? = null,
    val techniquesUsedIds: List<String>? = null,
    val timings: List<GraphQlTiming>? = null,
    val title: String? = null,
    val utensilsAndApplianceIds: List<String>? = null,
    val webPublicationDate: String? = null,
)

data class GraphQlImage(
    val caption: String? = null,
    val cropId: String? = null,
    val mediaApiUrl: String? = null,
    val mediaId: String? = null,
    val photographer: String? = null,
    val source: String? = null,
    val url: String,
)

data class GraphQlIngredientsList(
    val ingredientsList: List<GraphQlIngredientItem>? = null,
    val recipeSection: String? = null,
)

data class GraphQlIngredientItem(
    val name: String? = null,
    val optional: Boolean? = null,
    val template: String? = null,
    val text: String? = null,
    val unit: String? = null,
)

data class GraphQlInstruction(
    val description: String,
    val descriptionTemplate: String? = null,
)

data class GraphQlServes(
    val text: String? = null,
    val unit: String? = null,
)

data class GraphQlTiming(
    val durationInMins: GraphQlRange? = null,
    val qualifier: String? = null,
    val text: String? = null,
)

data class GraphQlRange(
    val max: Double? = null,
    val min: Double? = null,
)

