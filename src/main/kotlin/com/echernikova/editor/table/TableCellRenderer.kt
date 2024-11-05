package com.echernikova.editor.table

import com.echernikova.editor.table.model.EvaluatingTableModel
import com.echernikova.editor.table.model.TableCell
import com.echernikova.evaluator.core.*
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

        if (column == 0) {
            setNormalValue(null)
            cell.text = (row + 1).toString()
            cell.horizontalAlignment = LEFT
            return cell
        }

        val cellEvaluationData = viewModel.getValueAt(row, column) as? TableCell ?: return cell

        val result = cellEvaluationData.getEvaluationResult()
        val evaluatedResult = result.evaluatedValue

        when (result) {
            is ErrorEvaluationResult -> {
                cell.horizontalAlignment = CENTER
                cell.font = TableTheme.currentTheme.errorCellFont
                cell.foreground = TableTheme.currentTheme.errorCellColor
                cell.text = ERROR_MESSAGE
            }

            is DataEvaluationResult -> {
                when (evaluatedResult) {
                    is Int, is String -> {
                        cell.horizontalAlignment = RIGHT
                        setNormalValue(result)
                    }
                    is Double, is Boolean -> {
                        cell.horizontalAlignment = LEFT
                        setNormalValue(result)
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

    private fun JLabel.setNormalValue(evaluationResult: EvaluationResult<*>?) {
        font = TableTheme.currentTheme.normalCellFont
        foreground = TableTheme.currentTheme.normalCellColor
        text = evaluationResult?.evaluatedValue.toString()
    }
}