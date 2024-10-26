package com.echernikova.editor.table

import com.echernikova.editor.table.model.TableData
import com.echernikova.evaluator.core.EvaluationResultType
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

private const val ERROR_MESSAGE = "ERROR!"

class TableCellRenderer(
    private val tableData: TableData,
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
        val cellEvaluationData = tableData.getCell(row, column) ?: return cell

        when (cellEvaluationData.evaluationResult?.evaluatedType) {
            EvaluationResultType.Error -> {
                cell.horizontalAlignment = CENTER
                cell.font = TableTheme.errorCellFont
                cell.foreground = TableTheme.errorCellColor
                cell.text = ERROR_MESSAGE
            }

            EvaluationResultType.Int, EvaluationResultType.String -> {
                cell.horizontalAlignment = RIGHT
                cell.font = TableTheme.normalCellFont
                cell.foreground = TableTheme.normalCellColor
                cell.text = cellEvaluationData.evaluationResult?.evaluatedValue.toString()
            }

            EvaluationResultType.Double -> {
                cell.horizontalAlignment = LEFT
                cell.font = TableTheme.normalCellFont
                cell.foreground = TableTheme.normalCellColor
                cell.text = cellEvaluationData.evaluationResult?.evaluatedValue.toString()
            }

            EvaluationResultType.Boolean -> {
                cell.horizontalAlignment = LEFT
                cell.font = TableTheme.normalCellFont
                cell.foreground = TableTheme.normalCellColor
                cell.text = cellEvaluationData.evaluationResult?.evaluatedValue.toString()
            }

            EvaluationResultType.CellLink -> {
                cell.text = "Error, cell link in the end evaluation!"
            }

            null -> {
                cell.text = ""
            }
        }

        return cell
    }
}