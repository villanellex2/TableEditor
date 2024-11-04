package com.echernikova.editor.table

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.evaluator.core.ErrorEvaluationResult
import java.awt.Color
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.JTable
import javax.swing.ToolTipManager

class TableView(
    private val tableModel: TableViewModel
) : JTable(tableModel) {

    init {
        configureTable()
    }

    override fun getToolTipText(e: MouseEvent): String? {
        val p: Point = e.getPoint()
        val rowIndex = rowAtPoint(p)
        val colIndex = columnAtPoint(p)

        runCatching {
            val value = tableModel.tableDataController.getCell(CellPointer(rowIndex, colIndex))?.getEvaluationResult()
            if (value is ErrorEvaluationResult) {
                return value.evaluatedValue
            }
        }
        return null
    }

    private fun configureTable() {
        autoResizeMode = AUTO_RESIZE_OFF

        setShowGrid(true)
        gridColor = TableTheme.gridColor
        selectionBackground = TableTheme.tableSelectionBackgroundColor
        selectionForeground = TableTheme.tableSelectionForegroundColor

        rowHeight = 20

        setDefaultRenderer(Any::class.java, TableCellRenderer(tableModel.tableDataController))
        ToolTipManager.sharedInstance().registerComponent(this)
        ToolTipManager.sharedInstance().initialDelay = 0
    }
}
