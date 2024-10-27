package com.echernikova.editor.table

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.editor.table.model.TableData
import com.echernikova.evaluator.core.*
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

private const val ERROR_MESSAGE = "ERROR!"

class TableCellRenderer(
    val tableData: TableData,
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
            cell.text = row.toString()
            cell.horizontalAlignment = LEFT
            return cell
        }

        val cellEvaluationData = tableData.getCell(CellPointer(row, column)) ?: return cell
        val result = cellEvaluationData.evaluationResult
        val value = result?.evaluatedValue

        when (result) {
            is ErrorEvaluationResult -> {
                cell.horizontalAlignment = CENTER
                cell.font = TableTheme.errorCellFont
                cell.foreground = TableTheme.errorCellColor
                cell.text = result.evaluatedValue
            }

            is DataEvaluationResult -> {
                when (value) {
                    is Int, String -> {
                        cell.horizontalAlignment = RIGHT
                        setNormalValue(cellEvaluationData.evaluationResult)
                    }
                    is Double, Boolean -> {
                        cell.horizontalAlignment = LEFT
                        setNormalValue(cellEvaluationData.evaluationResult)
                    }
                    is EvaluationResult.Empty -> {
                        cell.text = ""
                    }
                }
            }

            null -> {
                cell.text = ""
            }
        }
        return cell
    }

    private fun JLabel.setNormalValue(evaluationResult: EvaluationResult<*>?) {
        font = TableTheme.normalCellFont
        foreground = TableTheme.normalCellColor
        text = evaluationResult?.evaluatedValue.toString()
    }
}