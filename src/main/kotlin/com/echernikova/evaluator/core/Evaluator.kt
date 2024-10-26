package com.echernikova.evaluator.core

import com.echernikova.evaluator.core.tokenizing.Tokenizer
import com.echernikova.evaluator.core.parsing.Parser

class Evaluator (
    private val context: Context,
) {
    fun evaluate(value: String): EvaluationResult {
        if (value.isNullOrEmpty()) return EvaluationResult(
            evaluatedValue = "",
            evaluatedType = EvaluationResultType.String
        )

        if (value.startsWith("=")) {
            val substring = value.substring(1, value.length)

            val parsingResult = runCatching {
                val tokenizedResult = Tokenizer.tokenize(substring)
                Parser.parse(tokenizedResult)
            }.onFailure {
                return EvaluationResult(
                    evaluatedType = EvaluationResultType.Error,
                    evaluatedValue = null
                )
            }

            return parsingResult.getOrNull()?.evaluate(context) ?: EvaluationResult(
                evaluatedType = EvaluationResultType.Error,
                evaluatedValue = null
            )
        } else {
            return tryToParseAsLiteral(value)
        }
    }

    private fun tryToParseAsLiteral(str: String): EvaluationResult {
        str.toIntOrNull()?.let {
            return EvaluationResult(
                evaluatedValue = it,
                evaluatedType = EvaluationResultType.Int
            )
        }

        str.toDoubleOrNull()?.let {
            return EvaluationResult(
                evaluatedValue = it,
                evaluatedType = EvaluationResultType.Double
            )
        }

        str.toBooleanStrictOrNull()?.let {
            return EvaluationResult(
                evaluatedValue = it,
                evaluatedType = EvaluationResultType.Boolean
            )
        }

        return EvaluationResult(
            evaluatedValue = str,
            evaluatedType = EvaluationResultType.String
        )
    }
}