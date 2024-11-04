package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.Evaluator
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import java.util.Vector
import kotlin.test.assertEquals

class TableDataControllerInitTest {
    private val evaluator = spy(Evaluator(emptyMap()))
    private val underTest = TableDataController(evaluator = evaluator)

    @Test
    fun `TableData correctly init values`() {
        initData()

        for (i in 0..10) {
           assertEquals(i.toString(), underTest.getCell(CellPointer(0, i))?.rawValue)
        }
        for (i in 0..9) {
            assertEquals("=${'A' + i}1", underTest.getCell(CellPointer(1, i))?.rawValue)
        }
    }

    @Test
    fun `TableData updates value in table and tries to update dependent cell`() {
        initData()
        verify(evaluator).evaluate("=${'A' + 1}1", underTest)

        val updatingCell = CellPointer(0, 2)

        underTest.setValueToCell(updatingCell, UPDATED_VALUE.toString())

        assertEquals(UPDATED_VALUE.toString(), underTest.getCell(updatingCell)?.rawValue)
        verify(evaluator, times(2)).evaluate("=${'A' + 1}1", underTest)
        assertEquals(
            UPDATED_VALUE,
            underTest.getCell(CellPointer.fromString("${'A' + 1}1")!!)?.getEvaluationResult()?.evaluatedValue
        )
    }

    @Test
    fun `TableData evaluates values only ones on init`() {
        initData()
        verify(evaluator, times(1)).evaluate("=${'A' + 1}1", underTest)
        verify(evaluator, times(1)).evaluate("=${'A' + 2}1", underTest)
        verify(evaluator, times(1)).evaluate("=${'A' + 3}1", underTest)
        verify(evaluator, times(1)).evaluate("=${'A' + 4}1", underTest)

        verify(evaluator, times(1)).evaluate("1", underTest)
        verify(evaluator, times(1)).evaluate("2", underTest)
        verify(evaluator, times(1)).evaluate("3", underTest)
        verify(evaluator, times(1)).evaluate("4", underTest)
    }

    private fun initData() {
        val row0 = Vector<Any?>()
        for (i in 0..10) {
            row0.add(i.toString())
        }
        val row1 = Vector<Any?>()
        for (i in 0..9) {
            row1.add("=${'A' + i}1")
        }
        val vector = Vector<Vector<Any?>>()
        vector.add(row0)
        vector.add(row1)

        underTest.initData(vector)
    }
}