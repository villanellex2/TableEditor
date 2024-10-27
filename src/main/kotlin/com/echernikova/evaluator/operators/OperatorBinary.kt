package com.echernikova.evaluator.operators

import com.echernikova.editor.table.model.CellPointer
import com.echernikova.evaluator.core.*
import com.echernikova.evaluator.core.tokenizing.Token
import kotlin.math.pow

private val numbersErrorMessage = { str: String -> "Binary operator '$str' supports only number values as arguments."}
private val booleanErrorMessage = { str: String -> "Binary operator '$str' supports only boolean values as arguments."}

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
                val leftBool = leftEvaluated.toBooleanResult()
                val rightBool = rightEvaluated.toBooleanResult()

                if (leftBool != null && rightBool != null) {
                    val left = leftBool.evaluatedValue
                    val right = rightBool.evaluatedValue
                    BooleanEvaluationResult((left || right), dependencies)
                } else {
                    ErrorEvaluationResult(booleanErrorMessage.invoke("||"), dependencies)
                }
            }

            Token.Operator.Binary.And -> {
                val leftBool = leftEvaluated.toBooleanResult()
                val rightBool = rightEvaluated.toBooleanResult()

                if (leftBool != null && rightBool != null) {
                    val left = leftBool.evaluatedValue
                    val right = rightBool.evaluatedValue
                    BooleanEvaluationResult((left && right), dependencies)
                } else {
                    ErrorEvaluationResult(booleanErrorMessage.invoke("&&"), dependencies)
                }
            }

            Token.Operator.Binary.Plus -> numbersToNumbersEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first + second },
                { first, second -> first + second },
            )

            Token.Operator.Binary.Minus -> numbersToNumbersEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first - second },
                { first, second -> first - second },
            )

            Token.Operator.Binary.Multiplication -> numbersToNumbersEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first * second },
                { first, second -> first * second },
            )

            Token.Operator.Binary.Division -> numbersToNumbersEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first / second },
                { first, second -> first / second },
            )

            Token.Operator.Binary.Modulo -> numbersToNumbersEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first % second },
                { first, second -> first % second },
            )

            Token.Operator.Binary.Power -> numbersToNumbersEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first.toDouble().pow(second).toInt() },
                { first, second -> first.pow(second) },
            )

            Token.Operator.Binary.Greater -> numbersToBoolEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first > second },
                { first, second -> first > second },
            )

            Token.Operator.Binary.GreaterOrEqual -> numbersToBoolEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first >= second },
                { first, second -> first >= second },
            )

            Token.Operator.Binary.Less -> numbersToBoolEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first < second },
                { first, second -> first < second },
            )

            Token.Operator.Binary.LessOrEqual -> numbersToBoolEvaluation(
                operator,
                leftEvaluated, rightEvaluated, dependencies,
                { first, second -> first <= second },
                { first, second -> first <= second },
            )

            Token.Operator.Binary.Equal -> BooleanEvaluationResult(
                evaluatedValue = (leftEvaluated.evaluatedValue == rightEvaluated.evaluatedValue),
                cellDependencies = dependencies
            )
        }
    }

    private fun numbersToNumbersEvaluation(
        operator: Token.Operator.Binary,
        firstArg: EvaluationResult<*>?,
        secondArg: EvaluationResult<*>?,
        dependencies: List<CellPointer>,
        intCallback: (Int, Int) -> Int,
        doubleCallback: (Double, Double) -> Double,
    ): EvaluationResult<*> {
        val firstInt = firstArg?.toIntResult()
        val secondInt = secondArg?.toIntResult()
        if (firstInt != null && secondInt != null) {
            return IntegerEvaluationResult(
                evaluatedValue = intCallback.invoke(firstInt.evaluatedValue, secondInt.evaluatedValue),
                cellDependencies = dependencies
            )
        }

        val firstDouble = firstArg?.toDoubleResult()
        val secondDouble = secondArg?.toDoubleResult()
        if (firstDouble != null && secondDouble != null) {
            return DoubleEvaluationResult(
                evaluatedValue = doubleCallback.invoke(
                    firstDouble.evaluatedValue,
                    secondDouble.evaluatedValue
                ),
                cellDependencies = dependencies
            )
        }

        return ErrorEvaluationResult(numbersErrorMessage.invoke(operator.symbol), dependencies)
    }

    private fun numbersToBoolEvaluation(
        operator: Token.Operator.Binary,
        firstArg: EvaluationResult<*>?,
        secondArg: EvaluationResult<*>?,
        dependencies: List<CellPointer>,
        intCallback: (Int, Int) -> Boolean,
        doubleCallback: (Double, Double) -> Boolean,
    ): EvaluationResult<*> {
        val firstInt = firstArg?.toIntResult()
        val secondInt = secondArg?.toIntResult()
        if (firstInt != null && secondInt != null) {
            return BooleanEvaluationResult(
                evaluatedValue = intCallback.invoke(firstInt.evaluatedValue, secondInt.evaluatedValue),
                cellDependencies = dependencies
            )
        }

        val firstDouble = firstArg?.toDoubleResult()
        val secondDouble = secondArg?.toDoubleResult()
        if (firstDouble != null && secondDouble != null) {
            return BooleanEvaluationResult(
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