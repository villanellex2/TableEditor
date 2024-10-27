package com.echernikova.evaluator.operators

import com.echernikova.evaluator.core.*
import com.echernikova.evaluator.core.tokenizing.Token

class OperatorLiteral(
    private val token: Token.Literal
): Operator {
    override fun evaluate(context: Context): EvaluationResult<*> {
        return when (token) {
            is Token.Literal.Int -> IntegerEvaluationResult(
                token.value, emptyList()
            )
            is Token.Literal.Str -> StringEvaluationResult(
                token.value, emptyList()
            )
            is Token.Literal.Double -> DoubleEvaluationResult(
                token.value, emptyList()
            )
            is Token.Literal.Bool -> BooleanEvaluationResult(
                token.value, emptyList()
            )
        }
    }
}
