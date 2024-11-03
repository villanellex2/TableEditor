package com.chernikova.evaluator

import com.echernikova.editor.table.model.TableData
import com.echernikova.evaluator.core.Evaluator
import com.echernikova.evaluator.core.tokenizing.Token
import com.echernikova.evaluator.functions.*
import com.echernikova.evaluator.operators.OperatorCellLink
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EvaluatorTest {

    private val supportedFunc = listOf(
        FunctionSum(),
        FunctionMin(),
        FunctionMax(),
        FunctionAverage(),
        FunctionProduct(),
        FunctionIf(),
    ).associateBy {
        it.name
    }

    private val underTest = Evaluator(supportedFunc)
    private val tableData = TableData(evaluator = underTest)

    init {
        tableData.setValueToCell(
            OperatorCellLink( Token.Cell.CellLink("A1")).cellPosition,
            "false"
        )

        tableData.setValueToCell(
            OperatorCellLink( Token.Cell.CellLink("A2")).cellPosition,
            "true"
        )

        tableData.setValueToCell(
            OperatorCellLink( Token.Cell.CellLink("A3")).cellPosition,
            "5"
        )

        tableData.setValueToCell(
            OperatorCellLink( Token.Cell.CellLink("A4")).cellPosition,
            "1.0"
        )

        tableData.setValueToCell(
            OperatorCellLink( Token.Cell.CellLink("A5")).cellPosition,
            "= 3.0 + 5"
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

    @Test
    fun `Evaluator correctly evaluate function`() {
        assertEquals(11, underTest.evaluate("=SUM(5;6)", tableData).evaluatedValue)
        assertEquals(19.0, underTest.evaluate("=SUM(5;6;A5)", tableData).evaluatedValue)
        assertEquals(0.0, underTest.evaluate("= 19 - SUM(5;6;A5)", tableData).evaluatedValue)
        assertEquals(5.0, underTest.evaluate("=MIN(5;6;A5)", tableData).evaluatedValue)
        assertEquals(11.0, underTest.evaluate("= 19 - MAX(5;6;A5)", tableData).evaluatedValue)
        assertEquals(21.0, underTest.evaluate("= 15 + AVERAGE(4;6;A5)", tableData).evaluatedValue)
        assertEquals(5.0, underTest.evaluate("=IF(15>17; 2; 5.0)", tableData).evaluatedValue)
    }
}
