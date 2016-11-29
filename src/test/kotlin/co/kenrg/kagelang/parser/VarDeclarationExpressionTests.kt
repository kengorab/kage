package co.kenrg.kagelang.parser

import co.kenrg.kagelang.helper.parseCode
import co.kenrg.kagelang.helper.toParseTree
import org.junit.Test
import kotlin.test.assertEquals

class VarDeclarationExpressionTests {

    @Test fun parsesSingleVarDeclarationAssignment_Int() {
        val code = "var a = 1"
        val parseTreeStr = toParseTree(parseCode(code)).multiLineString()
        assertEquals(
                """KageFile
                  |  Line
                  |    VarDeclarationStatement
                  |      VarDeclaration
                  |        Leaf[var]
                  |        Assignment
                  |          Leaf[a]
                  |          Leaf[=]
                  |          IntLiteral
                  |            Leaf[1]
                  |    Leaf[<EOF>]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesSingleVarDeclarationAssignment_Decimal() {
        val code = "var a = 1.114"
        val parseTreeStr = toParseTree(parseCode(code)).multiLineString()
        assertEquals(
                """KageFile
                  |  Line
                  |    VarDeclarationStatement
                  |      VarDeclaration
                  |        Leaf[var]
                  |        Assignment
                  |          Leaf[a]
                  |          Leaf[=]
                  |          DecLiteral
                  |            Leaf[1.114]
                  |    Leaf[<EOF>]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesSingleVarDeclarationAssignment_Bool() {
        val code = "var a = true"
        val parseTreeStr = toParseTree(parseCode(code)).multiLineString()
        assertEquals(
                """KageFile
                  |  Line
                  |    VarDeclarationStatement
                  |      VarDeclaration
                  |        Leaf[var]
                  |        Assignment
                  |          Leaf[a]
                  |          Leaf[=]
                  |          BoolLiteral
                  |            Leaf[true]
                  |    Leaf[<EOF>]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesSingleVarDeclarationAssignment_BinaryOperation() {
        val code = "var a = 1 + 2"
        val parseTreeStr = toParseTree(parseCode(code)).multiLineString()
        assertEquals(
                """KageFile
                  |  Line
                  |    VarDeclarationStatement
                  |      VarDeclaration
                  |        Leaf[var]
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

    @Test fun parsesMultipleVarDeclarations() {
        val code = """var a = 10
                     |var b = 5""".trimMargin("|")
        val parseTreeStr = toParseTree(parseCode(code)).multiLineString()
        assertEquals(
                """KageFile
                  |  Line
                  |    VarDeclarationStatement
                  |      VarDeclaration
                  |        Leaf[var]
                  |        Assignment
                  |          Leaf[a]
                  |          Leaf[=]
                  |          IntLiteral
                  |            Leaf[10]
                  |    Leaf[<NEWLINE>]
                  |  Line
                  |    VarDeclarationStatement
                  |      VarDeclaration
                  |        Leaf[var]
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
