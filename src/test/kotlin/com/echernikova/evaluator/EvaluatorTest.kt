package com.echernikova.evaluator

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.editor.table.model.TableDataController
import com.echernikova.evaluator.core.Evaluator
import com.echernikova.evaluator.core.tokenizing.Token
import com.echernikova.evaluator.functions.*
import com.echernikova.evaluator.operators.OperatorCellLink
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class EvaluatorTest {

    private val supportedFunc = listOf(
        FunctionSum(),
        FunctionMin(),
        FunctionMax(),
        FunctionAverage(),
        FunctionProduct(),
        FunctionIf(),
        FunctionVLookUp(),
    ).associateBy { it.name }

    private val underTest = Evaluator(supportedFunc)
    private val tableDataController = TableDataController(evaluator = underTest)

    init {
        tableDataController.setValueToCell(OperatorCellLink(Token.Cell.CellLink("A1")).cellPosition!!, "false")
        tableDataController.setValueToCell(OperatorCellLink(Token.Cell.CellLink("A2")).cellPosition!!, "true")
        tableDataController.setValueToCell(OperatorCellLink(Token.Cell.CellLink("A3")).cellPosition!!, "5")
        tableDataController.setValueToCell(OperatorCellLink(Token.Cell.CellLink("A4")).cellPosition!!, "1.0")
        tableDataController.setValueToCell(OperatorCellLink(Token.Cell.CellLink("A5")).cellPosition!!, "= 3.0 + 5")

        tableDataController.setValueToCell(OperatorCellLink(Token.Cell.CellLink("C1")).cellPosition!!, "Dog")
        tableDataController.setValueToCell(OperatorCellLink(Token.Cell.CellLink("C2")).cellPosition!!, "Cat")
        tableDataController.setValueToCell(OperatorCellLink(Token.Cell.CellLink("C3")).cellPosition!!, "Tiger")

        tableDataController.setValueToCell(OperatorCellLink(Token.Cell.CellLink("D1")).cellPosition!!, "17kg")
        tableDataController.setValueToCell(OperatorCellLink(Token.Cell.CellLink("D2")).cellPosition!!, "2.5kg")
        tableDataController.setValueToCell(OperatorCellLink(Token.Cell.CellLink("D3")).cellPosition!!, "80kg")
    }

    companion object {

        private val animalWeightsTable = setOf(
            OperatorCellLink(Token.Cell.CellLink("C1")).cellPosition,
            OperatorCellLink(Token.Cell.CellLink("C2")).cellPosition,
            OperatorCellLink(Token.Cell.CellLink("C3")).cellPosition,
            OperatorCellLink(Token.Cell.CellLink("D1")).cellPosition,
            OperatorCellLink(Token.Cell.CellLink("D2")).cellPosition,
            OperatorCellLink(Token.Cell.CellLink("D3")).cellPosition,
        )

        private val A3 = setOf(OperatorCellLink(Token.Cell.CellLink("A3")).cellPosition,)
        private val A4 = setOf(OperatorCellLink(Token.Cell.CellLink("A4")).cellPosition,)
        private val A5 = setOf(OperatorCellLink(Token.Cell.CellLink("A5")).cellPosition,)

        private val A3A5 = A3 + A4 + A5
        private val A4A5 = A4 + A5

        @JvmStatic
        fun testCases(): Stream<Arguments> = Stream.of(
            Arguments.of("=+-+-+-1", -1, emptySet<CellPointer>()),
            Arguments.of("=-1", -1, emptySet<CellPointer>()),
            Arguments.of("-1", -1, emptySet<CellPointer>()),
            Arguments.of("=--1", 1, emptySet<CellPointer>()),

            Arguments.of("=15 + 60.0", 75.0, emptySet<CellPointer>()),
            Arguments.of("=15^1", 15, emptySet<CellPointer>()),
            Arguments.of("=16 / 2", 8, emptySet<CellPointer>()),
            Arguments.of("=16 / 2 + 5", 13, emptySet<CellPointer>()),
            Arguments.of("=11+ \t16 / 2 + 5 - 11^0", 23, emptySet<CellPointer>()),
            Arguments.of("=false || true", true, emptySet<CellPointer>()),
            Arguments.of("=false && true", false, emptySet<CellPointer>()),

            Arguments.of("=15 + (60.0)", 75.0, emptySet<CellPointer>()),
            Arguments.of("=15^(1-1)", 1, emptySet<CellPointer>()),
            Arguments.of("=16 / (2 + 2)", 4, emptySet<CellPointer>()),
            Arguments.of("=(1.0 / 2) + 5", 5.5, emptySet<CellPointer>()),

            Arguments.of("=SUM(5;6)", 11, emptySet<CellPointer>()),
            Arguments.of("=SUM(5;6;A5)", 19.0, A5),
            Arguments.of("= 19 - SUM(5;6;A5)", 0.0, A5),
            Arguments.of("=MIN(5;6;A5)", 5.0, A5),
            Arguments.of("= 19 - MAX(5;6;A5)", 11.0, A5),
            Arguments.of("= 15 + AVERAGE(4;6;A5)", 21.0, A5),
            Arguments.of("=IF(15>17; 2; 5.0)", 5.0, emptySet<CellPointer>()),

            Arguments.of("=AVERAGE(A3:A5)", 14.0 / 3, A3A5),
            Arguments.of("=SUM(A4:A5)", 9.0, A4A5),
            Arguments.of("=PRODUCT(A3:A5)", 40.0, A3A5),
            Arguments.of("=MIN(A3:A5)", 1.0, A3A5),
            Arguments.of("=MAX(A3:A5)", 8.0, A3A5),

            Arguments.of("=VLOOKUP(\"Tiger\";C1:D3;2)", "80kg", animalWeightsTable),
            Arguments.of("=VLOOKUP(\"Cat\";C1:D3;2)", "2.5kg", animalWeightsTable),
            Arguments.of("=VLOOKUP(\"Dog\";C1:D3;2)", "17kg", animalWeightsTable),
            Arguments.of("=VLOOKUP(\"17kg\";C1:D3;1)", "Dog", animalWeightsTable),
        )
    }

    @ParameterizedTest(name = "Test case {0} = {1}, with dependencies = {2}")
    @MethodSource("testCases")
    fun `Evaluator evaluate cases`(expression: String, expected: Any, cellLinks: Set<CellPointer>) {
        val output = underTest.evaluate(expression, tableDataController)

        assertEquals(expected, output.evaluatedValue)
        assertEquals(cellLinks, output.cellDependencies)
    }
}
