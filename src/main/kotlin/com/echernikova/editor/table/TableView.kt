package com.echernikova.editor.table

import org.koin.mp.KoinPlatform.getKoin
import javax.swing.JTable

private const val DEFAULT_CELL_MIN_WIDTH = 30
class TableView(
    private val tableModel: TableViewModel
) : JTable(tableModel) {

    init {
        configureTable()
    }

    private fun configureTable() {
        autoResizeMode = AUTO_RESIZE_OFF

        for (i in 0 until columnCount) {
            val column = columnModel.getColumn(i)
            column.minWidth = DEFAULT_CELL_MIN_WIDTH
        }

        setShowGrid(true)
        gridColor = TableTheme.gridColor

        setDefaultRenderer(Any::class.java, TableCellRenderer(tableModel.tableData))
    }
}
