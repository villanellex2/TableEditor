package com.echernikova.evaluator.core

import com.echernikova.editor.table.model.CellPointer

//todo: переделать на интерфейс
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

    fun tryToConvertType(newType: EvaluationResultType): EvaluationResult? {
        return when {
            evaluatedType == newType -> this
            evaluatedType == EvaluationResultType.Empty -> {
                when (newType) {
                    EvaluationResultType.Int -> EvaluationResult(
                        evaluatedValue = 0,
                        evaluatedType = EvaluationResultType.Int,
                        cellDependencies = cellDependencies
                    )

                    EvaluationResultType.Boolean -> EvaluationResult(
                        evaluatedValue = false,
                        evaluatedType = EvaluationResultType.Boolean,
                        cellDependencies = cellDependencies
                    )

                    EvaluationResultType.Double -> EvaluationResult(
                        evaluatedValue = 0.0,
                        evaluatedType = EvaluationResultType.Double,
                        cellDependencies = cellDependencies
                    )

                    EvaluationResultType.String -> EvaluationResult(
                        evaluatedValue = "",
                        evaluatedType = EvaluationResultType.String,
                        cellDependencies = cellDependencies
                    )

                    else -> null
                }
            }
            evaluatedType == EvaluationResultType.Int && newType == EvaluationResultType.Double -> {
                EvaluationResult(
                    evaluatedValue = evaluatedValue.toString().toDouble(), //todo!!!
                    evaluatedType = EvaluationResultType.Double,
                    cellDependencies = cellDependencies
                )
            }

            else -> null
        }
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
