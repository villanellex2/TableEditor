package com.echernikova.evaluator

import com.echernikova.evaluator.core.tokenizing.Token
import com.echernikova.evaluator.core.tokenizing.Tokenizer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertFails

class TokenizerTest {

    @ParameterizedTest(name = "tokenizer parses input: {0}")
    @MethodSource("correct")
    fun `tokenizer correctly works with correct cases`(input: String, expected: Array<Token>) {
        runComparison(input, expected)
    }

    @ParameterizedTest(name = "tokenizer failure on input: {0}")
    @MethodSource("incorrect")
    fun `tokenizer fail on incorrect expressions`(input: String, errorMessage: String) {
        runComparisonForFail(input, errorMessage)
    }

    private fun runComparison(string: String, expected: Array<Token>) {
        val output = Tokenizer.tokenize(string)
        assertEquals(expected.size, output.size)
        output.forEachIndexed { i, value -> assertEquals(expected[i], value) }
    }

    private fun runComparisonForFail(string: String, errorMessage: String) {
        assertFails(errorMessage) {
            Tokenizer.tokenize(string)
        }
    }

    companion object {
        @JvmStatic
        fun correct(): Stream<Arguments> = Stream.of(
            Arguments.of("1", arrayOf(Token.Literal.Int(1))),
            Arguments.of("123456789", arrayOf(Token.Literal.Int(123456789))),
            Arguments.of("0.2389", arrayOf(Token.Literal.Double(0.2389))),
            Arguments.of("17856.1285", arrayOf(Token.Literal.Double(17856.1285))),
            Arguments.of("\"\"", arrayOf(Token.Literal.Str(""))),
            Arguments.of("true", arrayOf(Token.Literal.Bool(true))),
            Arguments.of("false", arrayOf(Token.Literal.Bool(false))),

            Arguments.of("1+123.0",
                arrayOf(Token.Literal.Int(1), Token.Operator.Binary.Plus, Token.Literal.Double(123.0))
            ),
            Arguments.of("24 / C1",
                arrayOf(Token.Literal.Int(24), Token.Operator.Binary.Division, Token.Cell.CellLink("C1"))
            ),
            Arguments.of("1 >= 2",
                arrayOf(Token.Literal.Int(1), Token.Operator.Binary.GreaterOrEqual, Token.Literal.Int(2))
            ),


            Arguments.of("+123.0", arrayOf(Token.Operator.Unary.Plus, Token.Literal.Double(123.0))),
            Arguments.of("- \"ssss\"", arrayOf(Token.Operator.Unary.Minus, Token.Literal.Str("ssss"))),


            Arguments.of("C1:C6",
                arrayOf(Token.Cell.CellLink("C1"), Token.Cell.CellDelimiter, Token.Cell.CellLink("C6"))
            ),
            Arguments.of("A8999", arrayOf(Token.Cell.CellLink("A8999"))),


            Arguments.of(
                "C1:C6 + 1.2222 - - 15", arrayOf(
                    Token.Cell.CellLink("C1"), Token.Cell.CellDelimiter, Token.Cell.CellLink("C6"),
                    Token.Operator.Binary.Plus, Token.Literal.Double(1.2222), Token.Operator.Binary.Minus,
                    Token.Operator.Unary.Minus, Token.Literal.Int(15)
                )
            ),


            Arguments.of(
                "SUM(C1:C6; 5)",
                arrayOf(
                    Token.Function("SUM"), Token.Bracket.LeftRound,
                    Token.Cell.CellLink("C1"), Token.Cell.CellDelimiter, Token.Cell.CellLink("C6"),
                    Token.Function.ArgumentDelimiter.Comma, Token.Literal.Int(5), Token.Bracket.RightRound
                ),
            ),
        )

        @JvmStatic
        fun incorrect(): Stream<Arguments> = Stream.of(
            Arguments.of("AA8", "Incorrect cell link AA8"),
            Arguments.of("C8 + \"Lorem ipsum", "Unterminated string '\"Lorem ipsum'"),
            Arguments.of("C1:C6 + 1.2222.0.8 - - 15", "Invalid number: 1.2222.0.8"),
            Arguments.of("C1:C6 + ${Integer.MAX_VALUE.toLong() + 1} - - 15", "Invalid number: ${Integer.MAX_VALUE.toLong() + 1}")
        )
    }
}
