package com.echernikova.editor.table

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.editor.table.model.TableDataController
import com.echernikova.evaluator.core.*
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

private const val ERROR_MESSAGE = "ERROR!"

class TableCellRenderer(
    private val tableDataController: TableDataController,
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

        val cellEvaluationData = tableDataController.getCell(CellPointer(row, column)) ?: return cell
        val result = cellEvaluationData.getEvaluationResult()
        val value = result.evaluatedValue

        when (result) {
            is ErrorEvaluationResult -> {
                cell.horizontalAlignment = CENTER
                cell.font = TableTheme.errorCellFont
                cell.foreground = TableTheme.errorCellColor
                cell.text = result.evaluatedValue
            }

            is DataEvaluationResult -> {
                when (value) {
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
        return cell
    }

    private fun JLabel.setNormalValue(evaluationResult: EvaluationResult<*>?) {
        font = TableTheme.normalCellFont
        foreground = TableTheme.normalCellColor
        text = evaluationResult?.evaluatedValue.toString()
    }
}