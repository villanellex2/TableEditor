package com.echernikova.evaluator.functions

import com.echernikova.evaluator.core.*

class FunctionAverage : Function {
    override val name = "AVERAGE"

    override fun evaluate(context: Context, args: List<EvaluationResult<*>?>): FinalEvaluationResult<*> {
        val sumRes = FunctionSum().evaluate(context, args)

        when (val res = sumRes.evaluatedValue) {
             is Int -> return DataEvaluationResult(res / args.size, sumRes.cellDependencies)
             is Double -> return DataEvaluationResult(res / args.size, sumRes.cellDependencies)
        }

        return ErrorEvaluationResult("Error on AVERAGE evaluation.", sumRes.cellDependencies)
    }
}
