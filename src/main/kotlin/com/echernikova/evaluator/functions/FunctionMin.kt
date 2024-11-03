package com.echernikova.evaluator.functions

import com.echernikova.evaluator.core.*

class FunctionMin : Function {
    override val name = "MIN"

    override fun evaluate(context: Context, args: List<EvaluationResult<*>?>): FinalEvaluationResult<*> {
        args.findError()?.let { return it }
        val dependencies = args.getDependencies()

        if (args.size < 2) return ErrorEvaluationResult(
            evaluatedValue = "Function 'MIN' should have at least 2 arguments.",
            cellDependencies = dependencies
        )

        val numberArgs = args.castToCommonNumberType()
            ?: return ErrorEvaluationResult(
                evaluatedValue = "Function 'MIN' supports only number arguments.",
                cellDependencies = dependencies
            )

        when (numberArgs.firstOrNull()) {
            is Int -> return DataEvaluationResult((numberArgs as List<Int>).min(), dependencies)
            is Double -> return DataEvaluationResult((numberArgs as List<Double>).min(), dependencies)
        }

        return ErrorEvaluationResult("Unknown error on 'MIN' evaluation", dependencies)
    }
}
