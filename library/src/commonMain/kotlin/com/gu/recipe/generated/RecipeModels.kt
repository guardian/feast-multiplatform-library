// To parse the JSON, install kotlin's serialization plugin and do:
//
// val json     = Json { allowStructuredMapKeys = true }
// val recipeV3 = json.parse(RecipeV3.serializer(), jsonString)

package com.gu.recipe.generated

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 * Complete recipe with metadata, ingredients, instructions, and categorization
 */
@Serializable
data class RecipeV3 (
    /**
     * Credit to cookbook or publication source
     */
    val bookCredit: String? = null,

    /**
     * Author or chef attribution
     */
    val byline: List<String>? = null,

    /**
     * URL or identifier of the canonical article
     */
    val canonicalArticle: String? = null,

    /**
     * Identifiers for celebrations or holidays associated with the recipe
     */
    val celebrationIds: List<String?>? = null,

    /**
     * Call-to-action items for commerce links
     */
    val commerceCtas: List<CommerceCta>? = null,

    /**
     * Identifier for the content management system
     */
    val composerId: String? = null,

    /**
     * List of people who contributed to the recipe
     */
    val contributors: List<String>? = null,

    /**
     * Identifiers for cuisine types
     */
    val cuisineIds: List<String?>? = null,

    /**
     * Description or summary of the recipe
     */
    val description: String? = null,

    /**
     * Difficulty level of the recipe (e.g., 'easy', 'medium', 'hard')
     */
    val difficultyLevel: String? = null,

    /**
     * Main image for the recipe
     */
    val featuredImage: Image? = null,

    /**
     * Unique identifier for the recipe
     */
    val id: String,

    /**
     * Ingredients organized by sections
     */
    val ingredients: List<IngredientsList>? = null,

    /**
     * Step-by-step cooking instructions
     */
    val instructions: List<Instruction>? = null,

    /**
     * Whether the recipe is ready for app display
     */
    val isAppReady: Boolean? = null,

    /**
     * Identifiers for meal types (breakfast, lunch, dinner, etc.)
     */
    val mealTypeIds: List<String?>? = null,

    /**
     * Information about how many people the recipe serves
     */
    val serves: List<Serves>? = null,

    /**
     * Identifiers for dietary restrictions the recipe accommodates
     */
    val suitableForDietIds: List<String?>? = null,

    /**
     * Identifiers for cooking techniques used in the recipe
     */
    val techniquesUsedIds: List<String?>? = null,

    /**
     * Various timing information (prep, cook, total, etc.)
     */
    val timings: List<Timing>? = null,

    /**
     * Title of the recipe
     */
    val title: String? = null,

    /**
     * Identifiers for required utensils and appliances
     */
    val utensilsAndApplianceIds: List<String?>? = null,

    /**
     * Date when the recipe was published on the web
     */
    val webPublicationDate: String? = null
)

/**
 * Schema for commerce call-to-action details
 */
@Serializable
data class CommerceCta (
    /**
     * Name of the sponsor
     */
    val sponsorName: String,

    /**
     * Territory associated with the campaign
     */
    val territory: String,

    /**
     * The URL to point to
     */
    val url: String
)

/**
 * Image metadata including URL, media identifiers, and attribution
 */
@Serializable
data class Image (
    /**
     * Caption or description of the image
     */
    val caption: String? = null,

    /**
     * Identifier for the specific crop of the image
     */
    val cropId: String,

    /**
     * Type or category of the image
     */
    val imageType: String? = null,

    /**
     * API URL for accessing the media
     */
    val mediaApiUrl: String? = null,

    /**
     * Unique identifier for the media
     */
    val mediaId: String,

    /**
     * Name of the photographer
     */
    val photographer: String? = null,

    /**
     * Source of the image
     */
    val source: String? = null,

    /**
     * The URL of the image
     */
    val url: String
)

/**
 * A section of ingredients with an optional section name
 */
@Serializable
data class IngredientsList (
    /**
     * List of ingredients in this section
     */
    val ingredientsList: List<IngredientItem>? = null,

    /**
     * Name of the recipe section (e.g., 'For the sauce', 'For the garnish')
     */
    val recipeSection: String? = null
)

/**
 * Individual ingredient item with amount, unit, and optional modifiers
 */
@Serializable
data class IngredientItem (
    /**
     * Amount of the ingredient as a range or null
     */
    val amount: Range? = null,

    /**
     * Unique identifier for the ingredient
     */
    val ingredientId: String? = null,

    /**
     * Name of the ingredient
     */
    val name: String? = null,

    /**
     * Whether the ingredient is optional
     */
    val optional: Boolean? = null,

    val prefix: String? = null,
    val suffix: String? = null,

    /**
     * The template representation of the ingredient
     */
    val template: String? = null,

    /**
     * Full text representation of the ingredient
     */
    val text: String? = null,

    /**
     * Unit of measurement for the ingredient
     */
    val unit: String? = null
) {
    fun textWithoutSuffix(): String? {
        return text?.substringBefore(",")
    }
}

/**
 * A numeric range with minimum and maximum values
 */
@Serializable
data class Range (
    /**
     * The maximum value of the range
     */
    val max: Double? = null,

    /**
     * The minimum value of the range
     */
    val min: Double? = null
)

/**
 * A single cooking instruction step with optional images
 */
@Serializable
data class Instruction (
    /**
     * Detailed description of the cooking step
     */
    val description: String,

    /**
     * The template representation of the instruction
     */
    val descriptionTemplate: String? = null,

    /**
     * Array of image URLs or identifiers for this step
     */
    val images: List<String>? = null,

    /**
     * The sequential number of this instruction step
     */
    val stepNumber: Double? = null
)

/**
 * Information about how many servings the recipe makes
 */
@Serializable
data class Serves (
    /**
     * Number of servings as a range or null
     */
    val amount: Range? = null,

    /**
     * Human-readable text representation of the serving information
     */
    val text: String? = null,

    /**
     * Unit for the serving amount (e.g., 'people', 'portions')
     */
    val unit: String
)

/**
 * Timing information for recipe preparation or cooking
 */
@Serializable
data class Timing (
    /**
     * Duration in minutes as a range or null
     */
    val durationInMins: Range? = null,

    /**
     * Type of timing (e.g., 'prep time', 'cook time', 'total time')
     */
    val qualifier: String? = null,

    /**
     * Human-readable text representation of the timing
     */
    val text: String? = null
)
