package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.Evaluator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

private const val UPDATED_VALUE = 1024

class TableDataControllerTest {
    private val dispatcher = StandardTestDispatcher()
    private val underTest by lazy { EvaluatingTableModel(emptyArray(), Evaluator(emptyMap()), CoroutineScope(dispatcher)) }

    private val A1 by lazy { underTest.getValueAt(CellPointer.fromString("A1")!!) }
    private val A3 by lazy { underTest.getValueAt(CellPointer.fromString("A3")!!) }
    private val A2 by lazy { underTest.getValueAt(CellPointer.fromString("A2")!!) }

    private val B1 by lazy { underTest.getValueAt(CellPointer.fromString("B1")!!) }
    private val B2 by lazy { underTest.getValueAt(CellPointer.fromString("B2")!!) }
    private val B3 by lazy { underTest.getValueAt(CellPointer.fromString("B3")!!) }

    private val C1 by lazy { underTest.getValueAt(CellPointer.fromString("C1")!!) }

    private val D1 by lazy { underTest.getValueAt(CellPointer.fromString("D1")!!) }
    private val D2 by lazy { underTest.getValueAt(CellPointer.fromString("D2")!!) }
    private val D3 by lazy { underTest.getValueAt(CellPointer.fromString("D3")!!) }

    @BeforeTest
    fun fillTable() {
        underTest.apply {
            // A1 <- A2 <- A3
            setValueAt("0", CellPointer.fromString("A3")!!)
            setValueAt("=A3", CellPointer.fromString("A1")!!)
            setValueAt("=A1", CellPointer.fromString("A2")!!)

            // B1 <- B2 <-> B3 | Cycle!
            setValueAt("=0", CellPointer.fromString("B1")!!)
            setValueAt("=B1+B3", CellPointer.fromString("B2")!!)
            setValueAt("=B2", CellPointer.fromString("B3")!!)

            // C1 -> C1 | Cycle!
            setValueAt("=C1", CellPointer.fromString("C1")!!)

            // D1 -> D2 -> D3 -> D1 | Cycle!
            setValueAt("=D3", CellPointer.fromString("D1")!!)
            setValueAt("=D1", CellPointer.fromString("D2")!!)
            setValueAt("=D2", CellPointer.fromString("D3")!!)
        }
    }

    @Test
    fun `TableData recalculates dependencies`() = runTest(dispatcher) {
        underTest.setValueAt(UPDATED_VALUE.toString(), A3!!.pointer)
        advanceUntilIdle()
        assertEquals(UPDATED_VALUE.toString(), A1?.getEvaluationResult()?.evaluatedValue.toString())
        assertEquals(UPDATED_VALUE.toString(), A2?.getEvaluationResult()?.evaluatedValue.toString())
    }

    @Test
    fun `TableData recalculates only it's dependencies`() = runTest(dispatcher) {
        underTest.setValueAt(UPDATED_VALUE.toString(), A1!!.pointer)
        advanceUntilIdle()
        assertEquals("0", A3!!.getEvaluationResult().evaluatedValue.toString())
        assertEquals(UPDATED_VALUE.toString(), A1!!.getEvaluationResult().evaluatedValue.toString())
        assertEquals(UPDATED_VALUE.toString(), A2!!.getEvaluationResult().evaluatedValue.toString())
    }

    @Test
    fun `TableData finds cycle dependencies on itself`() = runTest(dispatcher) {
        assertEquals("Cycle dependencies!", C1!!.getEvaluationResult().evaluatedValue)
    }

    @Test
    fun `TableData finds cycle dependencies and resolve it for everyone once it is ok`() = runTest(dispatcher) {
        assertEquals("Cycle dependencies!", D1!!.getEvaluationResult().evaluatedValue)
        assertEquals("Cycle dependencies!", D2!!.getEvaluationResult().evaluatedValue)
        assertEquals("Cycle dependencies!", D3!!.getEvaluationResult().evaluatedValue)

        underTest.setValueAt(UPDATED_VALUE.toString(), D1!!.pointer)
        advanceUntilIdle()
        assertEquals(UPDATED_VALUE, D1!!.getEvaluationResult().evaluatedValue)
        assertEquals(UPDATED_VALUE, D2!!.getEvaluationResult().evaluatedValue)
        assertEquals(UPDATED_VALUE, D3!!.getEvaluationResult().evaluatedValue)
    }

    @Test
    fun `Cell evaluation is not blocked if it's dependent cells have cycle`() = runTest(dispatcher) {

        assertEquals(0, B1!!.getEvaluationResult().evaluatedValue)
        assertEquals("Cycle dependencies!", B2!!.getEvaluationResult().evaluatedValue)
        assertEquals("Cycle dependencies!", B3!!.getEvaluationResult().evaluatedValue)

        underTest.setValueAt(UPDATED_VALUE.toString(), B1!!.pointer)
        advanceUntilIdle()
        assertEquals(UPDATED_VALUE, B1!!.getEvaluationResult().evaluatedValue)
        assertEquals("Cycle dependencies!", B2!!.getEvaluationResult().evaluatedValue)
        assertEquals("Cycle dependencies!", B3!!.getEvaluationResult().evaluatedValue)
    }
}