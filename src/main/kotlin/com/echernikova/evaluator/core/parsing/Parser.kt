package com.echernikova.evaluator.core.parsing

import com.echernikova.evaluator.core.EvaluationException
import com.echernikova.evaluator.core.ParsingState
import com.echernikova.evaluator.core.tokenizing.Token
import com.echernikova.evaluator.operators.*

object Parser {

    fun parse(tokens: List<Token>): Operator {
        if (tokens.isEmpty()) throw EvaluationException("Expression expected")

        val state = ParsingState(tokens)
        val expression = startParsing(state)

        if (!state.isAtEnd()) throw EvaluationException("Expression expected")

        return expression
    }

    private fun startParsing(state: ParsingState): Operator = parseBinary(state)

    private fun parseBinary(
        state: ParsingState,
        priority: Int = 0
    ): Operator {
        var left = parseUnary(state)

        while (!state.isAtEnd() && state.current() is Token.Operator.Binary) {
            val currToken = state.current() as Token.Operator.Binary
            val currPriority = currToken.priority

            if (currPriority < priority) break

            state.forward()
            val right = parseBinary(state, currPriority)
            left = OperatorBinary(currToken, left, right)
        }

        return left
    }


    private fun parseUnary(state: ParsingState): Operator {
        val currToken = state.current()
        return if (!state.isAtEnd() && currToken is Token.Operator.Unary) {
            OperatorUnary(state.next() as Token.Operator.Unary, parseUnary(state))
        } else {
            parseRest(state)
        }
    }

    private fun parseRest(state: ParsingState): Operator {
        if (state.isAtEnd()) throw EvaluationException("Expression expected")

        return when (val token = state.current()) {
            is Token.Literal -> {
                state.forward()
                OperatorLiteral.getFor(token)
            }
            is Token.Cell.CellLink -> parseCellLink(token, state)
            is Token.Function -> parseFunction(token, state)
            Token.Bracket.LeftRound -> parseBracket(state)
            else -> throw EvaluationException("Expression expected")
        }
    }

    private fun parseBracket(state: ParsingState): Operator {
        state.forward()
        val result = startParsing(state)
        if (state.current() != Token.Bracket.RightRound) {
            throw EvaluationException("')' expected after expression")
        }
        state.forward()
        return result
    }

    private fun parseCellLink(cell: Token.Cell.CellLink, state: ParsingState): Operator {
        if (state.next() !is Token.Cell.CellDelimiter) return OperatorCellRange(cell, cell)
        state.forward()

        val nextCell = state.next()
        if (nextCell !is Token.Cell.CellLink) {
            throw EvaluationException("Incorrect cell link '${cell.name}:'. Cell link expected after ':'.")
        }
        state.forward()
        return OperatorCellRange(cell, nextCell)
    }

    private fun parseFunction(token: Token.Function, state: ParsingState): Operator {
        if (state.next() != Token.Bracket.LeftRound) {
            throw EvaluationException("'(' expected after function call")
        }
        val arguments = mutableListOf<Operator>()
        while (!state.isAtEnd() && state.current() != Token.Bracket.RightRound) {
            arguments += startParsing(state)
            if (state.current() is Token.Function.ArgumentDelimiter) state.forward()
        }
        if (state.next() != Token.Bracket.RightRound) {
            throw EvaluationException("expected ')' after a function call")
        }
        return OperatorFunction(token, arguments)
    }
}
