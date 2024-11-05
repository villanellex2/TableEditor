package com.echernikova.editor.table.renderers

import com.echernikova.editor.table.TableTheme
import java.awt.Component
import javax.swing.JTable
import javax.swing.border.LineBorder
import javax.swing.table.DefaultTableCellRenderer

class HeaderRenderer : DefaultTableCellRenderer() {

    init {
        font = TableTheme.currentTheme.markersCellFont
        foreground = TableTheme.currentTheme.markersCellFontColor
        border = LineBorder(TableTheme.currentTheme.gridColor)
        horizontalAlignment = LEFT
    }

    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

        background = if (table?.selectedColumn == column) {
            TableTheme.currentTheme.tableEditorBackgroundColor
        } else {
            TableTheme.currentTheme.secondaryBackground
        }

        return component
    }
}
