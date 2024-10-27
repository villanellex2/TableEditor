package com.echernikova.evaluator.operators

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.evaluator.core.*
import com.echernikova.evaluator.core.tokenizing.Token
import kotlin.math.pow

private val numbersErrorMessage = { str: String -> "Binary operator '$str' supports only number values as arguments."}
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
                { first, second -> first / second },
                { first, second -> first / second },
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
                { first, second -> first.toDouble().pow(second).toInt() },
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
        dependencies: List<CellPointer>,
        intCallback: (Int, Int) -> Any,
        doubleCallback: (Double, Double) -> Any,
    ): EvaluationResult<*> {
        val firstInt = firstArg.tryConvertToInt()
        val secondInt = secondArg.tryConvertToInt()
        if (firstInt != null && secondInt != null) {
            return DataEvaluationResult(
                evaluatedValue = intCallback.invoke(firstInt.evaluatedValue, secondInt.evaluatedValue),
                cellDependencies = dependencies
            )
        }

        val firstDouble = firstArg.tryConvertToDouble()
        val secondDouble = secondArg.tryConvertToDouble()

        if (firstDouble != null && secondDouble != null) {
            return DataEvaluationResult(
                evaluatedValue = doubleCallback.invoke(
                    firstDouble.evaluatedValue,
                    secondDouble.evaluatedValue
                ),
                cellDependencies = dependencies
            )
        }

        return ErrorEvaluationResult(numbersErrorMessage.invoke(operator.symbol), dependencies)
    }
}