package com.echernikova.evaluator.core

import com.echernikova.editor.table.model.TableDataController
import com.echernikova.evaluator.functions.Function

data class Context(
    val table: TableDataController,
    val declaredFunctions: Map<String,Function>,
)
