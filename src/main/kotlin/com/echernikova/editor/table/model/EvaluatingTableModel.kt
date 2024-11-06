package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.Evaluator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import javax.swing.table.DefaultTableModel

private const val DEFAULT_PAGE_SIZE = 100

open class EvaluatingTableModel(
    initialData: Array<Array<String?>>?,
    private val evaluator: Evaluator,
    private val scope: CoroutineScope,
) : DefaultTableModel(initialData?.size ?: 0, getColumns().size) {

    private val dependenciesGraph = TableDependenciesGraph()
    private val lock = Any()

    init {
        initializeDataVector(initialData)
    }

    fun initColumnIdentifiers() {
        setColumnIdentifiers(getColumns())
    }

    fun getValueAt(pointer: CellPointer): TableCell? {
        synchronized(lock) {
            while (pointer.row >= rowCount) loadNextPage()
            return getValueAt(pointer.row, pointer.column)
        }
    }

    override fun setValueAt(value: Any?, row: Int, column: Int) {
        synchronized(lock) {
            while (column >= columnCount) loadNextPage()
            value ?: return
            setValueToCell(CellPointer(row, column), value)
        }
    }

    fun setValueAt(value: Any?, pointer: CellPointer) {
        synchronized(lock) {
            while (pointer.column >= columnCount) loadNextPage()
            value ?: return
            setValueToCell(pointer, value)
        }
    }

    override fun getValueAt(row: Int, column: Int): TableCell? {
        synchronized(lock) {
            while (row >= rowCount || column >= columnCount) loadNextPage()
            return super.getValueAt(row, column) as? TableCell
        }
    }

    override fun isCellEditable(row: Int, column: Int): Boolean {
        return column != 0
    }

    override fun addRow(rowData: Array<Any?>) {
        addNewRow(rowCount, rowData)
    }

    fun loadNextPage() {
        synchronized(lock) {
            getDataForNewPage().forEach {
                addRow(it)
            }
        }
    }

    private fun addNewRow(row: Int, values: Array<Any?>) {
        val newRow = Vector<Any?>(columnCount)
        values.forEachIndexed { i, value ->
            if (i == 0) newRow.add(null) else newRow.add(createTableCell(CellPointer(row, i), value.toString()))
        }
        for (i in newRow.size until columnCount) {
            newRow.add(null)
        }
        synchronized(lock) {
            dataVector.add(newRow)
        }
    }

    private fun getDataForNewPage(): List<Array<Any?>> {
        val data = mutableListOf<Array<Any?>>()
        val rowsBefore = rowCount - 1
        for (i in 1..DEFAULT_PAGE_SIZE) {
            data.add(arrayOf((rowsBefore + i).toString()))
        }
        return data
    }

    private fun initializeDataVector(initialData: Array<Array<String?>>?) {
        val createdCells = mutableListOf<TableCell>()
        initialData?.forEachIndexed { row, array ->
            array.forEachIndexed { column, value ->
                val cellPointer = CellPointer(row, column)
                dataVector[row][column] = createTableCell(cellPointer, value).also {
                    createdCells.add(it)
                }
            }
        }

        scope.launch {
            synchronized(lock) {
                createdCells.forEach { it.getEvaluationResult() }
            }
        }
    }

    private fun setValueToCell(pointer: CellPointer, value: Any) {
        val newValue = value as? String ?: return

        synchronized(lock) {
            val cell = getValueAt(pointer) ?: run {
                if (newValue.isNotEmpty()) {
                    createTableCell(pointer, newValue).also { newCell ->
                        dataVector[pointer.row][pointer.column] = newCell
                        evaluateCellDependencies(newCell)
                    }
                } else return
            }

            if (newValue.isEmpty()) {
                dataVector[pointer.row][pointer.column] = null
            } else {
                cell.rawValue = newValue
            }
            evaluateCellDependencies(cell)
        }
    }

    private fun createTableCell(pointer: CellPointer, rawValue: String?) =
        TableCell(rawValue, pointer, this, evaluator) { cell ->
            dependenciesGraph.updateDependencies(cell)
            fireTableCellUpdated(cell.pointer.row, cell.pointer.column)
        }

    private fun evaluateCellDependencies(cell: TableCell) {
        scope.launch {
            cell.evaluate()
            val (evaluationOrder, cycleCells) = dependenciesGraph.getCellsToUpdate(cell)

            synchronized(lock) {
                evaluationOrder.forEach {
                    val dependentCell = dataVector[it.row][it.column] as? TableCell
                    dependentCell?.evaluate()
                }
                cycleCells.forEach { (cellPointer, dependencies) ->
                    val cycleCell = dataVector[cellPointer.row][cellPointer.column] as? TableCell
                    cycleCell?.markHasCycleDependencies(dependencies)
                }
            }

            evaluationOrder.forEach { fireTableCellUpdated(it.row, it.column) }
            cycleCells.forEach { fireTableCellUpdated(it.key.row, it.key.column) }
        }
    }

    companion object {
        private fun getColumns(): Array<Char> {
            return Array(27) { if (it == 0) ' ' else 'A' + it - 1 }
        }
    }
}