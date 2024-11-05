package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.Evaluator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

private const val UPDATED_VALUE = 1024

class TableDataControllerInitTest {
    private val evaluator = spy(Evaluator(emptyMap()))
    private val dispatcher = StandardTestDispatcher()
    private val underTest = EvaluatingTableModel(initData(), evaluator, CoroutineScope(dispatcher))

    private fun initData(): Array<Array<String?>> {
        val row0 = Array<String?>(10) { it.toString() }
        val row1 = Array<String?>(10) { "=${'A' + it}1" }
        return arrayOf(row0, row1)
    }

    @Test
    fun `TableData correctly init values`() {

        for (i in 0..9) {
            assertEquals(i.toString(), underTest.getValueAt(CellPointer(0, i))?.rawValue)
        }
        for (i in 0..9) {
            assertEquals("=${'A' + i}1", underTest.getValueAt(CellPointer(1, i))?.rawValue)
        }
    }

    @Test
    fun `TableData updates value in table and tries to update dependent cell`() = runTest(dispatcher) {
        advanceUntilIdle()

        verify(evaluator).evaluate("=${'A' + 1}1", underTest)

        val updatingCell = CellPointer(0, 2)
        underTest.setValueAt(UPDATED_VALUE.toString(), 0, 2)

        assertEquals(UPDATED_VALUE.toString(), underTest.getValueAt(updatingCell)?.rawValue)
        verify(evaluator, times(2)).evaluate("=${'A' + 1}1", underTest)
        assertEquals(
            UPDATED_VALUE,
            underTest.getValueAt(CellPointer.fromString("${'A' + 1}1")!!)?.getEvaluationResult()?.evaluatedValue
        )
    }

    @Test
    fun `TableData evaluates values only ones on init`() = runTest(dispatcher) {
        advanceUntilIdle()

        verify(evaluator, times(1)).evaluate("=${'A' + 1}1", underTest)
        verify(evaluator, times(1)).evaluate("=${'A' + 2}1", underTest)
        verify(evaluator, times(1)).evaluate("=${'A' + 3}1", underTest)
        verify(evaluator, times(1)).evaluate("=${'A' + 4}1", underTest)

        verify(evaluator, times(1)).evaluate("1", underTest)
        verify(evaluator, times(1)).evaluate("2", underTest)
        verify(evaluator, times(1)).evaluate("3", underTest)
        verify(evaluator, times(1)).evaluate("4", underTest)
    }
}