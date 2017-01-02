package co.kenrg.kagelang.parser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ParenExpressionTests {

    @Test fun parsesParenExpression_IntAddition() {
        val code = "(1 + 2)"
        val binaryOperation = parseCode(code).line(0).statementOrExpression().expression()
        val parseTreeStr = toParseTree(binaryOperation).multiLineString()
        assertEquals(
                """ParenExpression
                  |  Leaf[(]
                  |  BinaryOperation
                  |    IntLiteral
                  |      Leaf[1]
                  |    Leaf[+]
                  |    IntLiteral
                  |      Leaf[2]
                  |  Leaf[)]
                  |""".trimMargin("|"),
                parseTreeStr)
    }
}