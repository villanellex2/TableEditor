package com.echernikova.evaluator.functions

import com.echernikova.evaluator.core.Context
import com.echernikova.evaluator.core.EvaluationResult

interface Function {
    val name: String

    fun evaluate(context: Context, args: List<Any?>): EvaluationResult
}