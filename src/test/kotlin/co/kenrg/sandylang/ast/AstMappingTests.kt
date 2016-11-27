package co.kenrg.sandylang.ast;

import co.kenrg.sandylang.ast.extensions.toAst
import co.kenrg.sandylang.parser.SandyParser
import org.junit.Test
import kotlin.test.assertEquals

class AstMappingTests {
    @Test fun mapSimpleFile() {
        val code = """var a = 1 + 2
                     |a = 7 * (2 / 3)"""
                .trimMargin("|")
        val ast = SandyParser.parse(code).root!!.toAst()
        val expectedAst = SandyFile(listOf(
                VarDeclarationStatement("a", SumExpression(IntLiteralExpression("1"), IntLiteralExpression("2"))),
                AssignmentStatement("a", MultiplicationExpression(
                        IntLiteralExpression("7"),
                        DivisionExpression(
                                IntLiteralExpression("2"),
                                IntLiteralExpression("3"))))))
        assertEquals(expectedAst, ast)
    }

    @Test fun mapCastInt() {
        val code = "a = 7 as Int"
        val ast = SandyParser.parse(code).root!!.toAst()
        val expectedAst = SandyFile(listOf(
                AssignmentStatement("a", TypeConversionExpression(IntLiteralExpression("7"), IntType))))
        assertEquals(expectedAst, ast)
    }

    @Test fun mapCastDecimal() {
        val code = "a = 7 as Decimal"
        val ast = SandyParser.parse(code).root!!.toAst()
        val expectedAst = SandyFile(listOf(
                AssignmentStatement("a", TypeConversionExpression(IntLiteralExpression("7"), DecimalType))))
        assertEquals(expectedAst, ast)
    }

    @Test fun mapPrint() {
        val code = "print(a)"
        val ast = SandyParser.parse(code).root!!.toAst()
        val expectedAst = SandyFile(listOf(PrintStatement(VarReferenceExpression("a"))))
        assertEquals(expectedAst, ast)
    }
}
