package com.gu.recipe

import kotlin.math.round

internal object FormatUtils {
    fun formatToNearestFraction(number: Float): String {
        if (number == 0f) return "0"
        val integerPart = number.toInt()
        val integerPartStr = if (integerPart > 0) integerPart.toString() else ""

        val fractionalPart = number - integerPart

        // these hard coded values are midpoints between fractions.
        // For instance 0.1875 is halfway between 1/8 (0.125) and 1/4 (0.25)
        // This keeps the logic dead simple
        return when (fractionalPart) {
            in 0.0f..0.062500f -> integerPartStr
            in 0.062500f..0.187500f -> "$integerPartStr⅛"
            in 0.187500f..0.291667f -> "$integerPartStr¼"
            in 0.291667f..0.354167f -> "$integerPartStr⅓"
            in 0.354167f..0.437500f -> "$integerPartStr⅜"
            in 0.437500f..0.562500f -> "$integerPartStr½"
            in 0.562500f..0.645833f -> "$integerPartStr⅝"
            in 0.645833f..0.708333f -> "$integerPartStr⅔"
            in 0.708333f..0.812500f -> "$integerPartStr¾"
            in 0.812500f..0.937500f -> "$integerPartStr⅞"
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