package com.echernikova.evaluator.core

import com.echernikova.editor.table.model.TableData
import com.echernikova.evaluator.core.tokenizing.Tokenizer
import com.echernikova.evaluator.core.parsing.Parser
import com.echernikova.evaluator.functions.Function

class Evaluator (
    private val declaredFunctions: Map<String, Function>,
) {
    fun evaluate(value: String, tableData: TableData): FinalEvaluationResult<*> {
        if (value.isEmpty()) return DataEvaluationResult(
            evaluatedValue = EvaluationResult.Empty,
            cellDependencies = emptySet()
        )

        if (value.startsWith("=")) {
            val context = Context(tableData, declaredFunctions)
            val substring = value.substring(1, value.length)

            val parsingResult = runCatching {
                val tokenizedResult = Tokenizer.tokenize(substring)
                Parser.parse(tokenizedResult)
            }.onFailure { e ->
                return ErrorEvaluationResult(
                    evaluatedValue = e.message,
                    cellDependencies = emptySet()
                )
            }

            return (parsingResult.getOrNull()?.evaluate(context) as? FinalEvaluationResult) ?: ErrorEvaluationResult(
                evaluatedValue = "Can't display cell range.",
                cellDependencies = emptySet()
            )
        } else {
            return tryToParseAsLiteral(value)
        }
    }

    private fun tryToParseAsLiteral(str: String): FinalEvaluationResult<*> {
        str.toIntOrNull()?.let {
            return DataEvaluationResult(
                evaluatedValue = it,
                cellDependencies = emptySet()
            )
        }

        str.toDoubleOrNull()?.let {
            return DataEvaluationResult(
                evaluatedValue = it,
                cellDependencies = emptySet()
            )
        }

        str.toBooleanStrictOrNull()?.let {
            return DataEvaluationResult(
                evaluatedValue = it,
                cellDependencies = emptySet()
            )
        }

        return DataEvaluationResult(
            evaluatedValue = str,
            cellDependencies = emptySet()
        )
    }
}