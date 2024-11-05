package com.echernikova.evaluator

import com.echernikova.editor.table.model.EvaluatingTableModel
import com.echernikova.editor.table.model.CellPointer
import com.echernikova.editor.table.model.TableCell
import com.echernikova.evaluator.core.DataEvaluationResult
import com.echernikova.evaluator.core.Evaluator
import com.echernikova.evaluator.functions.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.util.stream.Stream
import kotlin.test.BeforeTest

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
    private val viewModel = mock<EvaluatingTableModel>()

    @BeforeTest
    fun initTableMock() {
        createMockCell("A3", 5)
        createMockCell("A4", 1.0)
        createMockCell("A5", "= 3.0 + 5")

        createMockCell("C1", "Dog")
        createMockCell("C2", "Cat")
        createMockCell("C3", "Tiger")

        createMockCell("D1", "17kg")
        createMockCell("D2", "2.5kg")
        createMockCell("D3", "80kg")
    }

    companion object {
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

            Arguments.of("=SUM(5,6)", 11, emptySet<CellPointer>()),
            Arguments.of("=SUM(5,6,A5)", 19.0, A5),
            Arguments.of("= 19 - SUM(5,6,A5)", 0.0, A5),
            Arguments.of("=MIN(5,6,A5)", 5.0, A5),
            Arguments.of("= 19 - MAX(5,6,A5)", 11.0, A5),
            Arguments.of("= 15 + AVERAGE(4,6,A5)", 21.0, A5),
            Arguments.of("=IF(15>17, 2, 5.0)", 5.0, emptySet<CellPointer>()),

            Arguments.of("=AVERAGE(A3:A5)", 14.0 / 3, A3A5),
            Arguments.of("=SUM(A4:A5)", 9.0, A4A5),
            Arguments.of("=PRODUCT(A3:A5)", 40.0, A3A5),
            Arguments.of("=MIN(A3:A5)", 1.0, A3A5),
            Arguments.of("=MAX(A3:A5)", 8.0, A3A5),

            Arguments.of("=VLOOKUP(\"Tiger\",C1:D3,2)", "80kg", animalWeightsTable),
            Arguments.of("=VLOOKUP(\"Cat\",C1:D3,2)", "2.5kg", animalWeightsTable),
            Arguments.of("=VLOOKUP(\"Dog\",C1:D3,2)", "17kg", animalWeightsTable),
            Arguments.of("=VLOOKUP(\"17kg\",C1:D3,1)", "Dog", animalWeightsTable),
        )

        // Cell dependencies

        private val animalWeightsTable = setOf(
            CellPointer.fromString("C1"),
            CellPointer.fromString("C2"),
            CellPointer.fromString("C3"),
            CellPointer.fromString("D1"),
            CellPointer.fromString("D2"),
            CellPointer.fromString("D3"),
        )

        private val A3 = setOf(CellPointer.fromString("A3"))
        private val A4 = setOf(CellPointer.fromString("A4"))
        private val A5 = setOf(CellPointer.fromString("A5"))

        private val A3A5 = A3 + A4 + A5
        private val A4A5 = A4 + A5
    }

    @ParameterizedTest(name = "Test case {0} = {1}, with dependencies = {2}")
    @MethodSource("testCases")
    fun `Evaluator evaluate cases`(expression: String, expected: Any, cellLinks: Set<CellPointer>) {
        val output = underTest.evaluate(expression, viewModel)

        assertEquals(expected, output.evaluatedValue)
        assertEquals(cellLinks, output.cellDependencies)
    }

    private fun createMockCell(position: String, value: Any): TableCell {
        val mock = mock<TableCell>()
        whenever(mock.getEvaluationResult()).doReturn(DataEvaluationResult(value, emptySet()))
        whenever(viewModel.getValueAt(CellPointer.fromString(position)!!)).doReturn(mock)
        return mock
    }
}
