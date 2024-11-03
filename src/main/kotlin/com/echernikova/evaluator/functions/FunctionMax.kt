package com.echernikova.evaluator.functions

import com.echernikova.evaluator.core.*

class FunctionMax : Function {
    override val name = "MAX"

    override fun evaluate(context: Context, args: List<EvaluationResult<*>?>): FinalEvaluationResult<*> {
        args.findError()?.let { return it }
        val dependencies = args.getDependencies()

        if (args.size < 1) return ErrorEvaluationResult(
            evaluatedValue = "Function 'MAX' should have at least 1 argument.",
            cellDependencies = emptySet()
        )

        val numberArgs = args.castToCommonNumberType(context)
            ?: return ErrorEvaluationResult(
                evaluatedValue = "Function 'MAX' supports only number arguments.",
                cellDependencies = dependencies
            )

        when (numberArgs.firstOrNull()) {
            is Int -> return DataEvaluationResult((numberArgs as List<Int>).max(), dependencies)
            is Double -> return DataEvaluationResult((numberArgs as List<Double>).max(), dependencies)
        }

        return ErrorEvaluationResult("Unknown error on 'MAX' evaluation", dependencies)
    }
}
