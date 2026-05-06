package com.gu.recipe

import com.gu.recipe.FormatUtils.applySmartPunctuation
import com.gu.recipe.density.DensityTable
import com.gu.recipe.density.loadDensityTable
import com.gu.recipe.density.loadInternalDensityTable
import com.gu.recipe.egg.EggRegion
import com.gu.recipe.egg.convertEggSizesInText
import com.gu.recipe.generated.*
import com.gu.recipe.template.OvenTemperaturePlaceholder
import com.gu.recipe.template.ParsedTemplate
import com.gu.recipe.template.QuantityPlaceholder
import com.gu.recipe.template.TemplateConst
import com.gu.recipe.template.TemplateElement
import com.gu.recipe.template.parseTemplate
import com.gu.recipe.unit.MeasuringSystem
import com.gu.recipe.unit.UnitConversions
import com.gu.recipe.unit.UnitType
import com.gu.recipe.unit.Units
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.math.max

private val NON_BOLD_REGEX = Regex("""\([^()]*\)| • """)
private const val MARKER = "\u0000"

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
class TemplateSession(private val densityTable: DensityTable) {
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
            Units.METRIC_TEASPOON, Units.METRIC_TABLESPOON, Units.US_TABLESPOON, Units.US_TEASPOON -> amount.min < 1f
            else -> true
        }
        val unitString = if (amount.unit != null) {
            if (max(amount.min, amount.max ?: amount.min) > 1.1f) { //need to offset from exactly one, so that when rounding a value below 1/8 we don't get "1 cups
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
                    is MeasuringSystem.Metric, is MeasuringSystem.Imperial, is MeasuringSystem.USCustomary, is MeasuringSystem.Butter -> renderQuantity(element, factor, measuringSystem)
                    is MeasuringSystem.USCustomaryWithMetric -> {
                        if(element.unit.isNullOrBlank()) {
                            renderQuantity(element, factor, MeasuringSystem.USCustomary)
                        } else {
                            val cupsPart = renderQuantity(element, factor, MeasuringSystem.USCustomary)
                            val metricPart = renderQuantity(element, factor, MeasuringSystem.Metric)
                            if (cupsPart == metricPart) {
                                cupsPart
                            } else {
                                //NOTE - according to https://kotlinlang.org/docs/strings.html#string-formatting String.format()
                                //only works on JVM; therefore we can't use it here
                                cupsPart + " (" + metricPart + ")"
                            }
                        }
                    }
                    is MeasuringSystem.USCustomaryWithImperial -> {
                        if(element.unit.isNullOrBlank()) {
                            renderQuantity(element, factor, MeasuringSystem.USCustomary)
                        } else {
                            val cupsPart = renderQuantity(element, factor, MeasuringSystem.USCustomary)
                            val imperialPart = renderQuantity(element, factor, MeasuringSystem.Imperial)

                            if (cupsPart == imperialPart) {
                                cupsPart
                            } else {
                                cupsPart + " (" + imperialPart + ")"
                            }
                        }
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
                            val cupsPart = renderQuantity(element, factor, MeasuringSystem.USCustomary)
                            val imperialPart = if (unit == Units.FLUID_OUNCE) { // Skip rendering fluid ounces
                                null
                            } else if (unit.unitType == UnitType.VOLUME) { //don't show extra volumes in US
                                cupsPart
                            } else {
                                renderQuantity(element, factor, MeasuringSystem.Imperial)
                            }
                            val metricPart = renderQuantity(element, factor, MeasuringSystem.Metric)

                            if (cupsPart == metricPart && cupsPart == imperialPart) {
                                metricPart
                            } else if (cupsPart == imperialPart) {
                                cupsPart + " (" + metricPart + ")"
                            } else {
                                imperialPart + " • " + cupsPart + " (" + metricPart + ")"
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

        var result = applySmartPunctuation(renderedParts.joinToString(""))

        // Post-process: convert egg size labels for US audiences
        val eggRegion = measuringSystemToEggRegion(measuringSystem)
        if (eggRegion != null) {
            result = convertEggSizesInText(result, eggRegion)
        }

        return result
    }

    /**
     * scaleAndConvertUnitRecipe used to convert units and scale recipe
     *
     * @param recipe The recipe as provided by the server (RecipeV3)
     * @param factor The factor applied to change the proportions of the recipe.
     *  For instance 0.5 halves the recipe and 2 doubles it.
     *  To calculate the factor, take the number of desired servings and divide it by the original servings.
     * @param measuringSystem The target unit system for ingredient measurements (e.g., Metric or Imperial)
     */
    fun scaleAndConvertUnitRecipe(recipe: RecipeV3, factor: Float, measuringSystem: MeasuringSystem): RecipeV3 {
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

        return recipe.copy(ingredients = scaledIngredients, instructions = scaledInstructions)
    }
}

private fun measuringSystemToEggRegion(system: MeasuringSystem): EggRegion? {
    return when (system) {
        is MeasuringSystem.USCustomary,
        is MeasuringSystem.USCustomaryWithMetric,
        is MeasuringSystem.USCustomaryWithImperial,
        is MeasuringSystem.USCombined -> EggRegion.US
        else -> null
    }
}

fun newTemplateSession(rawDensityData: String? = null):Result<TemplateSession> {
    val densityTable = if(rawDensityData!=null) loadDensityTable(rawDensityData) else loadInternalDensityTable()
    return densityTable.map { TemplateSession(it) }
}

/**
 * Creates a TemplateSession without any density conversion data.  This is intended as a fallback
 * if newTemplateSession fails on internal data
 */
fun noCustomaryTemplateSession(): TemplateSession {
    val densityTable = DensityTable(preparedAt = "none", HashMap(), HashMap())
    return TemplateSession(densityTable)
}

fun ingredientWithoutSuffix(renderedTemplate: String): String {
    val (before, _) = splitBeforeSuffix(renderedTemplate)
    return before.trim()
}