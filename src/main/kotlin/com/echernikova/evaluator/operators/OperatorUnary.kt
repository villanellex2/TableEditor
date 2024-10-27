package com.echernikova.evaluator.operators

import com.echernikova.evaluator.core.Context
import com.echernikova.evaluator.core.EvaluationResult
import com.echernikova.evaluator.core.tokenizing.Token

class OperatorUnary(
    private val operator: Token.Operator.Unary,
    private val arg: Operator,
): Operator {

    override fun evaluate(context: Context): EvaluationResult {
        val argEvaluated = arg.evaluate(context)
        val value = argEvaluated.evaluatedValue

        return when (operator) {
            Token.Operator.Unary.Plus -> {
                if (argEvaluated.evaluatedValue is Number) {
                    argEvaluated
                } else {
                    EvaluationResult.buildErrorResult(
                        "Unary operator '+' supports only numbers as argument.",
                        argEvaluated.cellDependencies
                    )
                }
            }

            Token.Operator.Unary.Minus -> {
                when (value) {
                    is Int ->  EvaluationResult.copyWithNewValue(argEvaluated, -value)
                    is Double -> EvaluationResult.copyWithNewValue(argEvaluated, -value)
                    else -> EvaluationResult.buildErrorResult(
                        "Unary operator '-' supports only numbers as argument.",
                        argEvaluated.cellDependencies
                    )
                }
            }
        }
    }
}