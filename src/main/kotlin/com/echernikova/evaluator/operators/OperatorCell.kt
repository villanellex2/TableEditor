package com.echernikova.evaluator.operators

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.evaluator.core.*
import com.echernikova.evaluator.core.tokenizing.Token

interface OperatorCell: Operator

/**
 * Evaluation of other cell value, such as A2, C12 etc.
 */
class OperatorCellLink(val cellPosition: CellPointer?): OperatorCell {
    constructor(link: Token.Cell.CellLink) : this(CellPointer.fromString(link.name))

    override fun evaluate(context: Context): EvaluationResult<*> {
        return getCellEvaluationResult(context, cellPosition)
    }
}

/**
 * Cell range is not final type. We can only pass it to some function as an argument.
 */
class OperatorCellRange(
    private val from: Token.Cell.CellLink,
    private val to: Token.Cell.CellLink,
): OperatorCell {
    override fun evaluate(context: Context): EvaluationResult<*> {
        val cellPointers = CellPointer.buildCellDependenciesInBetween(
            CellPointer.fromString(from.name), CellPointer.fromString(to.name)
        )

        val evaluatedValues = cellPointers.map { OperatorCellLink(it) }

        return CellRangeEvaluationResult(
            evaluatedValue = evaluatedValues,
            cellDependencies = cellPointers,
        )
    }
}


private fun getCellEvaluationResult(context: Context, cellPointer: CellPointer?): EvaluationResult<*> {
    if (cellPointer == null ||cellPointer.row < 0 || cellPointer.column > 27 || cellPointer.column < 0) {
        return ErrorEvaluationResult("Incorrect cell link", emptySet())
    }

    val link = context.table.getValueAt(cellPointer) ?: run {
        return DataEvaluationResult(EvaluationResult.Empty, setOf(cellPointer))
    }

    if (link.evaluating) {
        return ErrorEvaluationResult(
            evaluatedValue = "Cycle dependencies!",
            cellDependencies = setOf(cellPointer)
        )
    }

    return link.getEvaluationResult().copyWithDependencies(setOf(cellPointer), override = true)
}
