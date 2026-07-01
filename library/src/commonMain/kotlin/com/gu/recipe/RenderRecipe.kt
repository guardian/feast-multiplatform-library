package com.gu.recipe

import com.gu.recipe.FormatUtils.applySmartPunctuation
import com.gu.recipe.density.DensityTable
import com.gu.recipe.density.loadDensityTable
import com.gu.recipe.density.loadInternalDensityTable
import com.gu.recipe.generated.*
import com.gu.recipe.template.OvenTemperaturePlaceholder
import com.gu.recipe.template.ParsedTemplate
import com.gu.recipe.template.QuantityPlaceholder
import com.gu.recipe.template.TemplateConst
import com.gu.recipe.template.TemplateElement
import com.gu.recipe.template.parseTemplate
import com.gu.recipe.terminology.TerminologyTable
import com.gu.recipe.terminology.loadInternalTerminologyTable
import com.gu.recipe.terminology.loadTerminologyTable
import com.gu.recipe.unit.MeasuringSystem
import com.gu.recipe.unit.UnitConversions
import com.gu.recipe.unit.UnitType
import com.gu.recipe.unit.Units
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.math.max

private val NON_BOLD_REGEX = Regex("""\([^()]*\)| • """)
private const val MARKER = "\u0000"

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class TerminologySection {
    ALL,
    TITLE,
    DESCRIPTION,
    INGREDIENTS,
    INSTRUCTIONS,
}

private fun splitBeforeSuffix(value: String): Pair<String, String?> {
    val index = value.indexOfAny(charArrayOf(',', ';', '('))
    return if (index != -1) {
        value.take(index) to value.drop(index)
    } else {
        value.trim() to null
    }
}

internal fun wrapWithStrongTag(value: String): String {
    // Rule: bold text runs until the first comma/semicolon; anything after that stays plain text,
    // and any parenthesized groups within the boldable portion also remain plain while surrounding
    // text may still be bold.
    val suffixStart = value.indexOfAny(charArrayOf(',', ';'))
    val boldPart = if (suffixStart >= 0) value.take(suffixStart) else value
    val plainSuffix = if (suffixStart >= 0) value.drop(suffixStart) else ""

    val result = NON_BOLD_REGEX.replace(boldPart) { "$MARKER${it.value}$MARKER" }
        .split(MARKER)
        .joinToString("") { chunk ->
            when {
                chunk.isEmpty() -> ""
                chunk.startsWith("(") -> chunk
                chunk.isBlank() || chunk == " • " -> chunk
                else -> "<strong>$chunk</strong>"
            }
        }

    return result + plainSuffix
}

@OptIn(ExperimentalJsExport::class)
@JsExport
class RenderSession(private val densityTable: DensityTable, private val terminologyTable: TerminologyTable? = null, private val convertTerminologies: Boolean? = null) {
    internal fun renderOvenTemperature(element: OvenTemperaturePlaceholder): String {
        val fanTempC = element.temperatureFanC?.let {
            if (element.temperatureC == null) {
                "${element.temperatureFanC}C fan"
            } else {
                " (${element.temperatureFanC}C fan)"
            }
        }
        return listOfNotNull(
            element.temperatureC?.let { "${element.temperatureC}C" },
            fanTempC,
            element.temperatureF?.let { "/${it}F" },
            element.gasMark?.let { "/gas mark ${FormatUtils.formatToNearestFraction(it)}" }
        ).joinToString("")
    }

