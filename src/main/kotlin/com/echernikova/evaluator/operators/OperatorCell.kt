package com.echernikova.evaluator.operators

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.evaluator.core.*
import com.echernikova.evaluator.core.tokenizing.Token
import com.echernikova.fileopening.FileOpeningFrameViewModel
import org.koin.java.KoinJavaComponent.getKoin

interface OperatorCell: Operator

/**
 * Evaluation of other cell value, such as A2, C12 etc.
 */
class OperatorCellLink(val cellPosition: CellPointer): OperatorCell {
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


private fun getCellEvaluationResult(context: Context, cellPointer: CellPointer): EvaluationResult<*> {
    if (cellPointer.row < 0 || cellPointer.column > 27 || cellPointer.column < 0) {
        return ErrorEvaluationResult("Incorrect cell link", emptySet())
    }

    val link = context.table.getOrCreateCell(cellPointer)
    if (link.evaluationResult == null) link.evaluate(getKoin().get<Evaluator>(), context.table)

    return link.evaluationResult?.copyWithDependencies(setOf(cellPointer)) ?: ErrorEvaluationResult(
        evaluatedValue = "Error on evaluation $cellPointer.",
        cellDependencies = setOf(cellPointer)
    )
}
