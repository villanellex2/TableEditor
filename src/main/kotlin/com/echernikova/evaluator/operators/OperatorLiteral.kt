package com.echernikova.evaluator.operators

import com.echernikova.evaluator.core.Context
import com.echernikova.evaluator.core.EvaluationResult
import com.echernikova.evaluator.core.EvaluationResultType
import com.echernikova.evaluator.core.tokenizing.Token

interface OperatorLiteral: Operator {
    companion object {
        fun getFor(token: Token.Literal): OperatorLiteral {
            return when (token) {
                is Token.Literal.Int -> OperatorLiteralInt(token)
                is Token.Literal.Str -> OperatorLiteralString(token)
                is Token.Literal.Double -> OperatorLiteralDouble(token)
            }
        }
    }
}

class OperatorLiteralInt(private val token: Token.Literal.Int): OperatorLiteral {
    override fun evaluate(context: Context): EvaluationResult {
        return EvaluationResult(token.value, EvaluationResultType.Int)
    }
}

class OperatorLiteralString(private val token: Token.Literal.Str): OperatorLiteral {
    override fun evaluate(context: Context): EvaluationResult {
        return EvaluationResult(token.value, EvaluationResultType.String)
    }
}

class OperatorLiteralDouble(private val token: Token.Literal.Double): OperatorLiteral {
    override fun evaluate(context: Context): EvaluationResult {
        return EvaluationResult(token.value, EvaluationResultType.Double)
    }
}
