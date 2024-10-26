package com.echernikova.evaluator.functions

import com.echernikova.evaluator.core.Context
import com.echernikova.evaluator.core.EvaluationException
import com.echernikova.evaluator.core.EvaluationResult
import com.echernikova.evaluator.core.tokenizing.Token

class FunctionSum: Function {
    override val name = "SUM"
    override fun evaluate(context: Context, args: List<Any?>): EvaluationResult {
        TODO("Not yet implemented")
    }
}