package com.echernikova.evaluator.core

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.evaluator.operators.OperatorCellRange

sealed class EvaluationResult<T> {
    abstract val evaluatedValue: T?
    abstract val cellDependencies: List<CellPointer>

    abstract fun copyWith(newValue: T?, newDependencies: List<CellPointer>): EvaluationResult<T>
    abstract fun toIntResult(): IntegerEvaluationResult?
    abstract fun toDoubleResult(): DoubleEvaluationResult?
    abstract fun toStringResult(): StringEvaluationResult?
    abstract fun toBooleanResult(): BooleanEvaluationResult?
}



sealed class NumberEvaluationResult<T: Number>: EvaluationResult<T>()

class IntegerEvaluationResult(
    override val evaluatedValue: Int,
    override val cellDependencies: List<CellPointer>,
) : NumberEvaluationResult<Int>() {
    override fun copyWith(newValue: Int?, newDependencies: List<CellPointer>) =
        IntegerEvaluationResult(
            newValue ?: evaluatedValue,
            cellDependencies + newDependencies
        )

    override fun toIntResult(): IntegerEvaluationResult = this
    override fun toDoubleResult(): DoubleEvaluationResult = DoubleEvaluationResult(
        evaluatedValue = evaluatedValue.toDouble(),
        cellDependencies = cellDependencies
    )

    override fun toStringResult(): StringEvaluationResult? = null
    override fun toBooleanResult(): BooleanEvaluationResult? = null
}

class DoubleEvaluationResult(
    override val evaluatedValue: Double,
    override val cellDependencies: List<CellPointer>,
) : EvaluationResult<Double>() {
    override fun copyWith(newValue: Double?, newDependencies: List<CellPointer>) =
        DoubleEvaluationResult(
            newValue ?: evaluatedValue,
            cellDependencies + newDependencies
        )

    override fun toIntResult(): IntegerEvaluationResult? = null
    override fun toDoubleResult(): DoubleEvaluationResult = this
    override fun toStringResult(): StringEvaluationResult? = null
    override fun toBooleanResult(): BooleanEvaluationResult? = null
}

class StringEvaluationResult(
    override val evaluatedValue: String,
    override val cellDependencies: List<CellPointer>,
) : EvaluationResult<String>() {
    override fun copyWith(newValue: String?, newDependencies: List<CellPointer>) =
        StringEvaluationResult(
            newValue ?: evaluatedValue,
            cellDependencies + newDependencies
        )

    override fun toIntResult(): IntegerEvaluationResult? = null
    override fun toDoubleResult(): DoubleEvaluationResult? = null
    override fun toStringResult(): StringEvaluationResult? = this
    override fun toBooleanResult(): BooleanEvaluationResult? = null
}

class BooleanEvaluationResult(
    override val evaluatedValue: Boolean,
    override val cellDependencies: List<CellPointer>,
) : EvaluationResult<Boolean>() {
    override fun copyWith(newValue: Boolean?, newDependencies: List<CellPointer>) =
        BooleanEvaluationResult(
            newValue ?: evaluatedValue,
            cellDependencies + newDependencies
        )

    override fun toIntResult(): IntegerEvaluationResult? = null
    override fun toDoubleResult(): DoubleEvaluationResult? = null
    override fun toStringResult(): StringEvaluationResult? = null
    override fun toBooleanResult(): BooleanEvaluationResult = this
}

class CellRangeEvaluationResult(
    override val evaluatedValue: OperatorCellRange,
    override val cellDependencies: List<CellPointer>,
) : EvaluationResult<OperatorCellRange>() {
    override fun copyWith(newValue: OperatorCellRange?, newDependencies: List<CellPointer>) =
        CellRangeEvaluationResult(
            newValue ?: evaluatedValue,
            cellDependencies + newDependencies
        )

    override fun toIntResult(): IntegerEvaluationResult? = null
    override fun toDoubleResult(): DoubleEvaluationResult? = null
    override fun toStringResult(): StringEvaluationResult? = null
    override fun toBooleanResult(): BooleanEvaluationResult? = null
}

class EmptyCellEvaluationResult(
    override val evaluatedValue: Any? = null,
    override val cellDependencies: List<CellPointer>,
) : EvaluationResult<Any?>() {
    override fun copyWith(newValue: Any?, newDependencies: List<CellPointer>) =
        EmptyCellEvaluationResult(
            null,
            cellDependencies + newDependencies
        )

    override fun toIntResult(): IntegerEvaluationResult = IntegerEvaluationResult(
        evaluatedValue = 1,
        cellDependencies = cellDependencies
    )

    override fun toDoubleResult(): DoubleEvaluationResult = DoubleEvaluationResult(
        evaluatedValue = 1.0,
        cellDependencies = cellDependencies
    )

    override fun toStringResult(): StringEvaluationResult = StringEvaluationResult(
        evaluatedValue = "",
        cellDependencies = cellDependencies
    )

    override fun toBooleanResult(): BooleanEvaluationResult = BooleanEvaluationResult(
        evaluatedValue = false,
        cellDependencies = cellDependencies
    )
}

class ErrorEvaluationResult(
    override val evaluatedValue: String?,
    override val cellDependencies: List<CellPointer>,
) : EvaluationResult<String?>() {
    override fun copyWith(newValue: String?, newDependencies: List<CellPointer>) =
        ErrorEvaluationResult(
            evaluatedValue ?: newValue,
            cellDependencies + newDependencies
        )

    override fun toIntResult(): IntegerEvaluationResult? = null
    override fun toDoubleResult(): DoubleEvaluationResult? = null
    override fun toStringResult(): StringEvaluationResult? = null
    override fun toBooleanResult(): BooleanEvaluationResult? = null
}
