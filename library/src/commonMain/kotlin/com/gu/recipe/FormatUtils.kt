package com.gu.recipe

import kotlin.math.round

internal object FormatUtils {
    fun formatFraction(number: Float): String {
        val integerPart = number.toInt()
        val fractionalPart = number - integerPart
        val fractionString = when (fractionalPart) {
            in 0.12f..0.13f -> "⅛"
            in 0.33f..0.34f -> "⅓"
            in 0.66f..0.67f -> "⅔"
            0.25f -> "¼"
            0.5f -> "½"
            0.75f -> "¾"
            else -> null
        }
        return if (fractionString != null) {
            if (integerPart > 0) {
                "$integerPart$fractionString"
            } else {
                fractionString
            }
        } else {
            number.toString()
        }
    }

    fun formatAmount(number: Float, decimals: Int, fraction: Boolean): String {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        val roundedNumber = round(number * multiplier) / multiplier
        // If the number is an integer, don't show decimal places
        if (roundedNumber % 1.0 == 0.0) {
            return roundedNumber.toInt().toString()
        }

        if (fraction) {
            return formatFraction(roundedNumber.toFloat())
        }
        return roundedNumber.toString()
    }

    val captureMatchingQuotes = Regex("([\"])(.*?)([\"])")

    fun applySmartPunctuation(text: String): String {
        // capture opening quotes with their matching closing quote in order to replace each pair together with opening and closing fancy quotes
        val withQuotes = captureMatchingQuotes.replace(text) { matchResult ->
            "“${matchResult.groupValues[2]}”"
        }

        return withQuotes.replace("'", "’")
    }
}