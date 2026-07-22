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
                    block = listOf(blockedPhrase)
                )
            )
        )

        assertEquals(
            blockedPhrase,
            table.convertTerm(blockedPhrase)
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
                    block = listOf("red pepper")
                )
            )
        )

        assertEquals(
            "tired black pepper",
            table.convertTerm("tired pepper")
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
                    block = listOf("victoria sponge", "sponge cake")
                )
            )
        )

        assertEquals(
            "Victoria sponge with Cake and SPONGE CAKE.",
            table.convertTerm("Victoria sponge with Sponge and SPONGE CAKE.")
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
                    block = listOf("red pepper", "green pepper")
                )
            )
        )

        assertEquals(
            "black pepper, red pepper, black pepper, green pepper, black pepper.",
            table.convertTerm("pepper, red pepper, pepper, green pepper, pepper.")
        )
    }
}
