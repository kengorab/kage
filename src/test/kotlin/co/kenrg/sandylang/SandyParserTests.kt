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
      VarDeclaration
        Leaf[var]
        Assignment
          Leaf[a]
          Leaf[=]
          IntLiteral
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
      VarDeclaration
        Leaf[var]
        Assignment
          Leaf[a]
          Leaf[=]
          IntLiteral
            Leaf[10]
  Line
    VarDeclarationStatement
      VarDeclaration
        Leaf[var]
        Assignment
          Leaf[b]
          Leaf[=]
          IntLiteral
            Leaf[5]
  Line
    VarDeclarationStatement
      VarDeclaration
        Leaf[var]
        Assignment
          Leaf[c]
          Leaf[=]
          BinaryOperation
            VarReference
              Leaf[a]
            Leaf[/]
            VarReference
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
      Assignment
        Leaf[a]
        Leaf[=]
        BinaryOperation
          IntLiteral
            Leaf[1]
          Leaf[+]
          IntLiteral
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
      VarDeclaration
        Leaf[var]
        Assignment
          Leaf[a]
          Leaf[=]
          ParenExpression
            Leaf[(]
            BinaryOperation
              IntLiteral
                Leaf[1]
              Leaf[+]
              IntLiteral
                Leaf[2]
            Leaf[)]
  Line
    VarDeclarationStatement
      VarDeclaration
        Leaf[var]
        Assignment
          Leaf[b]
          Leaf[=]
          BinaryOperation
            ParenExpression
              Leaf[(]
              BinaryOperation
                VarReference
                  Leaf[a]
                Leaf[+]
                IntLiteral
                  Leaf[1]
              Leaf[)]
            Leaf[+]
            IntLiteral
              Leaf[1]
    Leaf[<EOF>]
""",
                parseTreeStr)
    }
}