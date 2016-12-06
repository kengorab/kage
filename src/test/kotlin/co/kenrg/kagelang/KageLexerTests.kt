package co.kenrg.kagelang

import org.antlr.v4.runtime.ANTLRInputStream
import org.junit.Test
import java.io.StringReader
import java.util.*
import kotlin.test.assertEquals

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

    @Test fun parseVarDeclarationAssignedAnIntegerLiteral() {
        assertEquals(
                listOf("VAR", "ID", "ASSIGN", "INTLIT", "EOF"),
                tokens(lexerForCode("var a = 1"))
        )
    }

    @Test fun parseVarDeclarationAssignedADecimalLiteral() {
        assertEquals(
                listOf("VAR", "ID", "ASSIGN", "DECLIT", "EOF"),
                tokens(lexerForCode("var a = 1.23"))
        )
    }

    @Test fun parseVarDeclarationAssignedABoolTrueLiteral() {
        assertEquals(
                listOf("VAR", "ID", "ASSIGN", "BOOLLIT", "EOF"),
                tokens(lexerForCode("var t = true"))
        )
    }

    @Test fun parseVarDeclarationAssignedABoolFalseLiteral() {
        assertEquals(
                listOf("VAR", "ID", "ASSIGN", "BOOLLIT", "EOF"),
                tokens(lexerForCode("var t = false"))
        )
    }

    @Test fun parseMultipleVars() {
        val code = """var a = 1
                     |var b = 2""".trimMargin("|")
        assertEquals(
                listOf("VAR", "ID", "ASSIGN", "INTLIT", "NEWLINE", "VAR", "ID", "ASSIGN", "INTLIT", "EOF"),
                tokens(lexerForCode(code))
        )
    }

    @Test fun parseMultipleNewlines() {
        val code = """var a = 1
                     |
                     |print(a)
                     |""".trimMargin("|")
        assertEquals(
                listOf("VAR", "ID", "ASSIGN", "INTLIT", "NEWLINE", "NEWLINE", "PRINT", "LPAREN", "ID", "RPAREN", "NEWLINE", "EOF"),
                tokens(lexerForCode(code))
        )
    }

    @Test fun parseVarDeclarationAssignedASum() {
        assertEquals(
                listOf("VAR", "ID", "ASSIGN", "INTLIT", "PLUS", "INTLIT", "EOF"),
                tokens(lexerForCode("var a = 1 + 2"))
        )
    }

    @Test fun parseMathematicalExpression() {
        assertEquals(
                listOf("INTLIT", "PLUS", "ID", "ASTERISK", "INTLIT", "DIVISION", "INTLIT", "MINUS", "INTLIT", "EOF"),
                tokens(lexerForCode("1 + a * 3 / 4 - 5"))
        )
    }

    @Test fun parseMathematicalExpressionWithParenthesis() {
        assertEquals(
                listOf("INTLIT", "PLUS", "LPAREN", "ID", "ASTERISK", "INTLIT", "RPAREN", "MINUS", "DECLIT", "EOF"),
                tokens(lexerForCode("1 + (a * 3) - 5.12"))
        )
    }

    @Test fun parseBooleanOr() {
        assertEquals(
                listOf("BOOLLIT", "PIPES", "BOOLLIT", "EOF"),
                tokens(lexerForCode("true || false"))
        )
    }

    @Test fun parseBooleanAnd() {
        assertEquals(
                listOf("BOOLLIT", "AMPS", "BOOLLIT", "EOF"),
                tokens(lexerForCode("true && false"))
        )
    }
}