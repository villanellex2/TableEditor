package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.EvaluationResult
import com.echernikova.evaluator.core.Evaluator

class TableCell(
    initialValue: String?,
    val row: Int,
    val column: Int,
    private val tableData: TableData,
    private val evaluator: Evaluator,
    private val cellExpiredCallback: (Int, Int) -> Unit,
) {
    private val onDependenciesUpdatedCallback = OnCellChanged { evaluate() }
    private val onCellChangedListeners by lazy { mutableListOf<OnCellChanged>() }
    private val dependencies by lazy { mutableListOf<TableCell>() }

    var rawValue: String? = initialValue
        set(value) {
            if (field != value) {
                field = value
                evaluate()
            }
        }

    private var evaluating = false
    var evaluationResult: EvaluationResult? = null

    init {
        evaluate()
    }

    fun evaluate() {
        val value = rawValue ?: return clearResult()
        if (evaluating) {
            evaluationResult = EvaluationResult.buildErrorResult(
                errorMessage = "Cyclic dependenciesю",
                dependencies = evaluationResult?.cellDependencies ?: emptyList()
            )
            return
        }

        evaluating = true
        evaluationResult = evaluator.evaluate(value)

        dependencies.forEach { it.onCellChangedListeners.remove(onDependenciesUpdatedCallback) }
        dependencies.clear()

        evaluationResult?.cellDependencies?.forEach { (row, column) ->
            tableData.getOrCreateCell(row, column)?.also {
                dependencies.add(it)
                it.onCellChangedListeners.add(onDependenciesUpdatedCallback)
            }
        }

        cellExpiredCallback(row, column)
        onCellChangedListeners.forEach { it.onCellChanged() }
        evaluating = false
    }

    private fun clearResult() {
        evaluationResult = null
    }

    fun interface OnCellChanged {
        fun onCellChanged()
    }
}
