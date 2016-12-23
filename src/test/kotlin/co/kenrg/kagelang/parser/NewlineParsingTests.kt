package co.kenrg.kagelang.parser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NewlineParsingTests {
    @Test fun parsesCodeTerminatedWithNewline() {
        val code = """var a = 10
                     |""".trimMargin("|")
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
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesStatementsSeparatedByManyNewlines() {
        val code = """var a = 10
                     |
                     |
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
                  |    Leaf[<NEWLINE>]
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