    internal fun renderQuantity(element: QuantityPlaceholder, factor: Float, measuringSystem: MeasuringSystem.MeasuringSystemInternal): String {
        var amount = Amount(
            min = element.min,
            max = if (element.min != element.max) element.max else null,
            unit = element.unit?.let { Units.findRecipeUnit(it) },
            //Specific override for butter - this should definitely be in cups but CMS data usually indicates it should be in oz.
            //We will fix the upstream CMS but need to move ahead with testing now.
            usCust = if(element.ingredient=="butter") true else element.usCust,
        )

        val factorToUse = if (!element.scale) 1f else factor

        val density = element.ingredient?.let { densityTable.densityForNorm(it) }

        // Butter is.... special :shrug:
        val targetSystem = if(element.ingredient=="butter" && measuringSystem== MeasuringSystem.Imperial) {
            MeasuringSystem.Butter
        } else {
            measuringSystem
        }
        amount = UnitConversions.convertUnitSystemAndScale(amount, targetSystem, factorToUse, density)

        val decimals = when (amount.unit) {
            Units.GRAM, Units.MILLILITRE, Units.MILLIMETRE, Units.US_TEASPOON, Units.METRIC_TEASPOON, Units.US_TABLESPOON, Units.METRIC_TABLESPOON -> 0
            Units.CENTIMETRE, Units.INCH -> 1
            else -> 1
        }

        val fraction = when (amount.unit) {
            Units.CENTILITRE, Units.MILLILITRE, Units.CENTIMETRE, Units.GRAM, Units.KILOGRAM, Units.MILLIMETRE -> false
            else -> true // generally for Units.METRIC_TEASPOON, Units.METRIC_TABLESPOON, Units.US_TABLESPOON, Units.US_TEASPOON
        }

        val unitString = if (amount.unit != null) {
            if (max(amount.min, amount.max ?: amount.min) >= 1.125f) { //need to offset from exactly one, so that when rounding a value below 1/8 we don't get "1 cups
                " ${amount.unit.symbolPlural}"
            } else {
                " ${amount.unit.symbol}"
            }
        } else ""

        return listOfNotNull(
            FormatUtils.formatAmount(amount.min, decimals, fraction),
            amount.max?.let { "-" + FormatUtils.formatAmount(it, decimals, fraction) },
            unitString,
        ).joinToString("")
    }

    internal fun renderTemplateElement(
        element: TemplateElement,
        factor: Float,
        measuringSystem: MeasuringSystem
    ): String {
        return when (element) {
            is TemplateConst -> element.value
            is QuantityPlaceholder -> {

                when (measuringSystem) {
                    is MeasuringSystem.Metric,
                    is MeasuringSystem.Imperial,
                    is MeasuringSystem.USCustomary,
                    is MeasuringSystem.Butter -> renderQuantity(element, factor, measuringSystem)
                    is MeasuringSystem.USCustomaryWithMetric,
                    is MeasuringSystem.USCustomaryWithImperial -> {
                        renderQuantity(element, factor, MeasuringSystem.USCustomary)
                    }
                    is MeasuringSystem.USCombined -> {
                        val unit = element.unit?.let { Units.findRecipeUnit(it) }
                        if( unit==null ||
                            unit==Units.METRIC_TEASPOON ||
                            unit==Units.METRIC_TABLESPOON ||
                            unit==Units.US_TEASPOON ||
                            unit==Units.US_TABLESPOON) {
                                renderQuantity(element, factor, MeasuringSystem.USCustomary)
                        } else {
                            val usCustomaryPart = renderQuantity(element, factor, MeasuringSystem.USCustomary)
                            val imperialPart = if (unit == Units.FLUID_OUNCE) { // Skip rendering fluid ounces
                                null
                            } else if (unit.unitType == UnitType.VOLUME) {
                                usCustomaryPart
                            } else {
                                renderQuantity(element, factor, MeasuringSystem.Imperial)
                            }

                            if (usCustomaryPart == imperialPart || imperialPart == null) {
                                usCustomaryPart
                            } else {
                                imperialPart + " • " + usCustomaryPart
                            }
                        }
                    }
                }
            }
            is OvenTemperaturePlaceholder -> renderOvenTemperature(element)
        }
    }

    internal fun renderTemplate(template: ParsedTemplate, factor: Float, measuringSystem: MeasuringSystem): String {
        val renderedParts = template.elements.map { element ->
            renderTemplateElement(element, factor, measuringSystem)
        }

        return applySmartPunctuation(renderedParts.joinToString(""))
    }

