package com.echernikova.evaluator.core.tokenizing

import com.echernikova.evaluator.core.EvaluationException
import com.echernikova.evaluator.core.TokenizerException
import com.echernikova.evaluator.core.TokenizerState

object Tokenizer {
    fun tokenize(input: String): List<Token> = tokenize(input.toCharArray())

    private fun tokenize(input: CharArray): List<Token> {
        val state = TokenizerState(input)
        processTokens(state)
        return state.tokens
    }

    private fun processTokens(state: TokenizerState) {
        while (!state.isAtEnd()) {
            when (state.current()) {
                '"' -> processString(state)
                '+' -> addOperatorToken(state, Token.Operator.Unary.Plus, Token.Operator.Binary.Plus)
                '-' -> addOperatorToken(state, Token.Operator.Unary.Minus, Token.Operator.Binary.Minus)
                '(' -> addToken(state, Token.Bracket.LeftRound)
                ')' -> addToken(state, Token.Bracket.RightRound)
                '*' -> addToken(state, Token.Operator.Binary.Multiplication)
                '/' -> addToken(state, Token.Operator.Binary.Division)
                '%' -> addToken(state, Token.Operator.Binary.Modulo)
                '=' -> addToken(state, Token.Operator.Binary.Equal)
                '^' -> addToken(state, Token.Operator.Binary.Power)
                ',' -> addToken(state, Token.Function.ArgumentDelimiter.Comma)
                ':' -> addToken(state, Token.Cell.CellDelimiter)
                '&' -> if (state.next() == '&') {
                    addToken(state, Token.Operator.Binary.And)
                    state.forward()
                } else {
                    throw TokenizerException("Unknown operator '&' on position ${state.index}. Did you mean '&&'?")
                }
                '|' -> if (state.next() == '|') {
                    addToken(state, Token.Operator.Binary.Or)
                    state.forward()
                } else {
                    throw TokenizerException("Unknown operator '|' on position ${state.index}. Did you mean '||'?")
                }
                '<' -> if (state.next() == '=') {
                    addToken(state, Token.Operator.Binary.LessOrEqual)
                    state.forward()
                } else {
                    addToken(state, Token.Operator.Binary.Less)
                }
                '>' -> if (state.next() == '=') {
                    addToken(state, Token.Operator.Binary.GreaterOrEqual)
                    state.forward()
                } else {
                    addToken(state, Token.Operator.Binary.Greater)
                }
                else -> {
                    when {
                        state.current().isWhiteSpace() -> state.forward()
                        state.current().isDecimal(state.next()) -> processNumber(state)
                        state.current().isAlphabetic() -> processIdentifier(state)
                        else -> throw TokenizerException("Unknown character: ${state.current()}")
                    }
                }
            }
        }
    }

    private fun processString(state: TokenizerState) {
        state.forward()
        val start = state.index
        while (!state.isAtEnd() && state.current() != '"') {
            state.forward()
        }
        if (state.isAtEnd()) {
            throw TokenizerException("Unterminated string '${state.part(start-1, state.index)}'")
        }
        val stringValue = state.part(start, state.index)
        state.tokens.add(Token.Literal.Str(stringValue))
        state.forward()
    }

    private fun isUnaryOperator(tokens: List<Token>): Boolean {
        if (tokens.isEmpty()) return true
        val last = tokens.last()
        return last is Token.Operator || last == Token.Bracket.LeftRound || last is Token.Function.ArgumentDelimiter
    }

    private fun addOperatorToken(
        state: TokenizerState,
        unaryToken: Token.Operator.Unary,
        binaryToken: Token.Operator.Binary
    ) {
        val token = if (isUnaryOperator(state.tokens)) {
            unaryToken
        } else {
            binaryToken
        }
        state.tokens.add(token)
        state.forward()
    }

    private fun addToken(state: TokenizerState, token: Token) {
        state.tokens.add(token)
        state.forward()
    }

    private fun processNumber(state: TokenizerState) {
        val start = state.index
        var numberOfDots = 0
        do {
            if (state.current() == '.') numberOfDots++
            state.forward()
        } while (state.current().isDigit() || state.current() == '.')

        val numberStr = state.part(start, state.index)
        val error = TokenizerException("Invalid number: $numberStr")
        val token = when (numberOfDots) {
            0 -> numberStr.toIntOrNull()?.let { Token.Literal.Int(it) } ?: run { throw error }
            1 -> numberStr.toDoubleOrNull()?.let { Token.Literal.Double(it) } ?: run { throw error }
            // todo: Date? 2 ->
            else -> throw error
        }
        state.tokens.add(token)
    }

    private fun processIdentifier(state: TokenizerState) {
        val start = state.index
        do {
            state.forward()
        } while (state.current().isAlphabetic() || state.current().isDigit())

        val identifier = state.part(start, state.index)
        val boolean = identifier.toBooleanStrictOrNull()

        when {
            boolean != null -> state.tokens.add(Token.Literal.Bool(boolean))
            // correctness of function name will be checked on function evaluation
            state.current() == '(' -> state.tokens.add(Token.Function(identifier))
            isCellLinkCorrect(identifier) -> state.tokens.add(Token.Cell.CellLink(identifier))
            else ->  throw EvaluationException("Incorrect cell link $identifier")
        }
    }

    private fun isCellLinkCorrect(str: String): Boolean {
        if (str.length < 2) return false
        val firstSymbol = str[0]
        if (firstSymbol < 'A' || firstSymbol > 'Z') {
            return false
        }
        return str.substring(1, str.length).toIntOrNull() != null
    }
}
