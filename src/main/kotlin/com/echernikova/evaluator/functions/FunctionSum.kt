package com.echernikova.evaluator.functions

import com.echernikova.evaluator.core.*

class FunctionSum : Function {
    override val name = "SUM"

    override fun evaluate(context: Context, args: List<EvaluationResult<*>?>): FinalEvaluationResult<*> {
        args.findError()?.let { return it }
        val dependencies = args.getDependencies()

        if (args.size < 2) return ErrorEvaluationResult(
            evaluatedValue = "Function 'SUM' should have at least 2 arguments.",
            cellDependencies = dependencies
        )

        val numberArgs = args.castToCommonNumberType()
            ?: return ErrorEvaluationResult(
                evaluatedValue = "Function 'SUM' supports only number arguments.",
                cellDependencies = dependencies
            )

        when (numberArgs.firstOrNull()) {
            is Int -> {
                (numberArgs as List<Int>).sumOf { it.toLong() }.takeIf { it in Int.MIN_VALUE..Int.MAX_VALUE }?.also {
                    return DataEvaluationResult(it.toInt(), dependencies)
                } ?: return ErrorEvaluationResult("Overflow!", dependencies)
            }
            is Double -> {
                (numberArgs as List<Double>).sumOf { it }.takeIf { it.isFinite() }?.also {
                    return DataEvaluationResult(it, dependencies)
                } ?: return ErrorEvaluationResult("Overflow!", dependencies)
            }
        }

        return ErrorEvaluationResult("Unknown error on 'SUM' evaluation", dependencies)
    }
}
