package com.echernikova.evaluator.functions

import com.echernikova.evaluator.core.*

interface Function {
    val name: String

    fun evaluate(context: Context, args: List<EvaluationResult<*>?>): EvaluationResult<*>
}

fun List<EvaluationResult<*>?>.findError(): ErrorEvaluationResult? {
    return (firstOrNull { it is ErrorEvaluationResult } as? ErrorEvaluationResult)
}

fun List<EvaluationResult<*>?>.castToCommonNumberType(): List<Number>? {
    if (any {
            (it?.evaluatedValue !is Int &&
            it?.evaluatedValue !is Double &&
            it?.evaluatedValue != EvaluationResult.Empty) ||
            it.evaluatedValue == null ||
            it !is DataEvaluationResult
        }
    ) {
        return null
    }

    return if (any { it?.evaluatedValue is Double }) {
        mapNotNull { it?.tryConvertToDouble()?.evaluatedValue }
    } else {
        mapNotNull { it?.tryConvertToInt()?.evaluatedValue }
    }
}

fun List<EvaluationResult<*>?>.getDependencies() = map { it?.cellDependencies }.filterNotNull().flatten().toSet()
