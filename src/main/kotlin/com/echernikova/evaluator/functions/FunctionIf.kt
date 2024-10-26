package com.echernikova.evaluator.functions

import com.echernikova.evaluator.core.*

class FunctionIf : Function {
    override val name = "IF"

    override fun evaluate(context: Context, args: List<EvaluationResult<*>?>): EvaluationResult<*> {
        args.findError()?.let { return it }

        if (args.size != 3) return ErrorEvaluationResult(
            evaluatedValue = "Function 'IF' should have 3 arguments.",
            cellDependencies = emptySet()
        )

        val firstArgument = args[0]?.tryConvertToBoolean() ?: return ErrorEvaluationResult(
            evaluatedValue = "First argument of function 'IF' should be boolean value.",
            cellDependencies = args[0]?.cellDependencies ?: emptySet()
        )

        val res = if (firstArgument.evaluatedValue) args[1] else args[2]

        return res?.copyWithDependencies(firstArgument.cellDependencies) ?: ErrorEvaluationResult(
            "Unknown error on 'IF' evaluation.",
            (args[0]?.cellDependencies ?: emptySet())
        )
    }
}
