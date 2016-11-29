package co.kenrg.kagelang

import co.kenrg.kagelang.helper.toParseTree
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.Test
import kotlin.test.assertEquals

class KageParserTests {
    fun lexerForResource(resourceName: String) =
            KageLexer(ANTLRInputStream(this.javaClass.getResourceAsStream("/$resourceName")))

    fun parseResource(resourceName: String): KageParser.KageFileContext =
            KageParser(CommonTokenStream(lexerForResource(resourceName))).kageFile()

    @Test fun parsesFileSingleVarDeclaration() {
        val parseTreeStr = toParseTree(parseResource("single-var-declaration.kg")).multiLineString()
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

    @Test fun parsesFileMultipleVarDeclaration() {
        val parseTreeStr = toParseTree(parseResource("multiple-var-declaration.kg")).multiLineString()
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
                  |    Leaf[<NEWLINE>]
                  |  Line
                  |    VarDeclarationStatement
                  |      VarDeclaration
                  |        Leaf[var]
                  |        Assignment
                  |          Leaf[c]
                  |          Leaf[=]
                  |          BinaryOperation
                  |            VarReference
                  |              Leaf[a]
                  |            Leaf[/]
                  |            VarReference
                  |              Leaf[b]
                  |    Leaf[<EOF>]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesFileSingleAdditionAssignment() {
        val parseTreeStr = toParseTree(parseResource("single-addition-assignment.kg")).multiLineString()
        assertEquals(
                """KageFile
                  |  Line
                  |    AssignmentStatement
                  |      Assignment
                  |        Leaf[a]
                  |        Leaf[=]
                  |        BinaryOperation
                  |          IntLiteral
                  |            Leaf[1]
                  |          Leaf[+]
                  |          IntLiteral
                  |            Leaf[2]
                  |    Leaf[<EOF>]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesFileMultipleVarDeclarationWithParens() {
        val parseTreeStr = toParseTree(parseResource("multiple-var-declaration-with-parens.kg")).multiLineString()
        assertEquals(
                """KageFile
                  |  Line
                  |    VarDeclarationStatement
                  |      VarDeclaration
                  |        Leaf[var]
                  |        Assignment
                  |          Leaf[a]
                  |          Leaf[=]
                  |          ParenExpression
                  |            Leaf[(]
                  |            BinaryOperation
                  |              IntLiteral
                  |                Leaf[1]
                  |              Leaf[+]
                  |              IntLiteral
                  |                Leaf[2]
                  |            Leaf[)]
                  |    Leaf[<NEWLINE>]
                  |  Line
                  |    VarDeclarationStatement
                  |      VarDeclaration
                  |        Leaf[var]
                  |        Assignment
                  |          Leaf[b]
                  |          Leaf[=]
                  |          BinaryOperation
                  |            ParenExpression
                  |              Leaf[(]
                  |              BinaryOperation
                  |                VarReference
                  |                  Leaf[a]
                  |                Leaf[+]
                  |                IntLiteral
                  |                  Leaf[1]
                  |              Leaf[)]
                  |            Leaf[+]
                  |            IntLiteral
                  |              Leaf[1]
                  |    Leaf[<EOF>]
                  |""".trimMargin("|"),
                parseTreeStr)
    }
}