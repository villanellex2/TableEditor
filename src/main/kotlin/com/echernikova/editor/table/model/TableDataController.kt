package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.Evaluator
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

    var dataExpiredCallback: ((CellPointer) -> Unit) = {}

    /**
     * Init data using JTable data, evaluates cells values after table init.
     */
    fun initData(sharingVector: Vector<Vector<Any?>>) {
        assert(data.isEmpty()) { "Data should be empty on init" }

        sharingVector.forEachIndexed { row, vector ->
            vector.forEachIndexed { column, value ->
                if (value != null) {
                    data[CellPointer(row, column)] = TableCell(value.toString(), CellPointer(row, column)).also {
                        dependenciesGraph.initDependencies(it)
                    }
                }
            }
        }
        val evaluatedData = mutableSetOf<CellPointer>()

        data.forEach { (_, cell) ->
            if (evaluatedData.contains(cell.cellPointer)) return@forEach

            dependenciesGraph.initDependencies(cell)
            val evaluationOrder = dependenciesGraph.getCellsToEvaluate(cell)

            evaluationOrder.forEach { pointer ->
                if (evaluatedData.contains(pointer)) return@forEach

                val tableCell = getOrCreateCell(pointer)
                tableCell?.let { cell ->
                    cell.evaluate(evaluator, this)
                    dependenciesGraph.updateDependencies(cell)
                }
            }
            evaluatedData.addAll(evaluationOrder)
        }

        data.forEach {
            dataExpiredCallback(it.key)
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
        return data.getOrPut(pointer) { createCell(pointer) }
    }

    /**
     * Updates value of the cell.
     * Also causes reevaluation of all the dependent cells.
     */
    fun setValueToCell(pointer: CellPointer, value: String?) {
        val cell = getOrCreateCell(pointer)
        if (cell.rawValue == value) return
        cell.rawValue = value

        evaluateInCorrectOrder(cell)
    }

    fun addRow(row: Int, values: Array<Any?>) {
        values.mapIndexedNotNull { i, value ->
            if (i != 0 && value != null) {
                createCell(CellPointer(row, i), value.toString()).also { evaluateInCorrectOrder(it) }
            } else null
        }
    }

    private fun evaluateInCorrectOrder(cell: TableCell) {
        cell.evaluate(evaluator, this)
        dependenciesGraph.updateDependencies(cell)
        val evaluationOrder = dependenciesGraph.getCellsToUpdate(cell)

        evaluationOrder.forEach { getCell(it)?.evaluate(evaluator, this) }
        evaluationOrder.forEach { dataExpiredCallback(it) }
    }

    private fun createCell(pointer: CellPointer, value: String? = null): TableCell {
        val cell = TableCell(value, pointer)
        data[pointer] = cell
        dependenciesGraph.initDependencies(cell)

        return cell
    }
}
