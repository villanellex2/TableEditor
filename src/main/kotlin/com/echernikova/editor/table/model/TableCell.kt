package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.*

class TableCell(
    var rawValue: String?,
    val pointer: CellPointer,
    private val tableDataController: TableDataController,
    private val evaluator: Evaluator,
    private val onEvaluated: (TableCell) -> Unit
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
            evaluator.evaluate(value, tableDataController)
        }.also {
            evaluationResult = it
            evaluating = false
            onEvaluated(this)
        }
    }
}
