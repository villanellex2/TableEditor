package com.echernikova.editor.table.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TableDataControllerTest {

    @Test
    fun `TableData recalculates dependencies`() {
        fillTable()

        underTest.setValueToCell(A0.pointer, UPDATED_VALUE.toString())
        assertEquals(UPDATED_VALUE.toString(), A1.getEvaluationResult().evaluatedValue.toString())
        assertEquals(UPDATED_VALUE.toString(), A2.getEvaluationResult().evaluatedValue.toString())
    }

    @Test
    fun `TableData recalculates only it's dependencies`() {
        fillTable()

        underTest.setValueToCell(A1.pointer, UPDATED_VALUE.toString())
        assertEquals("0", A0.getEvaluationResult().evaluatedValue.toString())
        assertEquals(UPDATED_VALUE.toString(), A1.getEvaluationResult().evaluatedValue.toString())
        assertEquals(UPDATED_VALUE.toString(), A2.getEvaluationResult().evaluatedValue.toString())
    }

    @Test
    fun `TableData finds cycle dependencies on itself`() {
        fillTable()

        assertEquals("Cycle dependencies!", C1.getEvaluationResult().evaluatedValue)
    }

    @Test
    fun `TableData finds cycle dependencies and resolve it for everyone once it is ok`() {
        fillTable()

        assertEquals("Cycle dependencies!", D1.getEvaluationResult().evaluatedValue)
        assertEquals("Cycle dependencies!", D2.getEvaluationResult().evaluatedValue)
        assertEquals("Cycle dependencies!", D3.getEvaluationResult().evaluatedValue)

        underTest.setValueToCell(D1.pointer, UPDATED_VALUE.toString())
        assertEquals(UPDATED_VALUE, D1.getEvaluationResult().evaluatedValue)
        assertEquals(UPDATED_VALUE, D2.getEvaluationResult().evaluatedValue)
        assertEquals(UPDATED_VALUE, D3.getEvaluationResult().evaluatedValue)
    }

    @Test
    fun `Cell evaluation is not blocked if it's dependent cells have cycle`() {
        fillTable()

        assertEquals(0, B1.getEvaluationResult().evaluatedValue)
        assertEquals("Cycle dependencies!", B2.getEvaluationResult().evaluatedValue)
        assertEquals("Cycle dependencies!", B3.getEvaluationResult().evaluatedValue)

        underTest.setValueToCell(B1.pointer, UPDATED_VALUE.toString())
        assertEquals(UPDATED_VALUE, B1.getEvaluationResult().evaluatedValue)
        assertEquals("Cycle dependencies!", B2.getEvaluationResult().evaluatedValue)
        assertEquals("Cycle dependencies!", B3.getEvaluationResult().evaluatedValue)
    }
}