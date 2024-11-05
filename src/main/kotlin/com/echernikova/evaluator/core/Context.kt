package com.echernikova.evaluator.core

import com.echernikova.editor.table.model.EvaluatingTableModel
import com.echernikova.evaluator.functions.Function

data class Context(
    val table: EvaluatingTableModel,
    val declaredFunctions: Map<String,Function>,
)
