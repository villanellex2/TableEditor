package com.echernikova.evaluator.operators

import com.echernikova.evaluator.core.Context
import com.echernikova.evaluator.core.EvaluationException
import com.echernikova.evaluator.core.EvaluationResult
import com.echernikova.evaluator.core.EvaluationResultType
import com.echernikova.evaluator.core.tokenizing.Token
import kotlin.math.max
import kotlin.math.min

class OperatorCellRange(
    private val from: Token.Cell.CellLink,
    private val to: Token.Cell.CellLink,
): Operator {
    override fun evaluate(context: Context): EvaluationResult = EvaluationResult(
        evaluatedValue = this,
        evaluatedType = EvaluationResultType.CellLink,
        cellDependencies = buildCellDependenciesInBetween(),
    )

    private fun Token.Cell.CellLink.getCellPosition(): Pair<Int, Int> {
        val row = name[0] - 'A'
        if (row < 0 || row >= 21) throw EvaluationException("Cell link ${name} is incorrect.")
        val column = name.substring(1, name.length).toIntOrNull()
            ?: throw EvaluationException("Cell link ${name} is incorrect.")

        return row to column
    }

    private fun buildCellDependenciesInBetween(): MutableList<Pair<Int, Int>> {
        val (row1, column1) = from.getCellPosition()
        val (row2, column2) = to.getCellPosition()

        val dependencies = mutableListOf<Pair<Int,Int>>()

        for (i in min(row1, row2)..max(row1, row2)) {
            for (j in min(column1, column2)..max(column1, column2)) {
                dependencies.add(i to j)
            }
        }
        return dependencies
    }
}
