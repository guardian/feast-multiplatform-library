package com.gu.recipe

import kotlin.math.round

internal object FormatUtils {
    fun formatToNearestFraction(number: Float): String {
        if (number == 0f) return "0"
        val integerPart = number.toInt()
        val integerPartStr = if (integerPart > 0) integerPart.toString() else ""

        val fractionalPart = number - integerPart

        // these hard coded values are midpoints between fractions.
        // This keeps the logic dead simple
        return when (fractionalPart) {
            in 0.0f..0.125f -> integerPartStr
            in 0.125f..0.291667f -> "$integerPartStr¼"
            in 0.291667f..0.416667f -> "$integerPartStr⅓"
            in 0.416667f..0.583333f -> "$integerPartStr½"
            in 0.583333f..0.708333f -> "$integerPartStr⅔"
            in 0.708333f..0.875f -> "$integerPartStr¾"
            else -> "${integerPart + 1}"
        }
    }

    fun formatAmount(number: Float, decimals: Int, fraction: Boolean): String {
        if (fraction) {
            return formatToNearestFraction(number)
        } else {
            var multiplier = 1.0
            repeat(decimals) { multiplier *= 10 }
            val roundedNumber = round(number * multiplier) / multiplier
            // If the number is an integer, don't show decimal places
            return if (roundedNumber % 1.0 == 0.0) {
                roundedNumber.toInt().toString()
            } else {
                roundedNumber.toString()
            }
        }
    }

    val captureMatchingQuotes = Regex("([\"])(.*?)([\"])")

    fun applySmartPunctuation(text: String): String {
        // capture opening quotes with their matching closing quote in order to replace each pair together with opening and closing fancy quotes
        val withQuotes = captureMatchingQuotes.replace(text) { matchResult ->
            "“${matchResult.groupValues[2]}”"
        }

        return withQuotes
            .replace("'", "’")
            .replace(" - ", " – ")
    }
}