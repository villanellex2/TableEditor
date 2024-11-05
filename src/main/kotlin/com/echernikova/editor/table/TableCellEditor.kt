package com.echernikova.editor.table

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.editor.table.model.TableCell
import java.awt.Component
import java.awt.ComponentOrientation
import javax.swing.*
import javax.swing.table.TableCellEditor
import javax.swing.text.DefaultCaret

class TableCellEditor : AbstractCellEditor(), TableCellEditor {

    private val textField = JTextField()

    init {
        textField.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        textField.background = TableTheme.currentTheme.tableEditorBackgroundColor
        textField.foreground = TableTheme.currentTheme.tableEditorForegroundColor
        textField.font = TableTheme.currentTheme.normalCellFont

        textField.componentOrientation = ComponentOrientation.LEFT_TO_RIGHT
        textField.caret = DefaultCaret().apply {
            height = 15
            blinkRate = 500
        }
    }

    override fun getTableCellEditorComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int
    ): Component {
        val tableCell = value as? TableCell ?: TableCell("", CellPointer(row, column))

        textField.text = tableCell.rawValue ?: ""
        return textField
    }

    override fun getCellEditorValue(): Any? {
        return textField.text
    }
}
