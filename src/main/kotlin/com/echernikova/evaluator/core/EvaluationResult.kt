package com.echernikova.evaluator.core

data class EvaluationResult(
    val evaluatedValue: Any?,
    val evaluatedType: EvaluationResultType,
    val cellDependencies: List<Pair<Int, Int>> = emptyList(),
    val evaluatedError: EvaluationException? = null,
) {
    companion object {
        fun buildErrorResult(
            errorMessage: String,
            dependencies: List<Pair<Int, Int>>,
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
    CellLink,
    String,
    Error
}
