package com.echernikova.evaluator.core

import com.echernikova.editor.table.model.CellPointer

data class EvaluationResult(
    val evaluatedValue: Any?,
    val evaluatedType: EvaluationResultType,
    val cellDependencies: List<CellPointer> = emptyList(),
    val evaluatedError: EvaluationException? = null,
) {
    companion object {
        fun buildErrorResult(
            errorMessage: String,
            dependencies: List<CellPointer>,
        ) = EvaluationResult(
            null,
            EvaluationResultType.Error,
            dependencies,
            EvaluationException(errorMessage),
        )

        fun copyWithNewValue(
            original: EvaluationResult,
            newValue: Any?
        ) = EvaluationResult(
            newValue,
            original.evaluatedType,
            original.cellDependencies,
            original.evaluatedError,
        )

    }
}

enum class EvaluationResultType {
    Int,
    Double,
    Boolean,
    String,
    Empty,
    CellLink,
    CellRange,
    Error
}
