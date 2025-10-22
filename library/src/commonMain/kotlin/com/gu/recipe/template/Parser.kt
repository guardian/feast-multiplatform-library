package com.gu.recipe.template

import kotlinx.serialization.json.*
import com.gu.recipe.generated.StringTemplate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

private val tolerantJson = Json { ignoreUnknownKeys = true }

fun parseTemplate(template: StringTemplate): ParsedTemplate {
    val pattern = Regex("""\{(?:[^{}"]|"(?:[^"\\\\]|\\\\.)*")*\}""")
    val parts = mutableListOf<TemplateElement>()
    var lastEnd = 0

    pattern.findAll(template).forEach { match ->
        // Add text before JSON
        if (match.range.first > lastEnd) {
            val textPart = template.substring(lastEnd, match.range.first)
            if (textPart.isNotBlank()) {
                parts.add(TemplateElement.TemplateConst(textPart))
            }
        }

        // Try to parse JSON
        try {
            val jsonObj = Json.parseToJsonElement(match.value).jsonObject
            if (jsonObj.keys.contains("min")) {
                tolerantJson.decodeFromJsonElement<TemplateElement.QuantityPlaceholder>(jsonObj).let {
                    parts.add(it)
                }
            } else {
                tolerantJson.decodeFromJsonElement<TemplateElement.OvenTemperaturePlaceholder>(jsonObj).let {
                    parts.add(it)
                }
            }
        } catch (e: Exception) {
            print("Failed to parse JSON: ${match.value}, error: ${e.message}")
            parts.add(TemplateElement.TemplateConst(match.value))
        }

        lastEnd = match.range.last + 1
    }

    // Add remaining text
    if (lastEnd < template.length) {
        val remaining = template.substring(lastEnd)
        if (remaining.isNotBlank()) {
            parts.add(TemplateElement.TemplateConst(remaining))
        }
    }

    return ParsedTemplate(parts)
}