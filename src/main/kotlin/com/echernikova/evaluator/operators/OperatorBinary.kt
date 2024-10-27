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

    override fun evaluate(context: Context): EvaluationResult {
        val leftEvaluated = leftRaw.evaluate(context)
        if (leftEvaluated.evaluatedType == EvaluationResultType.Error) return leftEvaluated

        val rightEvaluated = rightRaw.evaluate(context)
        if (rightEvaluated.evaluatedType == EvaluationResultType.Error) return rightEvaluated

        val dependencies = rightEvaluated.cellDependencies + leftEvaluated.cellDependencies

        return when (operator) {
            Token.Operator.Binary.Or -> {
                if (rightEvaluated.tryToConvertType(EvaluationResultType.Boolean) != null && leftEvaluated.tryToConvertType(EvaluationResultType.Boolean) != null) {
                    val left = leftEvaluated.evaluatedValue as Boolean
                    val right = rightEvaluated.evaluatedValue as Boolean
                    EvaluationResult((left || right), EvaluationResultType.Boolean, dependencies)
                } else {
                    EvaluationResult.buildErrorResult(booleanErrorMessage.invoke("||"), dependencies)
                }
            }

            Token.Operator.Binary.And -> {
                if (rightEvaluated.tryToConvertType(EvaluationResultType.Boolean) != null && leftEvaluated.tryToConvertType(EvaluationResultType.Boolean) != null) {
                    val left = leftEvaluated.evaluatedValue as Boolean
                    val right = rightEvaluated.evaluatedValue as Boolean
                    EvaluationResult((left && right), EvaluationResultType.Boolean, dependencies)
                } else {
                    EvaluationResult.buildErrorResult(booleanErrorMessage.invoke("&&"), dependencies)
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

            Token.Operator.Binary.Equal -> EvaluationResult(
                (leftEvaluated.evaluatedValue == rightEvaluated.evaluatedValue), EvaluationResultType.Boolean, dependencies
            )
        }
    }

    private fun numbersEvaluation(
        operator: Token.Operator.Binary,
        firstArg: EvaluationResult?,
        secondArg: EvaluationResult?,
        dependencies: List<CellPointer>,
        intCallback: (Int, Int) -> Any,
        doubleCallback: (Double, Double) -> Any,
    ): EvaluationResult {
        val firstInt = firstArg?.tryToConvertType(EvaluationResultType.Int)
        val secondInt = secondArg?.tryToConvertType(EvaluationResultType.Int)
        if (firstInt != null && secondInt != null) {
            return EvaluationResult(
                evaluatedValue = intCallback.invoke(firstInt.evaluatedValue as Int, secondInt.evaluatedValue as Int),
                evaluatedType = EvaluationResultType.Int,
                cellDependencies = dependencies
            )
        }
        val firstDouble = firstArg?.tryToConvertType(EvaluationResultType.Double)
        val secondDouble = secondArg?.tryToConvertType(EvaluationResultType.Double)
        if (firstDouble != null && secondDouble != null) {
            return EvaluationResult(
                evaluatedValue = doubleCallback.invoke(
                    firstDouble.evaluatedValue as Double,
                    secondDouble.evaluatedValue as Double
                ),
                evaluatedType = EvaluationResultType.Int,
                cellDependencies = dependencies
            )
        }

        return EvaluationResult.buildErrorResult(numbersErrorMessage.invoke(operator.symbol), dependencies)
    }
}