    /**
     * renderRecipe used to convert units and scale recipe and now covert terminology too
     *
     * @param recipe The recipe as provided by the server (RecipeV3)
     * @param factor The factor applied to change the proportions of the recipe.
     *  For instance 0.5 halves the recipe and 2 doubles it.
     *  To calculate the factor, take the number of desired servings and divide it by the original servings.
     * @param measuringSystem The target unit system for ingredient measurements (e.g., Metric or Imperial)
     */
    fun renderRecipe(recipe: RecipeV3, factor: Float, measuringSystem: MeasuringSystem): RecipeV3 {
        val scaledIngredients = recipe.ingredients?.map { ingredientSection ->
            IngredientsList(
                ingredientsList = ingredientSection.ingredientsList?.map { templateIngredient ->
                    val scaledText = templateIngredient.template?.let { template ->
                        wrapWithStrongTag(renderTemplate(parseTemplate(template), factor, measuringSystem))
                    } ?: templateIngredient.text

                    templateIngredient.copy(text = scaledText)
                },
                recipeSection = ingredientSection.recipeSection
            )
        }
        val scaledInstructions = recipe.instructions?.map { instruction ->
            val description = instruction.descriptionTemplate?.let { template ->
                renderTemplate(parseTemplate(template), factor, measuringSystem)
            }
            instruction.copy(description = description ?: instruction.description)
        }

        val scaledRecipe = recipe.copy(ingredients = scaledIngredients, instructions = scaledInstructions)

        return if(convertTerminologies == false) {
            scaledRecipe
        } else {
            renderRecipeForTerminology(scaledRecipe)
        }

    }

    fun renderRecipeForTerminology(recipe: RecipeV3, section: TerminologySection = TerminologySection.ALL): RecipeV3 {
        return recipe.copy(
            title = if (shouldConvert(section, TerminologySection.TITLE)) {
                replaceInText(recipe.title)
            } else {
                recipe.title
            },
            description = if (shouldConvert(section, TerminologySection.DESCRIPTION)) {
                replaceInText(recipe.description)
            } else {
                recipe.description
            },
            ingredients = if (shouldConvert(section, TerminologySection.INGREDIENTS)) {
                recipe.ingredients?.map { ingredientSection ->
                    ingredientSection.copy(
                        ingredientsList = ingredientSection.ingredientsList?.map { ingredient ->
                            ingredient.copy(
                                text = replaceInText(ingredient.text),
                                template = replaceInText(ingredient.template)
                            )
                        },
                        recipeSection = replaceInText(ingredientSection.recipeSection)
                    )
                }
            } else {
                recipe.ingredients
            },
            instructions = if (shouldConvert(section, TerminologySection.INSTRUCTIONS)) {
                recipe.instructions?.map { instruction ->
                    instruction.copy(
                        description = replaceInText(instruction.description) ?: instruction.description,
                        descriptionTemplate = replaceInText(instruction.descriptionTemplate)
                    )
                }
            } else {
                recipe.instructions
            }
        )
    }

    private fun shouldConvert(section: TerminologySection, currentSection: TerminologySection): Boolean {
        return section == TerminologySection.ALL || section == currentSection
    }

    internal fun replaceInText(text: String?): String? {
        return terminologyTable?.convertTerm(text) ?: text
    }
}

fun newRenderSession(rawDensityData: String? = null, rawTerminologyData: String? = null, convertTerminologies: Boolean? = null): Result<RenderSession> {
    val terminologyTable = setUpTerminologyTable(rawTerminologyData).getOrElse {
        return Result.failure(it)
    }
    val densityTable = if (rawDensityData != null) loadDensityTable(rawDensityData) else loadInternalDensityTable()
    return densityTable.map { RenderSession(it, terminologyTable, convertTerminologies) }
}

/**
 * Creates a RenderSession without any density conversion data.  This is intended as a fallback
 * if newRenderSession fails on internal data
 */
fun noCustomaryRenderSession(): RenderSession {
    val densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap())
    val terminologyTable = TerminologyTable(HashMap())
    return RenderSession(densityTable, terminologyTable)
}

fun ingredientWithoutSuffix(renderedTemplate: String): String {
    val (before, _) = splitBeforeSuffix(renderedTemplate)
    return before.trim()
}

fun setUpTerminologyTable(rawTerminologyData: String? = null):Result<TerminologyTable> {
    val terminologyTable =
        (if (rawTerminologyData != null) loadTerminologyTable(rawTerminologyData) else loadInternalTerminologyTable()).map { it }
    return terminologyTable
}