package co.kenrg.sandylang

import co.kenrg.sandylang.helper.toParseTree
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.Test
import kotlin.test.assertEquals

class SandyParserTests {
    fun lexerForResource(resourceName: String) =
            SandyLexer(ANTLRInputStream(this.javaClass.getResourceAsStream("/$resourceName")))

    fun parseResource(resourceName: String): SandyParser.SandyFileContext =
            SandyParser(CommonTokenStream(lexerForResource(resourceName))).sandyFile()

    @Test fun parsesFileSingleVarDeclaration() {
        val parseTreeStr = toParseTree(parseResource("single-var-declaration.sandy")).multiLineString()
        assertEquals(
"""SandyFile
  Line
    VarDeclarationStatement
      VarDeclarationStatement
        Leaf[var]
        AssignmentStatement
          Leaf[a]
          Leaf[=]
          IntLiteralExpression
            Leaf[1]
    Leaf[<EOF>]
""",
                parseTreeStr)
    }

    @Test fun parsesFileMultipleVarDeclaration() {
        val parseTreeStr = toParseTree(parseResource("multiple-var-declaration.sandy")).multiLineString()
        assertEquals(
                """SandyFile
  Line
    VarDeclarationStatement
      VarDeclarationStatement
        Leaf[var]
        AssignmentStatement
          Leaf[a]
          Leaf[=]
          IntLiteralExpression
            Leaf[10]
  Line
    VarDeclarationStatement
      VarDeclarationStatement
        Leaf[var]
        AssignmentStatement
          Leaf[b]
          Leaf[=]
          IntLiteralExpression
            Leaf[5]
  Line
    VarDeclarationStatement
      VarDeclarationStatement
        Leaf[var]
        AssignmentStatement
          Leaf[c]
          Leaf[=]
          BinaryOperation
            VarReferenceExpression
              Leaf[a]
            Leaf[/]
            VarReferenceExpression
              Leaf[b]
    Leaf[<EOF>]
""",
                parseTreeStr)
    }

    @Test fun parsesFileSingleAdditionAssignment() {
        val parseTreeStr = toParseTree(parseResource("single-addition-assignment.sandy")).multiLineString()
        assertEquals(
                """SandyFile
  Line
    AssignmentStatement
      AssignmentStatement
        Leaf[a]
        Leaf[=]
        BinaryOperation
          IntLiteralExpression
            Leaf[1]
          Leaf[+]
          IntLiteralExpression
            Leaf[2]
    Leaf[<EOF>]
""",
                parseTreeStr)
    }

    @Test fun parsesFileMultipleVarDeclarationWithParens() {
        val parseTreeStr = toParseTree(parseResource("multiple-var-declaration-with-parens.sandy")).multiLineString()
        assertEquals(
                """SandyFile
  Line
    VarDeclarationStatement
      VarDeclarationStatement
        Leaf[var]
        AssignmentStatement
          Leaf[a]
          Leaf[=]
          ParenExpression
            Leaf[(]
            BinaryOperation
              IntLiteralExpression
                Leaf[1]
              Leaf[+]
              IntLiteralExpression
                Leaf[2]
            Leaf[)]
  Line
    VarDeclarationStatement
      VarDeclarationStatement
        Leaf[var]
        AssignmentStatement
          Leaf[b]
          Leaf[=]
          BinaryOperation
            ParenExpression
              Leaf[(]
              BinaryOperation
                VarReferenceExpression
                  Leaf[a]
                Leaf[+]
                IntLiteralExpression
                  Leaf[1]
              Leaf[)]
            Leaf[+]
            IntLiteralExpression
              Leaf[1]
    Leaf[<EOF>]
""",
                parseTreeStr)
    }
}