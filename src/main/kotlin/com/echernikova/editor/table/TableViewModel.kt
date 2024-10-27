package com.echernikova.editor.table

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.editor.table.model.TableData
import com.echernikova.evaluator.core.Evaluator
import javax.swing.table.DefaultTableModel

private const val DEFAULT_PAGE_SIZE = 100

class TableViewModel(
    initialData: Array<Array<Any?>>?,
    val tableData: TableData,
): DefaultTableModel(initialData, getColumns()) {

    private var isLoading = false
    private val lock = Any()

    init {
        tableData.initData(dataVector)
        tableData.setOnDataExpiredCallback { cellPointer: CellPointer ->
            fireTableCellUpdated(cellPointer.row, cellPointer.column)
        }

        addTableModelListener { event ->
            if (event.lastRow < 0 || event.column < 0) return@addTableModelListener
            if (event.lastRow >= rowCount || event.column >= columnCount) return@addTableModelListener

                val cellValue = getValueAt(event.lastRow, event.column)?.toString()
                tableData.setValueToCell(CellPointer(event.lastRow, event.column), cellValue)
        }
    }

    override fun addRow(rowData: Array<Any?>) {
        tableData.addRow(rowCount, rowData)
        super.addRow(rowData)
    }

    override fun isCellEditable(row: Int, column: Int): Boolean {
        return column != 0
    }

    fun loadNextPage() {
        if (isLoading) return

        synchronized(lock) {
            isLoading = true
            getDataForNewPage().forEach {
                addRow(it)
            }
            isLoading = false
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

    companion object {
        private fun getColumns(): Array<Char> {
            return Array(27) { if (it == 0) ' ' else 'A' + it - 1 }
        }
    }
}
