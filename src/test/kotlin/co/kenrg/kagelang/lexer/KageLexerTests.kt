package co.kenrg.kagelang.lexer

import co.kenrg.kagelang.KageLexer
import org.antlr.v4.runtime.ANTLRInputStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.StringReader
import java.util.*

class KageLexerTests {
    fun lexerForCode(code: String) = KageLexer(ANTLRInputStream(StringReader(code)))

    fun tokens(lexer: KageLexer): List<String> {
        val tokens = LinkedList<String>()
        do {
            val t = lexer.nextToken()
            when (t.type) {
                -1 -> tokens.add("EOF")
                else -> if (t.type != KageLexer.WS) tokens.add(lexer.ruleNames[t.type - 1])
            }
        } while (t.type != -1)

        return tokens
    }

    @Test fun parseValDeclarationAssignedAnIntegerLiteral() {
        assertEquals(
                listOf("VAL", "Identifier", "ASSIGN", "IntLiteral", "EOF"),
                tokens(lexerForCode("val a = 1"))
        )
    }

    @Test fun parseValDeclarationAssignedADecimalLiteral() {
        assertEquals(
                listOf("VAL", "Identifier", "ASSIGN", "DecimalLiteral", "EOF"),
                tokens(lexerForCode("val a = 1.23"))
        )
    }

    @Test fun parseValDeclarationAssignedABoolTrueLiteral() {
        assertEquals(
                listOf("VAL", "Identifier", "ASSIGN", "BooleanLiteral", "EOF"),
                tokens(lexerForCode("val t = true"))
        )
    }

    @Test fun parseValDeclarationAssignedABoolFalseLiteral() {
        assertEquals(
                listOf("VAL", "Identifier", "ASSIGN", "BooleanLiteral", "EOF"),
                tokens(lexerForCode("val t = false"))
        )
    }

    @Test fun parseMultipleVals() {
        val code = """val a = 1
                     |val b = 2""".trimMargin("|")
        assertEquals(
                listOf(
                        "VAL", "Identifier", "ASSIGN", "IntLiteral", "NEWLINE",
                        "VAL", "Identifier", "ASSIGN", "IntLiteral", "EOF"
                ),
                tokens(lexerForCode(code))
        )
    }

    @Test fun parseMultipleNewlines() {
        val code = """val a = 1
                     |
                     |print(a)
                     |""".trimMargin("|")
        assertEquals(
                listOf(
                        "VAL", "Identifier", "ASSIGN", "IntLiteral", "NEWLINE",
                        "NEWLINE",
                        "PRINT", "LPAREN", "Identifier", "RPAREN", "NEWLINE",
                        "EOF"
                ),
                tokens(lexerForCode(code))
        )
    }

    @Test fun parseValDeclarationAssignedASum() {
        assertEquals(
                listOf("VAL", "Identifier", "ASSIGN", "IntLiteral", "PLUS", "IntLiteral", "EOF"),
                tokens(lexerForCode("val a = 1 + 2"))
        )
    }

    @Test fun parseMathematicalExpression() {
        assertEquals(
                listOf("IntLiteral", "PLUS", "Identifier", "ASTERISK", "IntLiteral", "DIVISION", "IntLiteral", "MINUS", "IntLiteral", "EOF"),
                tokens(lexerForCode("1 + a * 3 / 4 - 5"))
        )
    }

    @Test fun parseMathematicalExpressionWithParenthesis() {
        assertEquals(
                listOf("IntLiteral", "PLUS", "LPAREN", "Identifier", "ASTERISK", "IntLiteral", "RPAREN", "MINUS", "DecimalLiteral", "EOF"),
                tokens(lexerForCode("1 + (a * 3) - 5.12"))
        )
    }

    @Test fun parseBooleanOr() {
        assertEquals(
                listOf("BooleanLiteral", "PIPES", "BooleanLiteral", "EOF"),
                tokens(lexerForCode("true || false"))
        )
    }

    @Test fun parseBooleanAnd() {
        assertEquals(
                listOf("BooleanLiteral", "AMPS", "BooleanLiteral", "EOF"),
                tokens(lexerForCode("true && false"))
        )
    }
}