package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.*

class TableCell(
    initialValue: String?,
    val cellPointer: CellPointer,
) {
    var rawValue: String? = initialValue
    var evaluationResult: FinalEvaluationResult<*>? = null

    fun evaluate(evaluator: Evaluator, tableDataController: TableDataController) {
        val value = rawValue

        if (value.isNullOrEmpty()) {
            evaluationResult = DataEvaluationResult(EvaluationResult.Empty, emptySet())
            return
        }

        evaluationResult = evaluator.evaluate(value, tableDataController)
    }
}
