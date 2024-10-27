package com.echernikova.evaluator.operators

import com.echernikova.evaluator.core.*
import com.echernikova.evaluator.core.tokenizing.Token

/**
 * Atomic final elements, such as String, Integer, Bool, Double.
 */
class OperatorLiteral(
    private val token: Token.Literal
): Operator {
    override fun evaluate(context: Context): EvaluationResult<*> {
        return DataEvaluationResult(
            evaluatedValue = token.value,
            cellDependencies = emptyList()
        )
    }
}
