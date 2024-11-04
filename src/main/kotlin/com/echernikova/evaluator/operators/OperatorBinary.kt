package com.echernikova.evaluator.operators

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.evaluator.core.*
import com.echernikova.evaluator.core.tokenizing.Token
import kotlin.math.pow

private val numbersErrorMessage = { str: String -> "Binary operator '$str' supports only number values as arguments."}
private val invalidArgumentsError = { str: String -> "Division by zero." }
private val overflowError = { "Overflow!." }
private val booleanErrorMessage = { str: String -> "Binary operator '$str' supports only boolean values as arguments."}

/**
 * Binary operators evaluations.
 */
class OperatorBinary(
    private val operator: Token.Operator.Binary,
    private val leftRaw: Operator,
    private val rightRaw: Operator,
) : Operator {

    override fun evaluate(context: Context): EvaluationResult<*> {
        val leftEvaluated = leftRaw.evaluate(context)
        if (leftEvaluated is ErrorEvaluationResult) return leftEvaluated

        val rightEvaluated = rightRaw.evaluate(context)
        if (rightEvaluated is ErrorEvaluationResult) return rightEvaluated

        val dependencies = rightEvaluated.cellDependencies + leftEvaluated.cellDependencies

        return when (operator) {
            Token.Operator.Binary.Or -> {
                val leftBool = leftEvaluated.tryConvertToBoolean()
                val rightBool = rightEvaluated.tryConvertToBoolean()

                if (leftBool != null && rightBool != null) {
                    val left = leftBool.evaluatedValue
                    val right = rightBool.evaluatedValue
                    DataEvaluationResult<Boolean>((left || right), dependencies)
                } else {
                    ErrorEvaluationResult(booleanErrorMessage.invoke("||"), dependencies)
                }
            }

            Token.Operator.Binary.And -> {
                val leftBool = leftEvaluated.tryConvertToBoolean()
                val rightBool = rightEvaluated.tryConvertToBoolean()

                if (leftBool != null && rightBool != null) {
                    val left = leftBool.evaluatedValue
                    val right = rightBool.evaluatedValue
                    DataEvaluationResult((left && right), dependencies)
                } else {
                    ErrorEvaluationResult(booleanErrorMessage.invoke("&&"), dependencies)
                }
            }

            Token.Operator.Binary.Plus -> numbersEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first + second },
                { first, second -> first + second },
            )

            Token.Operator.Binary.Minus -> numbersEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first - second },
                { first, second -> first - second },
            )

            Token.Operator.Binary.Multiplication -> numbersEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first * second },
                { first, second -> first * second },
            )

            Token.Operator.Binary.Division -> numbersEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> if (second != 0) first / second else null },
                { first, second -> if (second != 0.0) first / second else null},
            )

            Token.Operator.Binary.Modulo -> numbersEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first % second },
                { first, second -> first % second },
            )

            Token.Operator.Binary.Power -> numbersEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first.toDouble().pow(second.toDouble()).toInt() },
                { first, second -> first.pow(second) },
            )

            Token.Operator.Binary.Greater -> numbersEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first > second },
                { first, second -> first > second },
            )

            Token.Operator.Binary.GreaterOrEqual -> numbersEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first >= second },
                { first, second -> first >= second },
            )

            Token.Operator.Binary.Less -> numbersEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first < second },
                { first, second -> first < second },
            )

            Token.Operator.Binary.LessOrEqual -> numbersEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first <= second },
                { first, second -> first <= second },
            )

            Token.Operator.Binary.Equal -> DataEvaluationResult<Boolean>(
                evaluatedValue = (leftEvaluated.evaluatedValue == rightEvaluated.evaluatedValue),
                cellDependencies = dependencies
            )
        }
    }

    private fun numbersEvaluation(
        operator: Token.Operator.Binary,
        firstArg: EvaluationResult<*>,
        secondArg: EvaluationResult<*>,
        dependencies: Set<CellPointer>,
        intCallback: (Int, Int) -> Any?,
        doubleCallback: (Double, Double) -> Any?,
    ): EvaluationResult<*> {
        val firstInt = firstArg.tryConvertToInt()
        val secondInt = secondArg.tryConvertToInt()
        val createResult = { result: Any? ->
            when {
                result == null -> ErrorEvaluationResult(invalidArgumentsError(operator.symbol), dependencies)

                result is Double && result.isInfinite() -> ErrorEvaluationResult(overflowError(), dependencies)

                result is Int && (result == Int.MAX_VALUE || result == Int.MIN_VALUE) -> {
                    ErrorEvaluationResult(overflowError(), dependencies)
                }

                else -> DataEvaluationResult(result, dependencies)
            }
        }
        if (firstInt != null && secondInt != null) {
            return createResult(intCallback.invoke(firstInt.evaluatedValue, secondInt.evaluatedValue))
        }

        val firstDouble = firstArg.tryConvertToDouble()
        val secondDouble = secondArg.tryConvertToDouble()

        if (firstDouble != null && secondDouble != null) {
            val result = doubleCallback.invoke(firstDouble.evaluatedValue, secondDouble.evaluatedValue)
            return createResult(if (result is Double && result.isInfinite()) null else result)
        }

        return ErrorEvaluationResult(numbersErrorMessage.invoke(operator.symbol), dependencies)
    }
}