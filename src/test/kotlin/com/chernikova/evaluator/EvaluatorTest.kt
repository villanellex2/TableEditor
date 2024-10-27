package com.chernikova.evaluator

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.editor.table.model.TableData
import com.echernikova.evaluator.core.Evaluator
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import kotlin.test.assertEquals

class EvaluatorTest {
    private val tableData = TableData(mock())
    private val underTest = Evaluator(mock())

    init {
        tableData.setValueToCell(
            CellPointer(1, 1),
            "false"
        )

        tableData.setValueToCell(
            CellPointer(1, 2),
            "true"
        )

        tableData.setValueToCell(
            CellPointer(1, 3),
            "5"
        )

        tableData.setValueToCell(
            CellPointer(1, 4),
            "1.0"
        )

        tableData.setValueToCell(
            CellPointer(1, 4),
            "= 1.0 + 5"
        )
    }

    @Test
    fun `Evaluator correctly evaluate unary operators`() {
        assertEquals(-1, underTest.evaluate("=+-+-+-1", tableData).evaluatedValue)
        assertEquals(-1, underTest.evaluate("=-1", tableData).evaluatedValue)
        assertEquals(-1, underTest.evaluate("-1", tableData).evaluatedValue)
        assertEquals(1, underTest.evaluate("=--1", tableData).evaluatedValue)
    }

    @Test
    fun `Evaluator correctly evaluate binary operators using correct order of operators`() {
        assertEquals(75.0, underTest.evaluate("=15 + 60.0", tableData).evaluatedValue)
        assertEquals(15, underTest.evaluate("=15^1", tableData).evaluatedValue)
        assertEquals(8, underTest.evaluate("=16 / 2", tableData).evaluatedValue)
        assertEquals(13, underTest.evaluate("=16 / 2 + 5", tableData).evaluatedValue)
        assertEquals(23, underTest.evaluate("=11+ \t16 / 2 + 5 - 11^0", tableData).evaluatedValue)
        assertEquals(true, underTest.evaluate("=false || true", tableData).evaluatedValue)
        assertEquals(false, underTest.evaluate("=false && true", tableData).evaluatedValue)
    }

    @Test
    fun `Evaluator correctly evaluate expressions with brackets`() {
        assertEquals(75.0, underTest.evaluate("=15 + (60.0)", tableData).evaluatedValue)
        assertEquals(1, underTest.evaluate("=15^(1-1)", tableData).evaluatedValue)
        assertEquals(4, underTest.evaluate("=16 / (2 + 2)", tableData).evaluatedValue)
        assertEquals(5.5, underTest.evaluate("=(1.0 / 2) + 5", tableData).evaluatedValue)
    }
}
