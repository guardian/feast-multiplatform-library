package com.gu.recipe.generated

typealias StringTemplate = String

/**
 * Image metadata including URL, media identifiers, and attribution
 */
data class Image(
    /**
     * Caption or description of the image
     */
    val caption: String? = null,

    /**
     * Identifier for the specific crop of the image
     */
    val cropID: String,

    /**
     * Type or category of the image
     */
    val imageType: String? = null,

    /**
     * API URL for accessing the media
     */
    val mediaAPIURL: String? = null,

    /**
     * Unique identifier for the media
     */
    val mediaID: String,

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
 * Individual ingredient item with amount, unit, and optional modifiers
 */
data class IngredientItem(
    /**
     * Amount of the ingredient as a range or null
     */
    val amount: RangeClass? = null,

    /**
     * Unique identifier for the ingredient
     */
    val ingredientID: String? = null,

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
     * Full text representation of the ingredient
     */
    val text: String? = null,

    /**
     * Unit of measurement for the ingredient
     */
    val unit: String? = null
)

/**
 * A numeric range with minimum and maximum values
 */
data class RangeClass(
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
 * A section of ingredients with an optional section name
 */
data class IngredientsList(
    /**
     * List of ingredients in this section
     */
    val ingredientsList: List<IngredientsListIngredientsList>? = null,

    /**
     * Name of the recipe section (e.g., 'For the sauce', 'For the garnish')
     */
    val recipeSection: String? = null
)

/**
 * Individual ingredient item with amount, unit, and optional modifiers
 */
data class IngredientsListIngredientsList(
    /**
     * Amount of the ingredient as a range or null
     */
    val amount: RangeClass? = null,

    /**
     * Unique identifier for the ingredient
     */
    val ingredientID: String? = null,

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
     * Full text representation of the ingredient
     */
    val text: String? = null,

    /**
     * Unit of measurement for the ingredient
     */
    val unit: String? = null
)

/**
 * A section of ingredients with an optional section name
 */
data class IngredientsTemplateList(
    /**
     * List of ingredients in this section
     */
    val ingredientsList: List<IngredientsTemplateListIngredientsList>? = null,

    /**
     * Name of the recipe section (e.g., 'For the sauce', 'For the garnish')
     */
    val recipeSection: String? = null
)

/**
 * Individual ingredient item with amount, unit, and optional modifiers
 */
data class IngredientsTemplateListIngredientsList(
    /**
     * Amount of the ingredient as a range or null
     */
    val amount: RangeClass? = null,

    /**
     * Unique identifier for the ingredient
     */
    val ingredientID: String? = null,

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
     * Full text representation of the ingredient
     */
    val text: String? = null,

    /**
     * Unit of measurement for the ingredient
     */
    val unit: String? = null,

    val template: String? = null
)

/**
 * A single cooking instruction step with optional images
 */
data class InstructionTemplate(
    /**
     * Detailed description of the cooking step
     */
    val descriptionTemplate: String,

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
 * A single cooking instruction step with optional images
 */
data class Instruction(
    /**
     * Detailed description of the cooking step
     */
    val description: String,

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
 * A numeric range with minimum and maximum values
 */
data class Range(
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
 * Complete recipe with metadata, ingredients, instructions, and categorization
 */
data class RecipeV2(
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
    val celebrationIDS: List<String?>? = null,

    /**
     * Identifier for the content management system
     */
    val composerID: String? = null,

    /**
     * List of people who contributed to the recipe
     */
    val contributors: List<String>? = null,

    /**
     * Identifiers for cuisine types
     */
    val cuisineIDS: List<String?>? = null,

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
    val featuredImage: ImageClass? = null,

    /**
     * Unique identifier for the recipe
     */
    val id: String,

    /**
     * Ingredients organized by sections
     */
    val ingredients: List<IngredientElement>? = null,

    /**
     * Step-by-step cooking instructions
     */
    val instructions: List<InstructionElement>? = null,

    /**
     * Whether the recipe is ready for app display
     */
    val isAppReady: Boolean? = null,

    /**
     * Identifiers for meal types (breakfast, lunch, dinner, etc.)
     */
    val mealTypeIDS: List<String?>? = null,

    /**
     * Information about how many people the recipe serves
     */
    val serves: List<ServeElement>? = null,

    /**
     * Identifiers for dietary restrictions the recipe accommodates
     */
    val suitableForDietIDS: List<String?>? = null,

    /**
     * Identifiers for cooking techniques used in the recipe
     */
    val techniquesUsedIDS: List<String?>? = null,

    /**
     * Various timing information (prep, cook, total, etc.)
     */
    val timings: List<TimingElement>? = null,

    /**
     * Title of the recipe
     */
    val title: String? = null,

    /**
     * Identifiers for required utensils and appliances
     */
    val utensilsAndApplianceIDS: List<String?>? = null,

    /**
     * Date when the recipe was published on the web
     */
    val webPublicationDate: String? = null
)

/**
 * Image metadata including URL, media identifiers, and attribution
 */
data class ImageClass(
    /**
     * Caption or description of the image
     */
    val caption: String? = null,

    /**
     * Identifier for the specific crop of the image
     */
    val cropID: String,

    /**
     * Type or category of the image
     */
    val imageType: String? = null,

    /**
     * API URL for accessing the media
     */
    val mediaAPIURL: String? = null,

    /**
     * Unique identifier for the media
     */
    val mediaID: String,

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
data class IngredientElement(
    /**
     * List of ingredients in this section
     */
    val ingredientsList: List<IngredientsListIngredientsList>? = null,

    /**
     * Name of the recipe section (e.g., 'For the sauce', 'For the garnish')
     */
    val recipeSection: String? = null
)

/**
 * A single cooking instruction step with optional images
 */
data class InstructionElement(
    /**
     * Detailed description of the cooking step
     */
    val description: String,

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
data class ServeElement(
    /**
     * Number of servings as a range or null
     */
    val amount: RangeClass? = null,

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
data class TimingElement(
    /**
     * Duration in minutes as a range or null
     */
    val durationInMins: RangeClass? = null,

    /**
     * Type of timing (e.g., 'prep time', 'cook time', 'total time')
     */
    val qualifier: String? = null,

    /**
     * Human-readable text representation of the timing
     */
    val text: String? = null
)

/**
 * Complete recipe with metadata, ingredients, instructions, and categorization
 */
data class RecipeV3(
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
    val celebrationIDS: List<String?>? = null,

    /**
     * Identifier for the content management system
     */
    val composerID: String? = null,

    /**
     * List of people who contributed to the recipe
     */
    val contributors: List<String>? = null,

    /**
     * Identifiers for cuisine types
     */
    val cuisineIDS: List<String?>? = null,

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
    val featuredImage: ImageClass? = null,

    /**
     * Unique identifier for the recipe
     */
    val id: String,

    /**
     * Ingredients organized by sections
     */
    val ingredients: List<IngredientElement>? = null,

    /**
     * Ingredients organized by sections using string templates
     */
    val ingredientsTemplate: List<IngredientsTemplateElement>? = null,

    /**
     * Step-by-step cooking instructions
     */
    val instructions: List<InstructionElement>? = null,

    /**
     * Step-by-step cooking instructions using string templates
     */
    val instructionsTemplate: List<InstructionsTemplateElement>? = null,

    /**
     * Whether the recipe is ready for app display
     */
    val isAppReady: Boolean? = null,

    /**
     * Identifiers for meal types (breakfast, lunch, dinner, etc.)
     */
    val mealTypeIDS: List<String?>? = null,

    /**
     * Information about how many people the recipe serves
     */
    val serves: List<ServeElement>? = null,

    /**
     * Identifiers for dietary restrictions the recipe accommodates
     */
    val suitableForDietIDS: List<String?>? = null,

    /**
     * Identifiers for cooking techniques used in the recipe
     */
    val techniquesUsedIDS: List<String?>? = null,

    /**
     * Various timing information (prep, cook, total, etc.)
     */
    val timings: List<TimingElement>? = null,

    /**
     * Title of the recipe
     */
    val title: String? = null,

    /**
     * Identifiers for required utensils and appliances
     */
    val utensilsAndApplianceIDS: List<String?>? = null,

    /**
     * Date when the recipe was published on the web
     */
    val webPublicationDate: String? = null
)

/**
 * A section of ingredients with an optional section name
 */
data class IngredientsTemplateElement(
    /**
     * List of ingredients in this section
     */
    val ingredientsList: List<IngredientsTemplateIngredientsList>? = null,

    /**
     * Name of the recipe section (e.g., 'For the sauce', 'For the garnish')
     */
    val recipeSection: String? = null
)

/**
 * Individual ingredient item with amount, unit, and optional modifiers
 */
data class IngredientsTemplateIngredientsList(
    /**
     * Amount of the ingredient as a range or null
     */
    val amount: RangeClass? = null,

    /**
     * Unique identifier for the ingredient
     */
    val ingredientID: String? = null,

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
     * Full text representation of the ingredient
     */
    val text: String? = null,

    /**
     * Unit of measurement for the ingredient
     */
    val unit: String? = null,

    val template: String? = null
)

/**
 * A single cooking instruction step with optional images
 */
data class InstructionsTemplateElement(
    /**
     * Detailed description of the cooking step
     */
    val descriptionTemplate: String,

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
data class Serves(
    /**
     * Number of servings as a range or null
     */
    val amount: RangeClass? = null,

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
data class Timing(
    /**
     * Duration in minutes as a range or null
     */
    val durationInMins: RangeClass? = null,

    /**
     * Type of timing (e.g., 'prep time', 'cook time', 'total time')
     */
    val qualifier: String? = null,

    /**
     * Human-readable text representation of the timing
     */
    val text: String? = null
)
