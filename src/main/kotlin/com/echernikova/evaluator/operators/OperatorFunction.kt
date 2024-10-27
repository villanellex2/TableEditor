package com.echernikova.evaluator.operators

import com.echernikova.evaluator.core.Context
import com.echernikova.evaluator.core.EvaluationException
import com.echernikova.evaluator.core.EvaluationResult
import com.echernikova.evaluator.core.tokenizing.Token

class OperatorFunction(
    private val funcToken: Token.Function,
    private val args: List<Operator>
): Operator {
    override fun evaluate(context: Context): EvaluationResult<*> {
        val evaluatedArgs = args.map { it.evaluate(context) }
        val name = funcToken.name
        val function = context.declaredFunctions[name] ?: throw EvaluationException("Unsupported function '$name'")

        val argsValues = evaluatedArgs.map { it.evaluatedValue }
        return function.evaluate(context, argsValues).copyWith(
            newValue = null,
            newDependencies = evaluatedArgs.map { it.cellDependencies }.reduce { list1, list2 -> list1 + list2 }
        )
    }
}
