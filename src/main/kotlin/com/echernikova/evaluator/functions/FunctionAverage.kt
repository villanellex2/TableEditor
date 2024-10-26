package com.echernikova.evaluator.functions

import com.echernikova.evaluator.core.*

class FunctionAverage : Function {
    override val name = "AVERAGE"

    override fun evaluate(context: Context, args: List<EvaluationResult<*>?>): FinalEvaluationResult<*> {
        val sumRes = FunctionSum().evaluate(context, args)

        if (sumRes !is ErrorEvaluationResult) {
            val (cells, notCells) = args.partition { it is CellRangeEvaluationResult  }
            val args2: List<EvaluationResult<*>?> = (notCells + cells.map { it?.evaluatedValue as List<EvaluationResult<*>?> }.flatten())

            when (val res = sumRes.evaluatedValue) {
                is Int -> return DataEvaluationResult(res / args2.size, sumRes.cellDependencies)
                is Double -> return DataEvaluationResult(res / args2.size, sumRes.cellDependencies)
            }

        }
        return ErrorEvaluationResult("Error on AVERAGE evaluation.", sumRes.cellDependencies)
    }
}
