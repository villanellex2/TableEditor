package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.ErrorEvaluationResult
import com.echernikova.evaluator.core.EvaluationResult
import com.echernikova.evaluator.core.Evaluator
import com.echernikova.evaluator.core.FinalEvaluationResult

class TableCell(
    initialValue: String?,
    val cellPointer: CellPointer,
    private val tableData: TableData,
    private val evaluator: Evaluator,
    private val cellExpiredCallback: (CellPointer) -> Unit,
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

    var evaluating = false
    private set
    var evaluationResult: FinalEvaluationResult<*>? = null

    fun evaluate() {
        val value = rawValue ?: return clearResult()
        if (evaluating) {
            evaluationResult = ErrorEvaluationResult(
                evaluatedValue = "Cyclic dependencies",
                cellDependencies = evaluationResult?.cellDependencies ?: emptyList()
            )
            return
        }

        evaluating = true
        evaluationResult = evaluator.evaluate(value, tableData)

        dependencies.forEach { it.onCellChangedListeners.remove(onDependenciesUpdatedCallback) }
        dependencies.clear()

        evaluationResult?.cellDependencies?.forEach {
            tableData.getOrCreateCell(it)?.also { cell ->
                dependencies.add(cell)
                cell.onCellChangedListeners.add(onDependenciesUpdatedCallback)
            }
        }

        evaluating = false
        cellExpiredCallback(cellPointer)
        onCellChangedListeners.forEach { it.onCellChanged() }
    }

    private fun clearResult() {
        evaluationResult = null
    }

    fun interface OnCellChanged {
        fun onCellChanged()
    }
}
