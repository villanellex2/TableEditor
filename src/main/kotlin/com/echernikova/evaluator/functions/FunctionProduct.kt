package com.echernikova.evaluator.functions

import com.echernikova.evaluator.core.*

class FunctionProduct : Function {
    override val name = "PRODUCT"

    override fun evaluate(context: Context, args: List<EvaluationResult<*>?>): FinalEvaluationResult<*> {
        args.findError()?.let { return it }
        val dependencies = args.getDependencies()

        if (args.isEmpty()) return ErrorEvaluationResult(
            evaluatedValue = "Function 'PRODUCT' should have at least 1 argument.",
            cellDependencies = dependencies
        )

        val numberArgs = args.castToCommonNumberType()
            ?: return ErrorEvaluationResult(
                evaluatedValue = "Function 'PRODUCT' supports only number arguments.",
                cellDependencies = dependencies
            )

        when (numberArgs.firstOrNull()) {
            is Int -> {
                var res = 1L
                (numberArgs as List<Int>).forEach {
                    res *= it
                    if (res !in Int.MIN_VALUE..Int.MAX_VALUE) return ErrorEvaluationResult("Overflow!", dependencies)
                }
            }
            is Double -> {
                var res = 1.0
                (numberArgs as List<Double>).forEach {
                    res *= it
                    if (res.isInfinite()) return ErrorEvaluationResult("Overflow!", dependencies)
                }
            }
        }

        return ErrorEvaluationResult("Unknown error on 'PRODUCT' evaluation", dependencies)
    }
}
