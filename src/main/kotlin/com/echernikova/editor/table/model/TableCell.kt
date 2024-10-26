package com.echernikova.editor.table.model

import com.echernikova.evaluator.core.EvaluationResult
import com.echernikova.evaluator.core.Evaluator

class TableCell(
    initialValue: String?,
    private val row: Int,
    private val column: Int,
    private val tableData: TableData,
    private val evaluator: Evaluator,
    private val cellExpiredCallback: (Int, Int) -> Unit,
) {
    private val onDependenciesUpdatedCallback = OnCellChanged { evaluate() }
    private val onCellChangedListeners by lazy { mutableListOf<OnCellChanged>() }
    private val dependencies by lazy { mutableListOf<TableCell>() }

    var rawValue: String? = initialValue
        set(value) {
            if (field == value) return
            field = value
            evaluate()
        }
    var evaluating: Boolean = false
        private set
    var evaluationResult: EvaluationResult? = rawValue?.let { evaluate() }

    init {
        evaluate()
    }

    private fun addOnCellChangedListener(listener: OnCellChanged) {
        onCellChangedListeners.add(listener)
    }

    private fun removeOnCellChangedListener(listener: OnCellChanged) {
        onCellChangedListeners.remove(listener)
    }

    private fun evaluate(): EvaluationResult? {
        val value = rawValue ?: ""
        if (rawValue == null) {
            evaluationResult = null
            return null
        }

        evaluating = true
        val evaluationResult = evaluator.evaluate(value)

        //todo: должны ли они сами триггерить свои вычисления и разруливать зависимости?
        dependencies.forEach { cell -> cell.removeOnCellChangedListener(onDependenciesUpdatedCallback) }
        evaluationResult.cellDependencies.forEach { (row, column) ->
            tableData.getCell(row, column)?.also {
                it.addOnCellChangedListener(onDependenciesUpdatedCallback)
            }
        }

        cellExpiredCallback.invoke(row, column)
        onCellChangedListeners.forEach { it.onCellChanged() }
        this.evaluationResult = evaluationResult
        return evaluationResult
    }

    fun interface OnCellChanged {
        fun onCellChanged()
    }
}