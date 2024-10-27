package com.echernikova.evaluator.core

import com.echernikova.editor.table.model.TableData
import com.echernikova.evaluator.functions.Function

data class Context(
    val table: TableData,
    val declaredFunctions: Map<String,Function>,
)
