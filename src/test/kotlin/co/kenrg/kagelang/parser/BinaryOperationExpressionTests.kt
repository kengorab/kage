package co.kenrg.kagelang.parser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BinaryOperationExpressionTests {

    @Test fun parsesBinaryOperation_IntAddition() {
        val code = "1 + 2"
        val binaryOperation = parseCode(code).line(0).statementOrExpression().expression()
        val parseTreeStr = toParseTree(binaryOperation).multiLineString()
        assertEquals(
                """BinaryOperation
                  |  IntLiteral
                  |    Leaf[1]
                  |  Leaf[+]
                  |  IntLiteral
                  |    Leaf[2]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesBinaryOperation_DecimalAddition() {
        val code = "1.1 + 2.2"
        val binaryOperation = parseCode(code).line(0).statementOrExpression().expression()
        val parseTreeStr = toParseTree(binaryOperation).multiLineString()
        assertEquals(
                """BinaryOperation
                  |  DecLiteral
                  |    Leaf[1.1]
                  |  Leaf[+]
                  |  DecLiteral
                  |    Leaf[2.2]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesBinaryOperation_IntSubtraction() {
        val code = "1 - 2"
        val binaryOperation = parseCode(code).line(0).statementOrExpression().expression()
        val parseTreeStr = toParseTree(binaryOperation).multiLineString()
        assertEquals(
                """BinaryOperation
                  |  IntLiteral
                  |    Leaf[1]
                  |  Leaf[-]
                  |  IntLiteral
                  |    Leaf[2]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesBinaryOperation_DecimalSubtraction() {
        val code = "1.1 - 2.2"
        val binaryOperation = parseCode(code).line(0).statementOrExpression().expression()
        val parseTreeStr = toParseTree(binaryOperation).multiLineString()
        assertEquals(
                """BinaryOperation
                  |  DecLiteral
                  |    Leaf[1.1]
                  |  Leaf[-]
                  |  DecLiteral
                  |    Leaf[2.2]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesBinaryOperation_IntMultiplication() {
        val code = "1 * 2"
        val binaryOperation = parseCode(code).line(0).statementOrExpression().expression()
        val parseTreeStr = toParseTree(binaryOperation).multiLineString()
        assertEquals(
                """BinaryOperation
                  |  IntLiteral
                  |    Leaf[1]
                  |  Leaf[*]
                  |  IntLiteral
                  |    Leaf[2]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesBinaryOperation_DecimalMultiplication() {
        val code = "1.1 * 2.2"
        val binaryOperation = parseCode(code).line(0).statementOrExpression().expression()
        val parseTreeStr = toParseTree(binaryOperation).multiLineString()
        assertEquals(
                """BinaryOperation
                  |  DecLiteral
                  |    Leaf[1.1]
                  |  Leaf[*]
                  |  DecLiteral
                  |    Leaf[2.2]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesBinaryOperation_IntDivision() {
        val code = "1 / 2"
        val binaryOperation = parseCode(code).line(0).statementOrExpression().expression()
        val parseTreeStr = toParseTree(binaryOperation).multiLineString()
        assertEquals(
                """BinaryOperation
                  |  IntLiteral
                  |    Leaf[1]
                  |  Leaf[/]
                  |  IntLiteral
                  |    Leaf[2]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesBinaryOperation_DecimalDivision() {
        val code = "1.1 / 2.2"
        val binaryOperation = parseCode(code).line(0).statementOrExpression().expression()
        val parseTreeStr = toParseTree(binaryOperation).multiLineString()
        assertEquals(
                """BinaryOperation
                  |  DecLiteral
                  |    Leaf[1.1]
                  |  Leaf[/]
                  |  DecLiteral
                  |    Leaf[2.2]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesBinaryOperation_BooleanOr() {
        val code = "true || false"
        val binaryOperation = parseCode(code).line(0).statementOrExpression().expression()
        val parseTreeStr = toParseTree(binaryOperation).multiLineString()
        assertEquals(
                """BinaryOperation
                  |  BoolLiteral
                  |    Leaf[true]
                  |  Leaf[||]
                  |  BoolLiteral
                  |    Leaf[false]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesBinaryOperation_BooleanAnd() {
        val code = "true && false"
        val binaryOperation = parseCode(code).line(0).statementOrExpression().expression()
        val parseTreeStr = toParseTree(binaryOperation).multiLineString()
        assertEquals(
                """BinaryOperation
                  |  BoolLiteral
                  |    Leaf[true]
                  |  Leaf[&&]
                  |  BoolLiteral
                  |    Leaf[false]
                  |""".trimMargin("|"),
                parseTreeStr)
    }
}