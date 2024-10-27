package com.echernikova.evaluator.core

import com.echernikova.editor.table.model.TableData
import com.echernikova.evaluator.core.tokenizing.Tokenizer
import com.echernikova.evaluator.core.parsing.Parser
import com.echernikova.evaluator.functions.Function

class Evaluator (
    private val declaredFunctions: Map<String, Function>,
) {
    fun evaluate(value: String, tableData: TableData): EvaluationResult<*> {
        if (value.isEmpty()) return EmptyCellEvaluationResult(
            evaluatedValue = null,
            cellDependencies = emptyList()
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
                    cellDependencies = emptyList()
                )
            }

            return parsingResult.getOrNull()?.evaluate(context) ?: ErrorEvaluationResult(
                evaluatedValue = "",
                cellDependencies = emptyList()
            )
        } else {
            return tryToParseAsLiteral(value)
        }
    }

    private fun tryToParseAsLiteral(str: String): EvaluationResult<*> {
        str.toIntOrNull()?.let {
            return IntegerEvaluationResult(
                evaluatedValue = it,
                cellDependencies = emptyList()
            )
        }

        str.toDoubleOrNull()?.let {
            return DoubleEvaluationResult(
                evaluatedValue = it,
                cellDependencies = emptyList()
            )
        }

        str.toBooleanStrictOrNull()?.let {
            return BooleanEvaluationResult(
                evaluatedValue = it,
                cellDependencies = emptyList()
            )
        }

        return StringEvaluationResult(
            evaluatedValue = str,
            cellDependencies = emptyList()
        )
    }
}