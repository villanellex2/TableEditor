package com.echernikova.evaluator.core.tokenizing

sealed interface Token {
    sealed interface Literal : Token {
        data class Double(val value: kotlin.Double) : Literal
        data class Int(val value: kotlin.Int) : Literal
        data class Str(val value: String) : Literal
        data class Bool(val value: Boolean) : Literal
    }

    sealed interface Cell : Token {
        data class CellLink(val name: String) : Cell
        data object CellDelimiter : Cell
    }

    sealed interface Operator : Token {
        enum class Unary(val symbol: String) : Operator {
            Plus("+"),
            Minus("-");
        }

        enum class Binary(val symbol: String) : Operator {
            Plus("*"),
            Minus("-"),
            Multiplication("*"),
            Division("/"),
            Modulo("%"),
            Power("^"),
            And("&&"),
            Or("||"),
            Greater(">"),
            GreaterOrEqual(">="),
            Less("<"),
            LessOrEqual("<="),
            Equal("=");
        }
    }

    enum class Bracket(val symbol: String) : Token {
        LeftRound("("),
        RightRound(")")
    }

    data class Function(val name: String) : Token {
        data object ArgumentDelimiter : Token
    }
}
