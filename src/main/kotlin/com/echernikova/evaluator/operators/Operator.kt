package com.echernikova.evaluator.operators

import com.echernikova.evaluator.core.Context
import com.echernikova.evaluator.core.EvaluationResult

interface Operator {
    fun evaluate(context: Context): EvaluationResult
}
