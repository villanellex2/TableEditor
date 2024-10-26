package com.echernikova.evaluator.functions

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.evaluator.core.*
import com.echernikova.evaluator.operators.OperatorCellLink

class FunctionVLookUp : Function {
    override val name = "VLOOKUP"

    /**
     * Finds args[0] in table in arg[1] and return value in column arg[2].
     */
    override fun evaluate(context: Context, args: List<EvaluationResult<*>?>): EvaluationResult<*> {
        args.findError()?.let { return it }
        val dependencies = args.getDependencies()

        val valueToFind = args.getOrNull(0)
        if (valueToFind is ErrorEvaluationResult) {
            return valueToFind
        }
        val range = args.getOrNull(1) as? CellRangeEvaluationResult
        val column = args.getOrNull(2)?.evaluatedValue as? Int

        if (args.size != 3 || valueToFind == null || range == null || column == null) {
            return ErrorEvaluationResult(
                evaluatedValue = "Function 'VLOOKUP' should have exactly 3 arguments: value to find, table, column to return.",
                cellDependencies = dependencies
            )
        }

        val error = ErrorEvaluationResult(
            evaluatedValue = "Incorrect cell link.",
            cellDependencies = dependencies
        )

        val minColumn = range.evaluatedValue.first().cellPosition?.column ?: return error
        val maxColumn = range.evaluatedValue.last().cellPosition?.column ?: return error

        if (minColumn + column > maxColumn + 1 ) {
            return ErrorEvaluationResult(
                evaluatedValue = "Column $column is out of bounds.",
                cellDependencies = dependencies
            )
        }

        val foundValue = range.evaluatedValue.firstOrNull { it.evaluate(context).evaluatedValue == valueToFind.evaluatedValue }

        if (foundValue == null) return ErrorEvaluationResult("Couldn't find cell with value ${valueToFind.evaluatedValue}.", dependencies)
        if (foundValue.cellPosition == null) return error

        return OperatorCellLink(CellPointer(row = foundValue.cellPosition.row, column = column + minColumn - 1))
            .evaluate(context).copyWithDependencies(dependencies)
    }
}
