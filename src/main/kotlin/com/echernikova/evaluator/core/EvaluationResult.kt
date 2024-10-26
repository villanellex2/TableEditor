package com.echernikova.evaluator.core

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.evaluator.operators.OperatorCellLink

sealed class EvaluationResult<T : Any?>(
    open val evaluatedValue: T,
    open val cellDependencies: Set<CellPointer>
) {
    object Empty

    fun copyWithDependencies(
        newCellDependencies: Set<CellPointer>,
        override: Boolean = false
    ): EvaluationResult<*> {
        return when (this) {
            is DataEvaluationResult -> DataEvaluationResult(
                evaluatedValue = evaluatedValue,
                cellDependencies = if (override) newCellDependencies else (newCellDependencies + cellDependencies)
            )

            is CellRangeEvaluationResult -> CellRangeEvaluationResult(
                evaluatedValue = evaluatedValue,
                cellDependencies = if (override) newCellDependencies else (newCellDependencies + cellDependencies)
            )

            is ErrorEvaluationResult -> ErrorEvaluationResult(
                evaluatedValue = evaluatedValue,
                cellDependencies = if (override) newCellDependencies else (newCellDependencies + cellDependencies)
            )
        }
    }

    fun tryConvertToBoolean(): EvaluationResult<Boolean>? {
        return when (evaluatedValue) {
            is Boolean -> this as EvaluationResult<Boolean>
            Empty -> DataEvaluationResult(false, cellDependencies)
            is Int -> DataEvaluationResult((evaluatedValue as Int == 0), cellDependencies)
            else -> null
        }
    }

    fun tryConvertToInt(): EvaluationResult<Int>? {
        return when (evaluatedValue) {
            is Int -> this as EvaluationResult<Int>
            Empty -> DataEvaluationResult(0, cellDependencies)
            else -> null
        }
    }

    fun tryConvertToDouble(): EvaluationResult<Double>? {
        return when (evaluatedValue) {
            is Double -> this as EvaluationResult<Double>
            is Int -> DataEvaluationResult((evaluatedValue as Int).toDouble(), cellDependencies)
            Empty -> DataEvaluationResult(0.0, cellDependencies)
            else -> null
        }
    }

    fun tryConvertToString(): EvaluationResult<String>? {
        return when (evaluatedValue) {
            is ErrorEvaluationResult -> return null
            is CellRangeEvaluationResult -> return null
            is String -> return this as EvaluationResult<String>
            Empty -> DataEvaluationResult("", cellDependencies)
            else -> DataEvaluationResult(evaluatedValue.toString(), cellDependencies)
        }
    }
}

/**
 * Class for final values, which can be drawn on table.
 */
sealed class FinalEvaluationResult<T>(
    override val evaluatedValue: T,
    override val cellDependencies: Set<CellPointer>
) : EvaluationResult<T>(evaluatedValue, cellDependencies)

data class DataEvaluationResult<T>(
    override val evaluatedValue: T,
    override val cellDependencies: Set<CellPointer>
) : FinalEvaluationResult<T>(evaluatedValue, cellDependencies)

data class ErrorEvaluationResult(
    override val evaluatedValue: String?,
    override val cellDependencies: Set<CellPointer>
) : FinalEvaluationResult<String?>(evaluatedValue, cellDependencies)

/**
 * The only not final class, if evaluation ended with cell range, we can't show it on table correctly.
 */
data class CellRangeEvaluationResult(
    override val evaluatedValue: List<OperatorCellLink>,
    override val cellDependencies: Set<CellPointer>
) : EvaluationResult<List<OperatorCellLink>>(evaluatedValue, cellDependencies)

