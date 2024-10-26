package com.echernikova.evaluator.operators

import com.echernikova.evaluator.core.*
import com.echernikova.evaluator.core.tokenizing.Token

/**
 * Functions with names, such as SUM(args..), MIN(args..).
 */
class OperatorFunction(
    private val funcToken: Token.Function,
    private val args: List<Operator>
): Operator {
    override fun evaluate(context: Context): EvaluationResult<*> {
        val evaluatedArgs = args.map { it.evaluate(context) }
        val name = funcToken.name
        val function = context.declaredFunctions[name] ?: run {
            return ErrorEvaluationResult("Unsupported function '$name'", emptySet())
        }

        return function.evaluate(context, evaluatedArgs)
    }
}
