package com.echernikova.evaluator.operators

import com.echernikova.evaluator.core.*
import com.echernikova.evaluator.core.tokenizing.Token

private val ERROR_MESSAGE_NUMBERS = { s: String -> "Unary operator '$s' supports only numbers as argument." }

/**
 * Unary plus and minus evaluation.
 */
class OperatorUnary(
    private val operator: Token.Operator.Unary,
    private val arg: Operator,
): Operator {

    override fun evaluate(context: Context): EvaluationResult<*> {
        val argEvaluated = arg.evaluate(context)

        val error = ErrorEvaluationResult(
            evaluatedValue = ERROR_MESSAGE_NUMBERS(operator.symbol),
            cellDependencies = argEvaluated.cellDependencies
        )
        val value = argEvaluated.evaluatedValue

        if (value !is Number) return error
        if (argEvaluated !is DataEvaluationResult) return error

        return when (operator) {
            Token.Operator.Unary.Plus -> argEvaluated
            Token.Operator.Unary.Minus -> {
                when (value) {
                    is Int -> DataEvaluationResult(
                        evaluatedValue = -value,
                        cellDependencies = emptySet()
                    )
                    is Double -> DataEvaluationResult(
                        evaluatedValue = -value,
                        cellDependencies = emptySet()
                    )
                    else -> error
                }
            }
        }
    }
}
