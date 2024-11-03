package com.echernikova.evaluator.functions

import com.echernikova.evaluator.core.*

interface Function {
    val name: String

    fun evaluate(context: Context, args: List<EvaluationResult<*>?>): EvaluationResult<*>
}

fun List<EvaluationResult<*>?>.findError(): ErrorEvaluationResult? {
    return (firstOrNull { it is ErrorEvaluationResult } as? ErrorEvaluationResult)
}

/**
 * Returns a list of arguments cast to common number type.
 * Order is not guaranteed.
 */
fun List<EvaluationResult<*>?>.castToCommonNumberType(): List<Number>? {
    val (cells, notCells) = partition { it is CellRangeEvaluationResult  }
    val args: List<EvaluationResult<*>?> = (notCells + cells.map { it?.evaluatedValue as List<EvaluationResult<*>?> }.flatten())

    if (args.any {
            (it?.evaluatedValue !is Int &&
            it?.evaluatedValue !is Double &&
            it?.evaluatedValue != EvaluationResult.Empty) ||
            it.evaluatedValue == null ||
            it !is DataEvaluationResult
        }
    ) {
        return null
    }

    return if (args.any { it?.evaluatedValue is Double }) {
        args.mapNotNull { it?.tryConvertToDouble()?.evaluatedValue }
    } else {
        args.mapNotNull { it?.tryConvertToInt()?.evaluatedValue }
    }
}

fun List<EvaluationResult<*>?>.getDependencies() = mapNotNull { it?.cellDependencies }.flatten().toSet()
