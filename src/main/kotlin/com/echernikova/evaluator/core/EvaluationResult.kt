package com.echernikova.evaluator.core

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.evaluator.operators.OperatorCellRange

sealed class EvaluationResult<T : Any?>(
    open val evaluatedValue: T,
    open val cellDependencies: List<CellPointer>
) {
    object Empty : EvaluationResult<Nothing?>(null, emptyList())

    fun tryConvertToBoolean(): EvaluationResult<Boolean>? {
        return when (evaluatedValue) {
            is Boolean -> this as EvaluationResult<Boolean>

            Empty -> {
                DataEvaluationResult(
                    evaluatedValue = false,
                    cellDependencies = cellDependencies
                )
            }

            is Int -> {
                DataEvaluationResult(
                    evaluatedValue = (evaluatedValue as Int == 0),
                    cellDependencies = cellDependencies
                )
            }

            else -> null
        }
    }

    fun tryConvertToInt(): EvaluationResult<Int>? {
        return when (evaluatedValue) {
            is Int -> this as EvaluationResult<Int>

            Empty -> {
                DataEvaluationResult(
                    evaluatedValue = 0,
                    cellDependencies = cellDependencies
                )
            }

            else -> null
        }
    }

    fun tryConvertToDouble(): EvaluationResult<Double>? {
        return when (evaluatedValue) {
            is Double -> this as EvaluationResult<Double>

            is Int -> DataEvaluationResult(
                evaluatedValue = (evaluatedValue as Int).toDouble(),
                cellDependencies = cellDependencies
            )

            Empty -> {
                DataEvaluationResult(
                    evaluatedValue = 0.0,
                    cellDependencies = cellDependencies
                )
            }

            else -> null
        }
    }

    fun tryConvertToString(): EvaluationResult<String>? {
        return when (evaluatedValue) {
            is String -> return this as EvaluationResult<String>

            Empty -> {
                DataEvaluationResult(
                    evaluatedValue = "",
                    cellDependencies = cellDependencies
                )
            }

            else -> null
        }
    }
}

/**
 * Class for final values, which can be drawn on table.
 */
sealed class FinalEvaluationResult<T>(
    override val evaluatedValue: T,
    override val cellDependencies: List<CellPointer>
) : EvaluationResult<T>(evaluatedValue, cellDependencies)

data class DataEvaluationResult<T>(
    override val evaluatedValue: T,
    override val cellDependencies: List<CellPointer>
) : FinalEvaluationResult<T>(evaluatedValue, cellDependencies)

data class ErrorEvaluationResult(
    override val evaluatedValue: String?,
    override val cellDependencies: List<CellPointer>
) : FinalEvaluationResult<String?>(evaluatedValue, cellDependencies)

/**
 * The only not final class, if evaluation ended with cell range, we can't show it on table correctly.
 */
data class CellRangeEvaluationResult(
    override val evaluatedValue: OperatorCellRange,
    override val cellDependencies: List<CellPointer>
) : EvaluationResult<OperatorCellRange>(evaluatedValue, cellDependencies)

