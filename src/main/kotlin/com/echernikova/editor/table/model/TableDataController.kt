package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.Evaluator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

/**
 * Stores a copy of JTable with evaluated values.
 * Tracks updates in the graph and updates dependencies.
 */
class TableDataController(
    private val evaluator: Evaluator,
) {
    private val data: MutableMap<CellPointer, TableCell> = mutableMapOf()
    private val dependenciesGraph = TableDependenciesGraph()
    private val onEvaluatedCallback = { cell: TableCell -> dependenciesGraph.updateDependencies(cell) }
    private val scope = CoroutineScope(Dispatchers.Default)
    private val lock = Any()

    var dataExpiredCallback: ((CellPointer) -> Unit) = {}

    /**
     * Init data using JTable data, evaluates cells values after table init.
     */
    fun initData(sharingVector: Vector<Vector<Any?>>) {
        assert(data.isEmpty()) { "Data should be empty on init" }

        sharingVector.forEachIndexed { row, vector ->
            vector.forEachIndexed { column, value ->
                if (value != null) createCell(CellPointer(row, column), value.toString())
            }
        }

        scope.run {
            synchronized(lock) {
                data.toList().forEach { (_, cell) ->
                    cell.getEvaluationResult() // evaluate cell data and it's dependencies if needed.
                }

                data.forEach {
                    dataExpiredCallback(it.key)
                }
            }
        }
    }

    /**
     * Returns cell if it is initialised.
     */
    fun getCell(pointer: CellPointer): TableCell? = data[pointer]

    /**
     * Get or create empty cell if it is not initialised yet.
     */
    fun getOrCreateCell(pointer: CellPointer): TableCell {
        return synchronized(lock) { data.getOrPut(pointer) { createCell(pointer) } }
    }

    /**
     * Updates value of the cell.
     * Also causes reevaluation of all the dependent cells.
     */
    fun setValueToCell(pointer: CellPointer, value: String?) {
        val cell = getOrCreateCell(pointer)
        if (cell.rawValue == value) return
        synchronized(lock) {
            cell.rawValue = value
            evaluateInCorrectOrder(cell)
        }
    }

    fun getEvaluatedCells() = data.map { it.key to it.value.getEvaluationResult() }.toMap()

    fun addRow(row: Int, values: Array<Any?>) {
        synchronized(lock) {
            values.mapIndexedNotNull { i, value ->
                if (i != 0 && value != null) {
                    createCell(CellPointer(row, i), value.toString()).also { evaluateInCorrectOrder(it) }
                } else null
            }
        }
    }

    private fun evaluateInCorrectOrder(cell: TableCell) {
        scope.run {
            cell.evaluate()
            val (evaluationOrder, cycleCells) = dependenciesGraph.getCellsToUpdate(cell)

            evaluationOrder.forEach { if (it != cell.pointer) getCell(it)?.evaluate() }
            cycleCells.forEach { (cell, dependencies) -> getCell(cell)?.markHasCycleDependencies(dependencies) }

            evaluationOrder.forEach { dataExpiredCallback(it) }
            cycleCells.forEach { dataExpiredCallback(it.key) }
        }
    }

    private fun createCell(pointer: CellPointer, value: String? = null): TableCell {
        return TableCell(value, pointer, this, evaluator, onEvaluatedCallback).also {
            data[pointer] = it
        }
    }
}
