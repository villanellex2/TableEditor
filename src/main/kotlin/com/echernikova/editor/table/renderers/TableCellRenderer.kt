package com.echernikova.editor.table.renderers

import com.echernikova.editor.table.TableTheme
import com.echernikova.editor.table.model.EvaluatingTableModel
import com.echernikova.evaluator.core.DataEvaluationResult
import com.echernikova.evaluator.core.ErrorEvaluationResult
import com.echernikova.evaluator.core.EvaluationResult
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableCellRenderer

private const val ERROR_MESSAGE = "ERROR!"


class TableCellRenderer(
    private val viewModel: EvaluatingTableModel,
) : DefaultTableCellRenderer() {

    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        val rawCell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        val cell = rawCell as? JLabel ?: return rawCell

        cell.setNormalValue()

        if (column == 0) {
            cell.text = (row + 1).toString()
            cell.horizontalAlignment = LEFT
            cell.font = TableTheme.currentTheme.markersCellFont
            cell.foreground = TableTheme.currentTheme.markersCellFontColor
            return cell
        }

        val cellEvaluationData = viewModel.getValueAt(row, column) ?: return cell

        val result = cellEvaluationData.getEvaluationResult()
        val evaluatedResult = result.evaluatedValue

        when (result) {
            is ErrorEvaluationResult -> {
                cell.horizontalAlignment = CENTER
                cell.font = TableTheme.currentTheme.errorCellFont
                cell.foreground = TableTheme.currentTheme.errorCellFontColor
                cell.text = ERROR_MESSAGE
            }

            is DataEvaluationResult -> {
                when (evaluatedResult) {
                    is Int, is String -> {
                        cell.horizontalAlignment = RIGHT
                        cell.text = result.evaluatedValue?.toString() ?: ""
                    }

                    is Double, is Boolean -> {
                        cell.horizontalAlignment = LEFT
                        cell.text = result.evaluatedValue?.toString() ?: ""
                    }

                    is EvaluationResult.Empty -> {
                        cell.text = ""
                    }
                }
            }
        }

        cell.border = EmptyBorder(0, 8, 0, 8)
        return cell
    }

    private fun JLabel.setNormalValue() {
        font = TableTheme.currentTheme.normalCellFont
        foreground = TableTheme.currentTheme.normalCellFontColor
    }
}