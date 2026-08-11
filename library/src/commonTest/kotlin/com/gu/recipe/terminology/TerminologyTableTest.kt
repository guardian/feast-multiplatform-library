package com.gu.recipe.terminology

import kotlin.test.Test
import kotlin.test.assertEquals

class TerminologyTableTest {
    @Test
    fun `blocked phrase still blocks when phrase is longer`() {
        val blockedPhrase = "very very very very very long pepper"
        val table = TerminologyTable(
            terminologyMap = mapOf(
                "pepper" to TerminologyEntry(
                    id = 1,
                    ukTerm = "pepper",
                    usTerm = "black pepper",
                    block = listOf(blockedPhrase),
                    ukGuidance = "",
                    usGuidance = ""
                )
            )
        )

        assertEquals(
            blockedPhrase,
            table.convertTerm(blockedPhrase)!!.replacedText
        )
    }

    @Test
    fun `blocked phrase only blocks whole phrase matches`() {
        val table = TerminologyTable(
            terminologyMap = mapOf(
                "pepper" to TerminologyEntry(
                    id = 1,
                    ukTerm = "pepper",
                    usTerm = "black pepper",
                    block = listOf("red pepper"),
                    ukGuidance = "",
                    usGuidance = ""
                )
            )
        )

        assertEquals(
            "tired black pepper",
            table.convertTerm("tired pepper")!!.replacedText
        )
    }


    @Test
    fun `blocked phrases are matched case-insensitively while unblocked replacements preserve leading uppercase`() {
        val table = TerminologyTable(
            terminologyMap = mapOf(
                "sponge" to TerminologyEntry(
                    id = 1,
                    ukTerm = "sponge",
                    usTerm = "cake",
                    block = listOf("victoria sponge", "sponge cake"),
                    ukGuidance = "",
                    usGuidance = ""
                )
            )
        )

        assertEquals(
            "Victoria sponge with Cake and SPONGE CAKE.",
            table.convertTerm("Victoria sponge with Sponge and SPONGE CAKE.")!!.replacedText
        )
    }

    @Test
    fun `repeated terms in one line are blocked or replaced independently`() {
        val table = TerminologyTable(
            terminologyMap = mapOf(
                "pepper" to TerminologyEntry(
                    id = 1,
                    ukTerm = "pepper",
                    usTerm = "black pepper",
                    block = listOf("red pepper", "green pepper"),
                    ukGuidance = "",
                    usGuidance = ""
                )
            )
        )

        assertEquals(
            "black pepper, red pepper, black pepper, green pepper, black pepper.",
            table.convertTerm("pepper, red pepper, pepper, green pepper, pepper.")!!.replacedText
        )
    }

    @Test
    fun `guidance notes are available directly from an existing conversion result`() {
        val table = TerminologyTable(
            terminologyMap = mapOf(
                "aubergine" to TerminologyEntry(
                    id = 1,
                    ukTerm = "aubergine",
                    usTerm = "eggplant",
                    block = emptyList(),
                    ukGuidance = "Use this note for UK readers",
                    usGuidance = "Use this note for US readers"
                )
            )
        )

        val conversionResult = table.convertTerm("1 aubergine", requireUnderline = true)

        assertEquals("1 <u>eggplant</u>", conversionResult?.replacedText)
        assertEquals("Use this note for UK readers", conversionResult?.ukGuidance)
        assertEquals("Use this note for US readers", conversionResult?.usGuidance)
    }
}
