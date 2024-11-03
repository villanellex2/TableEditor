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

/**
 * Evaluation of other cell value, such as A2, C12 etc.
 */
class OperatorCellLink(
    private val link: Token.Cell.CellLink,
): OperatorCell {
    val cellPosition by lazy { link.getCellPosition() }

    override fun evaluate(context: Context): EvaluationResult<*> {
        if (cellPosition.row < 0 || cellPosition.column > 27 || cellPosition.column < 0) {
            return ErrorEvaluationResult("Incorrect cell link ${link.name}", emptySet())
        }

        val link = context.table.getCell(cellPosition) ?: run {
            return DataEvaluationResult(
                evaluatedValue = EvaluationResult.Empty,
                cellDependencies = emptySet()
            )
        }

        if (link.evaluating) {
            return ErrorEvaluationResult(
                evaluatedValue = "Cycle dependencies!",
                cellDependencies = setOf(cellPosition)
            )
        }

        return link.evaluationResult?.let {
            DataEvaluationResult(
                it.evaluatedValue,
                it.cellDependencies + setOf(cellPosition)
            )
        } ?: ErrorEvaluationResult(
            evaluatedValue = "Cell with link $cellPosition is not evaluated.",
            cellDependencies = setOf(cellPosition)
        )
    }
}

/**
 * Cell range is not final type. We can only pass it to some function as an argument.
 */
class OperatorCellRange(
    private val from: Token.Cell.CellLink,
    private val to: Token.Cell.CellLink,
): OperatorCell {
    override fun evaluate(context: Context): EvaluationResult<*> = CellRangeEvaluationResult(
        evaluatedValue = this,
        cellDependencies = buildCellDependenciesInBetween(),
    )

    private fun buildCellDependenciesInBetween(): MutableSet<CellPointer> {
        val (row1, column1) = from.getCellPosition()
        val (row2, column2) = to.getCellPosition()

        val dependencies = mutableSetOf<CellPointer>()

        for (i in min(row1, row2)..max(row1, row2)) {
            for (j in min(column1, column2)..max(column1, column2)) {
                dependencies.add(CellPointer(i, j))
            }
        }
        return dependencies
    }
}
