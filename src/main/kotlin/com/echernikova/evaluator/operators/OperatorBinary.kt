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
        val rightEvaluated = rightRaw.evaluate(context)

        val right = rightEvaluated.evaluatedValue
        val left = leftEvaluated.evaluatedValue
        val dependencies = rightEvaluated.cellDependencies + leftEvaluated.cellDependencies

        return when (operator) {
            Token.Operator.Binary.Or -> {
                if (left is Boolean && right is Boolean) {
                    EvaluationResult((left || right), EvaluationResultType.Boolean, dependencies)
                } else {
                    EvaluationResult.buildErrorResult(booleanErrorMessage.invoke("||"), dependencies)
                }
            }

            Token.Operator.Binary.And -> {
                if (left is Boolean && right is Boolean) {
                    EvaluationResult((left && right), EvaluationResultType.Boolean, dependencies)
                } else {
                    EvaluationResult.buildErrorResult(booleanErrorMessage.invoke("&&"), dependencies)
                }
            }

            Token.Operator.Binary.Plus -> numbersEvaluation(
                operator,
                left, right, dependencies,
                { first, second -> first + second },
                { first, second -> first + second },
            )

            Token.Operator.Binary.Minus -> numbersEvaluation(
                operator,
                left, right, dependencies,
                { first, second -> first - second },
                { first, second -> first - second },
            )

            Token.Operator.Binary.Multiplication -> numbersEvaluation(
                operator,
                left, right, dependencies,
                { first, second -> first * second },
                { first, second -> first * second },
            )

            Token.Operator.Binary.Division -> numbersEvaluation(
                operator,
                left, right, dependencies,
                { first, second -> first / second },
                { first, second -> first / second },
            )

            Token.Operator.Binary.Modulo -> numbersEvaluation(
                operator,
                left, right, dependencies,
                { first, second -> first % second },
                { first, second -> first % second },
            )

            Token.Operator.Binary.Power -> numbersEvaluation(
                operator,
                left, right, dependencies,
                { first, second -> first.toDouble().pow(second).toInt() },
                { first, second -> first.pow(second) },
            )

            Token.Operator.Binary.Greater -> numbersEvaluation(
                operator,
                left, right, dependencies,
                { first, second -> first > second },
                { first, second -> first > second },
            )

            Token.Operator.Binary.GreaterOrEqual -> numbersEvaluation(
                operator,
                left, right, dependencies,
                { first, second -> first >= second },
                { first, second -> first >= second },
            )

            Token.Operator.Binary.Less -> numbersEvaluation(
                operator,
                left, right, dependencies,
                { first, second -> first < second },
                { first, second -> first < second },
            )

            Token.Operator.Binary.LessOrEqual -> numbersEvaluation(
                operator,
                left, right, dependencies,
                { first, second -> first <= second },
                { first, second -> first <= second },
            )

            Token.Operator.Binary.Equal -> EvaluationResult(
                (left == right), EvaluationResultType.Boolean, dependencies
            )
        }
    }

    private fun numbersEvaluation(
        operator: Token.Operator.Binary,
        firstArg: Any?,
        secondArg: Any?,
        dependencies: List<CellPointer>,
        intCallback: (Int, Int) -> Any,
        doubleCallback: (Double, Double) -> Any,
    ): EvaluationResult {
        return if (firstArg is Number && secondArg is Number) {
            if (firstArg is Int && secondArg is Int) {
                EvaluationResult(
                    evaluatedValue = intCallback.invoke(firstArg, secondArg),
                    evaluatedType = EvaluationResultType.Int,
                    cellDependencies = dependencies
                )
            } else {
                EvaluationResult(
                    evaluatedValue = doubleCallback.invoke(firstArg.toDouble(), secondArg.toDouble()),
                    evaluatedType = EvaluationResultType.Int,
                    cellDependencies = dependencies
                )
            }
        } else {
            return EvaluationResult.buildErrorResult(numbersErrorMessage.invoke(operator.symbol), dependencies)
        }
    }
}