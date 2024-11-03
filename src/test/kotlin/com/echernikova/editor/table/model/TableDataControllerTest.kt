package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.Evaluator
import org.junit.jupiter.api.Test
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.kotlin.verify
import java.util.Vector
import kotlin.test.assertEquals

private const val UPDATED_VALUE = 1024

class TableDataControllerTest {
    private val evaluator = spy(Evaluator(emptyMap()))
    private val underTest = TableDataController(evaluator = evaluator)

    @Test
    fun `TableData correctly init values`() {
        initTable()

        for (i in 0..10) {
           assertEquals(i.toString(), underTest.getCell(CellPointer(0, i))?.rawValue)
        }
        for (i in 0..9) {
            assertEquals("=${'A' + i}0", underTest.getCell(CellPointer(1, i))?.rawValue)
        }
    }

    @Test
    fun `TableData updates value in table and tries to update dependent cell`() {
        initTable()
        verify(evaluator).evaluate("=${'A' + 1}0", underTest)

        val updatingCell = CellPointer(0, 2)

        underTest.setValueToCell(updatingCell, UPDATED_VALUE.toString())

        assertEquals(UPDATED_VALUE.toString(), underTest.getCell(updatingCell)?.rawValue)
        verify(evaluator, times(2)).evaluate("=${'A' + 1}0", underTest)
        assertEquals(UPDATED_VALUE, underTest.getCell(CellPointer.fromString("${'A' + 1}0"))?.evaluationResult?.evaluatedValue)
    }

    @Test
    fun `TableData evaluates values only ones on init`() {
        initTable()
        verify(evaluator, times(1)).evaluate("=${'A' + 1}0", underTest)
        verify(evaluator, times(1)).evaluate("=${'A' + 2}0", underTest)
        verify(evaluator, times(1)).evaluate("=${'A' + 3}0", underTest)
        verify(evaluator, times(1)).evaluate("=${'A' + 4}0", underTest)

        verify(evaluator, times(1)).evaluate("1", underTest)
        verify(evaluator, times(1)).evaluate("2", underTest)
        verify(evaluator, times(1)).evaluate("3", underTest)
        verify(evaluator, times(1)).evaluate("4", underTest)
    }

    private fun initTable() {
        val row0 = Vector<Any?>()
        for (i in 0..10) {
            row0.add(i.toString())
        }
        val row1 = Vector<Any?>()
        for (i in 0..9) {
            row1.add("=${'A' + i}0")
        }
        val vector = Vector<Vector<Any?>>()
        vector.add(row0)
        vector.add(row1)

        underTest.initData(vector)
    }
}