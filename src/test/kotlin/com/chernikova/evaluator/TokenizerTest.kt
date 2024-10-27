package com.chernikova.evaluator

import com.echernikova.evaluator.core.tokenizing.Token
import com.echernikova.evaluator.core.tokenizing.Tokenizer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class TokenizerTest {

    @Test
    fun `tokenizer correctly parse literals`() {
        val input = arrayOf<Pair<String, Array<Token>>>(
            "1" to arrayOf(Token.Literal.Int(1)),
            "123456789" to arrayOf(Token.Literal.Int(123456789)),
            "0.2389" to arrayOf(Token.Literal.Double(0.2389)),
            "17856.1285" to arrayOf(Token.Literal.Double(17856.1285)),
            "11111.520" to arrayOf(Token.Literal.Double(11111.520)),
            "    \"125 36.52, 85.0.0.;\t\" \t" to arrayOf(Token.Literal.Str("125 36.52, 85.0.0.;\t")),
            "\"\"" to arrayOf(Token.Literal.Str("")),
            "true" to arrayOf(Token.Literal.Bool(true)),
            "false" to arrayOf(Token.Literal.Bool(false))
        )

        input.forEach { (str, expected) -> runComparison(str, expected) }
    }

    @Test
    fun `tokenizer correctly parse binary operators`() {
        val input = arrayOf(
            "1+123.0" to arrayOf(Token.Literal.Int(1), Token.Operator.Binary.Plus, Token.Literal.Double(123.0)),
            "16 - \"ssss\"" to arrayOf(Token.Literal.Int(16), Token.Operator.Binary.Minus, Token.Literal.Str("ssss")),
            "24 / C1" to arrayOf(Token.Literal.Int(24), Token.Operator.Binary.Division, Token.Cell.CellLink("C1")),
            "11111.520 * 12" to arrayOf(Token.Literal.Double(11111.520), Token.Operator.Binary.Multiplication, Token.Literal.Int(12)),
            "16 - \"ssss\"" to arrayOf(Token.Literal.Int(16), Token.Operator.Binary.Minus, Token.Literal.Str("ssss")),
            "1 >= 2" to arrayOf(Token.Literal.Int(1), Token.Operator.Binary.GreaterOrEqual, Token.Literal.Int(2)),
            "1 > +2" to arrayOf(Token.Literal.Int(1), Token.Operator.Binary.Greater, Token.Operator.Unary.Plus, Token.Literal.Int(2)),
            "1 < 2" to arrayOf(Token.Literal.Int(1), Token.Operator.Binary.Less, Token.Literal.Int(2)),
            "1 <= 2.02" to arrayOf(Token.Literal.Int(1), Token.Operator.Binary.LessOrEqual, Token.Literal.Double(2.02)),
            "1|| 5000" to arrayOf(Token.Literal.Int(1), Token.Operator.Binary.Or, Token.Literal.Int(5000)),
            "1 && C2" to arrayOf(Token.Literal.Int(1), Token.Operator.Binary.And, Token.Cell.CellLink("C2")),
        )

        input.forEach { (str, expected) -> runComparison(str, expected) }
    }

    @Test
    fun `tokenizer correctly parse unary operators`() {
        val input = arrayOf(
            "+123.0" to arrayOf(Token.Operator.Unary.Plus, Token.Literal.Double(123.0)),
            "- \"ssss\"" to arrayOf(Token.Operator.Unary.Minus, Token.Literal.Str("ssss"))
        )

        input.forEach { (str, expected) -> runComparison(str, expected) }
    }

    @Test
    fun `tokenizer correctly parse cell links`() {
        val input = arrayOf(
            "C1:C6" to arrayOf<Token>(Token.Cell.CellLink("C1"), Token.Cell.CellDelimiter, Token.Cell.CellLink("C6")),
            "A8999" to arrayOf<Token>(Token.Cell.CellLink("A8999"))
        )

        input.forEach { (str, expected) -> runComparison(str, expected) }
    }

    @Test
    fun `tokenizer correctly parse complex expressions`() {
        val input = arrayOf(
            "C1:C6 + 1.2222 - - 15" to arrayOf(
                Token.Cell.CellLink("C1"), Token.Cell.CellDelimiter, Token.Cell.CellLink("C6"),
                Token.Operator.Binary.Plus, Token.Literal.Double(1.2222), Token.Operator.Binary.Minus,
                Token.Operator.Unary.Minus, Token.Literal.Int(15)
            ),
            "A8999 ^ (12 - \"15 85 25555 -1) \" \t)\t    85.2" to arrayOf(
                Token.Cell.CellLink("A8999"), Token.Operator.Binary.Power, Token.Bracket.LeftRound,
                Token.Literal.Int(12), Token.Operator.Binary.Minus, Token.Literal.Str("15 85 25555 -1) "),
                Token.Bracket.RightRound, Token.Literal.Double(85.2)
            )
        )

        input.forEach { (str, expected) -> runComparison(str, expected) }
    }

    @Test
    fun `tokenizer correctly parse functions`() {
        val input = arrayOf(
            "SUM(C1:C6; 5)" to arrayOf(
                Token.Function("SUM"), Token.Bracket.LeftRound,
                Token.Cell.CellLink("C1"), Token.Cell.CellDelimiter, Token.Cell.CellLink("C6"),
                Token.Function.ArgumentDelimiter, Token.Literal.Int(5), Token.Bracket.RightRound
            ),
            "MIN(A5)" to arrayOf(
                Token.Function("MIN"), Token.Bracket.LeftRound,
                Token.Cell.CellLink("A5"), Token.Bracket.RightRound
            )
        )

        input.forEach { (str, expected) -> runComparison(str, expected) }
    }

    @Test
    fun `tokenizer check cell link for correctness`() {
        val input = arrayOf(
            "AA8" to "Incorrect cell link AA8"
        )

        input.forEach { (str, expected) -> runComparisonForFail(str, expected) }
    }

    @Test
    fun `tokenizer check string is correctly terminated`() {
        val input = arrayOf(
            "C8 + \"Lorem ipsum" to "Unterminated string '\"Lorem ipsum'",
            "\"Boo! Scared?\" \"Don't be afraid,\" \" I'm a friend, I won't hurt you." to
                    "Unterminated string '\" I'm a friend, I won't hurt you.'"
        )

        input.forEach { (str, expected) -> runComparisonForFail(str, expected) }
    }

    @Test
    fun `tokenizer check numbers for correctness`() {
        val input = arrayOf(
            "C1:C6 + 1.2222.0.8 - - 15" to "Invalid number: 1.2222.0.8",
            "15252525347852" to "Invalid number: 15252525347852",
        )

        input.forEach { (str, expected) -> runComparisonForFail(str, expected) }
    }

    private fun runComparison(string: String, expected: Array<Token>) {
        val output = Tokenizer.tokenize(string)
        assertEquals(output.size, expected.size)

        output.forEachIndexed { i, value -> assertEquals(value, expected[i]) }
    }

    private fun runComparisonForFail(string: String, errorMessage: String) {
        kotlin.runCatching {
            Tokenizer.tokenize(string)

        }.onFailure { exception ->
            assertEquals(errorMessage, exception.message)
        }.onSuccess {
            throw AssertionError("Expected exceptions '$errorMessage' on tokenizing '$string'")
        }
    }
}
