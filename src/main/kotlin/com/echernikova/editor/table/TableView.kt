package com.echernikova.editor.table

import com.echernikova.editor.table.model.EvaluatingTableModel
import com.echernikova.editor.table.renderers.TableCellEditor
import com.echernikova.editor.table.renderers.TableCellRenderer
import com.echernikova.evaluator.core.ErrorEvaluationResult
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.JTable
import javax.swing.ToolTipManager

class TableView(
    private val tableModel: EvaluatingTableModel
) : JTable(tableModel) {

    init {
        configureTable()
    }

    override fun getToolTipText(e: MouseEvent): String? {
        val p: Point = e.getPoint()
        val rowIndex = rowAtPoint(p)
        val colIndex = columnAtPoint(p)

        runCatching {
            val value = tableModel.getValueAt(rowIndex, colIndex)?.getEvaluationResult()
            if (value is ErrorEvaluationResult) {
                return value.evaluatedValue
            }
        }
        return null
    }

    private fun configureTable() {
        autoResizeMode = AUTO_RESIZE_OFF

        setShowGrid(true)
        gridColor = TableTheme.currentTheme.gridColor
        selectionBackground = TableTheme.currentTheme.tableSelectionBackgroundColor
        selectionForeground = TableTheme.currentTheme.tableSelectionFontColor

        rowHeight = 20

        setDefaultRenderer(Any::class.java, TableCellRenderer(tableModel))
        setDefaultEditor(Any::class.java, TableCellEditor())
        ToolTipManager.sharedInstance().registerComponent(this)
        ToolTipManager.sharedInstance().initialDelay = 0
    }
}
