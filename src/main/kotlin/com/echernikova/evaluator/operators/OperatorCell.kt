package com.echernikova.evaluator.operators

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.evaluator.core.*
import com.echernikova.evaluator.core.tokenizing.Token
import kotlin.math.max
import kotlin.math.min

interface OperatorCell: Operator {
    fun Token.Cell.CellLink.getCellPosition(): CellPointer {
        val column = name[0] - 'A' + 1
        if (column < 0 || column >= 21) throw EvaluationException("Cell link ${name} is incorrect.")
        val row = name.substring(1, name.length).toIntOrNull()
            ?: throw EvaluationException("Cell link ${name} is incorrect.")

        return CellPointer(row, column)
    }
}

class OperatorCellLink(
    private val link: Token.Cell.CellLink,
): OperatorCell {
    private val cellPosition by lazy { link.getCellPosition() }

    override fun evaluate(context: Context): EvaluationResult<*> {
        if (cellPosition.row < 0 || cellPosition.column > 27 || cellPosition.column < 0) {
            return ErrorEvaluationResult("Incorrect cell link ${link.name}", emptyList())
        }

        val link = context.table.getCell(cellPosition) ?: run {
            return EmptyCellEvaluationResult(
                evaluatedValue = null,
                cellDependencies = emptyList()
            )
        }

        if (link.evaluating) {
            return ErrorEvaluationResult(
                evaluatedValue = "Cycle dependencies!",
                cellDependencies = listOf(cellPosition)
            )
        }

        return link.evaluationResult?.copyWith(
            newValue = null,
            newDependencies = listOf(cellPosition)
        ) ?: ErrorEvaluationResult(
            evaluatedValue = "Cell with link $cellPosition is not evaluated.",
            cellDependencies = listOf(cellPosition)
        )
    }
}

class OperatorCellRange(
    private val from: Token.Cell.CellLink,
    private val to: Token.Cell.CellLink,
): OperatorCell {
    override fun evaluate(context: Context): EvaluationResult<*> = CellRangeEvaluationResult(
        evaluatedValue = this,
        cellDependencies = buildCellDependenciesInBetween(),
    )

    private fun buildCellDependenciesInBetween(): MutableList<CellPointer> {
        val (row1, column1) = from.getCellPosition()
        val (row2, column2) = to.getCellPosition()

        val dependencies = mutableListOf<CellPointer>()

        for (i in min(row1, row2)..max(row1, row2)) {
            for (j in min(column1, column2)..max(column1, column2)) {
                dependencies.add(CellPointer(i, j))
            }
        }
        return dependencies
    }
}
