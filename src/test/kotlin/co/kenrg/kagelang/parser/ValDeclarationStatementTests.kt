package co.kenrg.kagelang.parser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ValDeclarationStatementTests {

    @Test fun parsesSingleValDeclarationAssignment_Int() {
        val code = "val a = 1"
        val parseTreeStr = toParseTree(parseCode(code)).multiLineString()
        assertEquals(
                """KageFile
                  |  Line
                  |    ValDeclarationStatement
                  |      ValDeclaration
                  |        Leaf[val]
                  |        Assignment
                  |          Leaf[a]
                  |          Leaf[=]
                  |          IntLiteral
                  |            Leaf[1]
                  |    Leaf[<EOF>]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesSingleValDeclarationAssignment_Decimal() {
        val code = "val a = 1.114"
        val parseTreeStr = toParseTree(parseCode(code)).multiLineString()
        assertEquals(
                """KageFile
                  |  Line
                  |    ValDeclarationStatement
                  |      ValDeclaration
                  |        Leaf[val]
                  |        Assignment
                  |          Leaf[a]
                  |          Leaf[=]
                  |          DecLiteral
                  |            Leaf[1.114]
                  |    Leaf[<EOF>]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesSingleValDeclarationAssignment_Bool() {
        val code = "val a = true"
        val parseTreeStr = toParseTree(parseCode(code)).multiLineString()
        assertEquals(
                """KageFile
                  |  Line
                  |    ValDeclarationStatement
                  |      ValDeclaration
                  |        Leaf[val]
                  |        Assignment
                  |          Leaf[a]
                  |          Leaf[=]
                  |          BoolLiteral
                  |            Leaf[true]
                  |    Leaf[<EOF>]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesSingleValDeclarationAssignment_BinaryOperation() {
        val code = "val a = 1 + 2"
        val parseTreeStr = toParseTree(parseCode(code)).multiLineString()
        assertEquals(
                """KageFile
                  |  Line
                  |    ValDeclarationStatement
                  |      ValDeclaration
                  |        Leaf[val]
                  |        Assignment
                  |          Leaf[a]
                  |          Leaf[=]
                  |          BinaryOperation
                  |            IntLiteral
                  |              Leaf[1]
                  |            Leaf[+]
                  |            IntLiteral
                  |              Leaf[2]
                  |    Leaf[<EOF>]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesMultipleValDeclarations() {
        val code = """val a = 10
                     |val b = 5""".trimMargin("|")
        val parseTreeStr = toParseTree(parseCode(code)).multiLineString()
        assertEquals(
                """KageFile
                  |  Line
                  |    ValDeclarationStatement
                  |      ValDeclaration
                  |        Leaf[val]
                  |        Assignment
                  |          Leaf[a]
                  |          Leaf[=]
                  |          IntLiteral
                  |            Leaf[10]
                  |    Leaf[<NEWLINE>]
                  |  Line
                  |    ValDeclarationStatement
                  |      ValDeclaration
                  |        Leaf[val]
                  |        Assignment
                  |          Leaf[b]
                  |          Leaf[=]
                  |          IntLiteral
                  |            Leaf[5]
                  |    Leaf[<EOF>]
                  |""".trimMargin("|"),
                parseTreeStr)
    }
}
