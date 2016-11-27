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
                AssignmentStatement("a", TypeConversionExpression(IntLiteralExpression("7"), IntType()))))
        assertEquals(expectedAst, ast)
    }

    @Test fun mapCastDecimal() {
        val code = "a = 7 as Decimal"
        val ast = SandyParser.parse(code).root!!.toAst()
        val expectedAst = SandyFile(listOf(
                AssignmentStatement("a", TypeConversionExpression(IntLiteralExpression("7"), DecimalType()))))
        assertEquals(expectedAst, ast)
    }

    @Test fun mapPrint() {
        val code = "print(a)"
        val ast = SandyParser.parse(code).root!!.toAst()
        val expectedAst = SandyFile(listOf(PrintStatement(VarReferenceExpression("a"))))
        assertEquals(expectedAst, ast)
    }

    @Test fun mapSimpleFileWithPositions() {
        val code = """var a = 1 + 2
                     |a = 7 * (2 / 3)""".trimMargin("|")
        val ast = SandyParser.parse(code).root!!.toAst(considerPosition = true)
        val expectedAst = SandyFile(listOf(
                VarDeclarationStatement("a",
                        SumExpression(
                                IntLiteralExpression("1", position(1, 8, 1, 9)),
                                IntLiteralExpression("2", position(1, 12, 1, 13)),
                                position(1, 8, 1, 13)),
                        position(1, 0, 1, 13)),
                AssignmentStatement("a",
                        MultiplicationExpression(
                                IntLiteralExpression("7", position(2, 4, 2, 5)),
                                DivisionExpression(
                                        IntLiteralExpression("2", position(2, 9, 2, 10)),
                                        IntLiteralExpression("3", position(2, 13, 2, 14)),
                                        position(2, 9, 2, 14)),
                                position(2, 4, 2, 15)),
                        position(2, 0, 2, 15))),
                position(1, 0, 2, 15))
        assertEquals(expectedAst, ast)
    }
}
