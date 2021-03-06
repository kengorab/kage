package co.kenrg.kagelang.lexer

import co.kenrg.kagelang.KageLexer
import org.antlr.v4.runtime.ANTLRInputStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.StringReader
import java.util.*

class KageLexerTests {

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

    @Test fun parseValDeclarationAssignedAStringLiteral() {
        assertEquals(
                listOf("VAL", "Identifier", "ASSIGN", "StringLiteral", "EOF"),
                tokens(lexerForCode("val s = \"Hello World!\""))
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

    @Test fun parseStringLiteralSingleChar() {
        assertEquals(
                listOf("StringLiteral", "EOF"),
                tokens(lexerForCode("\"h\""))
        )
    }

    @Test fun parseStringLiteral() {
        assertEquals(
                listOf("StringLiteral", "EOF"),
                tokens(lexerForCode("\"hello world\""))
        )
    }

    @Test fun parseStringLiteral_stringEncodesBinaryExpression() {
        assertEquals(
                listOf("StringLiteral", "EOF"),
                tokens(lexerForCode("\"1 + 2\""))
        )
    }

    @Test fun parseConcatenationOfTwoStrings() {
        assertEquals(
                listOf("StringLiteral", "CONCAT", "StringLiteral", "EOF"),
                tokens(lexerForCode("\"Hello\" ++ \"World\""))
        )
    }

    @Test fun parseConcatenationOfManyStrings() {
        assertEquals(
                listOf("StringLiteral", "CONCAT", "StringLiteral", "CONCAT", "StringLiteral", "CONCAT", "StringLiteral", "EOF"),
                tokens(lexerForCode("\"Concat\" ++ \"many\" ++ \"strings\" ++ \"together\""))
        )
    }

    @Test fun parseTypeAnnotation() {
        assertEquals(
                listOf("VAL", "Identifier", "COLON", "Identifier", "ASSIGN", "IntLiteral", "EOF"),
                tokens(lexerForCode("val a: Int = 1"))
        )
    }

    @Test fun parseEmptyFunctionDeclaration() {
        assertEquals(
                listOf("FN", "Identifier", "LPAREN", "RPAREN", "EOF"),
                tokens(lexerForCode("fn add()"))
        )
    }

    @Test fun parseFunctionDeclaration_fnBodyIsInt() {
        assertEquals(
                listOf("FN", "Identifier", "LPAREN", "RPAREN", "ASSIGN", "IntLiteral", "EOF"),
                tokens(lexerForCode("fn three() = 3"))
        )
    }

    @Test fun parseFunctionDeclaration_fnBodyIsPrintStatement() {
        assertEquals(
                listOf("FN", "Identifier", "LPAREN", "RPAREN", "ASSIGN", "PRINT", "LPAREN", "StringLiteral", "RPAREN", "EOF"),
                tokens(lexerForCode("fn printHello() = print(\"hello\")"))
        )
    }

    @Test fun parseFunctionDeclaration_fnBodyIsPrintStatement_bodyIsOnNewline() {
        val code = """fn printHello() =
                     |  print("hello")""".trimMargin("|")
        assertEquals(
                listOf("FN", "Identifier", "LPAREN", "RPAREN", "ASSIGN", "NEWLINE", "PRINT", "LPAREN", "StringLiteral", "RPAREN", "EOF"),
                tokens(lexerForCode(code))
        )
    }

    @Test fun parseFunctionDeclaration_returnTypeAnnotation() {
        assertEquals(
                listOf("FN", "Identifier", "LPAREN", "RPAREN", "COLON", "Identifier", "ASSIGN", "IntLiteral", "EOF"),
                tokens(lexerForCode("fn abc(): Int = 3"))
        )
    }

    @Test fun parseFunctionDeclaration_oneParam() {
        assertEquals(
                listOf("FN", "Identifier", "LPAREN", "Identifier", "COLON", "Identifier", "RPAREN", "ASSIGN", "IntLiteral", "EOF"),
                tokens(lexerForCode("fn abc(x: Int) = 3"))
        )
    }

    @Test fun parseFunctionDeclaration_manyParams() {
        assertEquals(
                listOf("FN", "Identifier", "LPAREN", "Identifier", "COLON", "Identifier", "COMMA", "Identifier", "COLON", "Identifier", "COMMA", "Identifier", "COLON", "Identifier", "RPAREN", "ASSIGN", "IntLiteral", "EOF"),
                tokens(lexerForCode("fn abc(x: Int, y: Int, z: Int) = 3"))
        )
    }

    @Test fun parseFunctionInvocation_noParams() {
        assertEquals(
                listOf("Identifier", "LPAREN", "RPAREN", "EOF"),
                tokens(lexerForCode("add()"))
        )
    }

    @Test fun parseLetInExpression_singleBinding_singleLine() {
        val code = "let val a = 123 in print(a)"
        assertEquals(
                listOf("LET", "VAL", "Identifier", "ASSIGN", "IntLiteral", "IN", "PRINT", "LPAREN", "Identifier", "RPAREN", "EOF"),
                tokens(lexerForCode(code))
        )
    }

    @Test fun parseLetInExpression_singleBinding_multipleLines() {
        val code = """let
                     |  val a = 123
                     |in
                     |  print(a)""".trimMargin("|")
        assertEquals(
                listOf(
                        "LET",
                        "NEWLINE",
                        "VAL", "Identifier", "ASSIGN", "IntLiteral",
                        "NEWLINE",
                        "IN",
                        "NEWLINE",
                        "PRINT", "LPAREN", "Identifier", "RPAREN",
                        "EOF"
                ),
                tokens(lexerForCode(code))
        )
    }

    @Test fun parseLetInExpression_singleBinding_nestedLetIn_multipleLines() {
        val code = """let
                     |  val a = let
                     |      val b = 11
                     |    in
                     |      b + 1
                     |in
                     |  print(a)""".trimMargin("|")
        assertEquals(
                listOf(
                        "LET",
                        "NEWLINE",
                        "VAL", "Identifier", "ASSIGN", "LET",
                        "NEWLINE",
                        "VAL", "Identifier", "ASSIGN", "IntLiteral",
                        "NEWLINE",
                        "IN",
                        "NEWLINE",
                        "Identifier", "PLUS", "IntLiteral",
                        "NEWLINE",
                        "IN",
                        "NEWLINE",
                        "PRINT", "LPAREN", "Identifier", "RPAREN",
                        "EOF"
                ),
                tokens(lexerForCode(code))
        )
    }

    @Test fun parseLetInExpression_multipleBindings_multipleLines() {
        val code = """let
                     |  val a = 123
                     |  val b = 456
                     |in
                     |  print(a + b)""".trimMargin("|")
        assertEquals(
                listOf(
                        "LET",
                        "NEWLINE",
                        "VAL", "Identifier", "ASSIGN", "IntLiteral",
                        "NEWLINE",
                        "VAL", "Identifier", "ASSIGN", "IntLiteral",
                        "NEWLINE",
                        "IN",
                        "NEWLINE",
                        "PRINT", "LPAREN", "Identifier", "PLUS", "Identifier", "RPAREN",
                        "EOF"
                ),
                tokens(lexerForCode(code))
        )
    }

    @Test fun parseGreaterThan() {
        assertEquals(
                listOf("IntLiteral", "RANGL", "IntLiteral", "EOF"),
                tokens(lexerForCode("1 > 2"))
        )
    }

    @Test fun parseLessThan() {
        assertEquals(
                listOf("IntLiteral", "LANGL", "IntLiteral", "EOF"),
                tokens(lexerForCode("2 < 1"))
        )
    }

    @Test fun parseGreaterThanOrEqual() {
        assertEquals(
                listOf("IntLiteral", "GTE", "IntLiteral", "EOF"),
                tokens(lexerForCode("1 >= 2"))
        )
    }

    @Test fun parseLessThanOrEqual() {
        assertEquals(
                listOf("IntLiteral", "LTE", "IntLiteral", "EOF"),
                tokens(lexerForCode("2 <= 1"))
        )
    }

    @Test fun parseSimpleEq() {
        assertEquals(
                listOf("IntLiteral", "EQ", "IntLiteral", "EOF"),
                tokens(lexerForCode("2 == 1"))
        )
    }

    @Test fun parseEqOfExpressions() {
        assertEquals(
                listOf("IntLiteral", "LTE", "IntLiteral", "EQ", "IntLiteral", "GTE", "IntLiteral", "EOF"),
                tokens(lexerForCode("2 <= 1 == 1 >= 2"))
        )
    }

    @Test fun parseSimpleNeq() {
        assertEquals(
                listOf("IntLiteral", "NEQ", "IntLiteral", "EOF"),
                tokens(lexerForCode("2 != 1"))
        )
    }

    @Test fun parseNeqOfExpressions() {
        assertEquals(
                listOf("IntLiteral", "LTE", "IntLiteral", "NEQ", "IntLiteral", "GTE", "IntLiteral", "EOF"),
                tokens(lexerForCode("2 <= 1 != 1 >= 2"))
        )
    }

    @Test fun parseIfExpressionWithNoElse() {
        val code = "if true then \"asdf\""
        assertEquals(
                listOf("IF", "BooleanLiteral", "THEN", "StringLiteral", "EOF"),
                tokens(lexerForCode(code))
        )
    }

    @Test fun parseIfExpressionNewlinesWithNoElse() {
        val code = """if true
                     |then "asdf"
                   """.trimMargin("|")
        assertEquals(
                listOf("IF", "BooleanLiteral", "NEWLINE", "THEN", "StringLiteral", "EOF"),
                tokens(lexerForCode(code))
        )
    }

    @Test fun parseIfExpressionWithElse() {
        val code = "if true then \"asdf\" else \"qwer\""
        assertEquals(
                listOf("IF", "BooleanLiteral", "THEN", "StringLiteral", "ELSE", "StringLiteral", "EOF"),
                tokens(lexerForCode(code))
        )
    }

    @Test fun parseIfExpressionWithElseWithNewlinesSeparatingClauses() {
        val code = """if true
                     |then "asdf"
                     |else "qwer"
                   """.trimMargin("|")
        assertEquals(
                listOf("IF", "BooleanLiteral", "NEWLINE", "THEN", "StringLiteral", "NEWLINE", "ELSE", "StringLiteral", "EOF"),
                tokens(lexerForCode(code))
        )
    }

    @Test fun parseIfExpressionWithElseWithNewlinesSeparatingEverything() {
        val code = """if true
                     |then "asdf"
                     |else "qwer"
                   """.trimMargin("|")
        assertEquals(
                listOf("IF", "BooleanLiteral", "NEWLINE", "THEN", "StringLiteral", "NEWLINE", "ELSE", "StringLiteral", "EOF"),
                tokens(lexerForCode(code))
        )
    }

    @Test fun parseTypeDeclaration_emptyBody() {
        assertEquals(
                listOf("TYPE", "Identifier", "EOF"),
                tokens(lexerForCode("type Empty"))
        )
    }

    @Test fun parseTypeDeclaration_oneProp() {
        assertEquals(
                listOf("TYPE", "Identifier", "LBRACE", "Identifier", "COLON", "Identifier", "RBRACE", "EOF"),
                tokens(lexerForCode("type OneProp { count: Int }"))
        )
    }

    @Test fun parseTypeDeclaration_twoPropsSameLine() {
        assertEquals(
                listOf("TYPE", "Identifier", "LBRACE", "Identifier", "COLON", "Identifier", "COMMA", "Identifier", "COLON", "Identifier", "RBRACE", "EOF"),
                tokens(lexerForCode("type TwoProps { count: Int, label: String }"))
        )
    }

    @Test fun parseTypeDeclaration_twoPropsWithNewlines() {
        assertEquals(
                listOf("TYPE", "Identifier", "LBRACE", "Identifier", "COLON", "Identifier", "COMMA", "NEWLINE", "Identifier", "COLON", "Identifier", "RBRACE", "EOF"),
                tokens(lexerForCode("type TwoProps { count: Int,\nlabel: String }"))
        )
    }

    @Test fun parsePropAccessor_oneLevel() {
        assertEquals(
                listOf("Identifier", "DOT", "Identifier", "EOF"),
                tokens(lexerForCode("a.b"))
        )
    }

    @Test fun parsePropAccessor_twoLevels() {
        assertEquals(
                listOf("Identifier", "DOT", "Identifier", "DOT", "Identifier", "EOF"),
                tokens(lexerForCode("a.b.c"))
        )
    }

    @Test fun parsePropAccessor_threeLevelsAndInvocation() {
        assertEquals(
                listOf("Identifier", "DOT", "Identifier", "DOT", "Identifier", "DOT", "Identifier", "LPAREN", "RPAREN", "EOF"),
                tokens(lexerForCode("a.b.c.d()"))
        )
    }

    @Test fun parseTuple_pair() {
        assertEquals(
                listOf("LPAREN", "IntLiteral", "COMMA", "IntLiteral", "RPAREN", "EOF"),
                tokens(lexerForCode("(1, 3)"))
        )
    }

    @Test fun parseTuple_pair_nestedTuple() {
        assertEquals(
                listOf("LPAREN", "IntLiteral", "COMMA", "LPAREN", "IntLiteral", "COMMA", "IntLiteral", "RPAREN", "RPAREN", "EOF"),
                tokens(lexerForCode("(1, (2, 3))"))
        )
    }

    @Test fun parseTuple_triple() {
        assertEquals(
                listOf("LPAREN", "IntLiteral", "COMMA", "IntLiteral", "COMMA", "IntLiteral", "RPAREN", "EOF"),
                tokens(lexerForCode("(1, 2, 3)"))
        )
    }

    @Test fun parseArray_singleElement() {
        assertEquals(
                listOf("LBRACK", "IntLiteral", "RBRACK", "EOF"),
                tokens(lexerForCode("[1]"))
        )
    }

    @Test fun parseArray_manyElements() {
        assertEquals(
                listOf("LBRACK", "IntLiteral", "COMMA", "IntLiteral", "COMMA", "IntLiteral", "COMMA", "IntLiteral", "RBRACK", "EOF"),
                tokens(lexerForCode("[1, 2, 3, 4]"))
        )
    }

    private fun lexerForCode(code: String) = KageLexer(ANTLRInputStream(StringReader(code)))

    private fun tokens(lexer: KageLexer): List<String> {
        val tokens = LinkedList<String>()
        do {
            val t = lexer.nextToken()
            when (t.type) {
                -1 -> tokens.add("EOF")
                else -> if (t.type != KageLexer.WS) {
                    // Use vocabulary to get symbolic name.
                    // Using lexer.ruleNames includes fragments, which mess up the indexing.
                    val tokenName = lexer.vocabulary.getSymbolicName(t.type)
                    tokens.add(tokenName)
                }
            }
        } while (t.type != -1)

        return tokens
    }
}