package com.echernikova.evaluator.core.tokenizing

sealed interface Token {
    sealed interface Literal : Token {
        val value: Any
        data class Double(override val value: kotlin.Double) : Literal
        data class Int(override val value: kotlin.Int) : Literal
        data class Str(override val value: String) : Literal
        data class Bool(override val value: Boolean) : Literal
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

        enum class Binary(val symbol: String, val priority: Int) : Operator {
            Or("||", 1),
            And("&&", 2),
            Equal("=", 3),
            Greater(">", 4),
            GreaterOrEqual(">=", 4),
            Less("<", 4),
            LessOrEqual("<=", 4),
            Plus("+", 5),
            Minus("-", 5),
            Multiplication("*", 6),
            Division("/", 6),
            Modulo("%", 6),
            Power("^", 7);
        }
    }

    enum class Bracket(val symbol: String) : Token {
        LeftRound("("),
        RightRound(")")
    }

    data class Function(val name: String) : Token {
        enum class ArgumentDelimiter: Token {
            Comma
        }
    }
}
