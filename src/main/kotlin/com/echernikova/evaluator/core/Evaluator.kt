package com.echernikova.evaluator.core

import com.echernikova.editor.table.model.TableData
import com.echernikova.evaluator.core.tokenizing.Tokenizer
import com.echernikova.evaluator.core.parsing.Parser
import com.echernikova.evaluator.functions.Function

class Evaluator (
    private val declaredFunctions: Map<String, Function>,
) {
    fun evaluate(value: String, tableData: TableData): EvaluationResult {
        if (value.isNullOrEmpty()) return EvaluationResult(
            evaluatedValue = "",
            evaluatedType = EvaluationResultType.String
        )

        if (value.startsWith("=")) {
            val context = Context(tableData, declaredFunctions)
            val substring = value.substring(1, value.length)

            val parsingResult = runCatching {
                val tokenizedResult = Tokenizer.tokenize(substring)
                Parser.parse(tokenizedResult)
            }.onFailure { e ->
                return EvaluationResult(
                    evaluatedType = EvaluationResultType.Error,
                    evaluatedValue = null,
                    evaluatedError = e as? EvaluationException
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