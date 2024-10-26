package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.*

class TableCell(
    var rawValue: String?,
    val pointer: CellPointer,
    private val tableDataController: EvaluatingTableModel? = null,
    private val evaluator: Evaluator? = null,
    private val onEvaluated: (TableCell) -> Unit = {}
) {
    private var evaluationResult: FinalEvaluationResult<*>? = null
    var evaluating = false
        private set

    fun getEvaluationResult(): FinalEvaluationResult<*> {
        return this.evaluationResult ?: evaluate()
    }

    fun markHasCycleDependencies(dependencies: Set<CellPointer>?) {
        evaluationResult = ErrorEvaluationResult(
            evaluatedValue = "Cycle dependencies!",
            cellDependencies = dependencies ?: emptySet(),
        )
    }

    fun evaluate(): FinalEvaluationResult<*> {
        val value = rawValue

        evaluating = true
        return if (value.isNullOrEmpty()) {
            DataEvaluationResult(EvaluationResult.Empty, emptySet())
        } else {
            tableDataController?.let { evaluator?.evaluate(value, tableDataController) } ?: ErrorEvaluationResult(
                "TableCell is not initialized!", emptySet()
            )
        }.also {
            evaluationResult = it
            evaluating = false
            onEvaluated(this)
        }
    }
}
