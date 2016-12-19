package co.kenrg.kagelang.ast

import co.kenrg.kagelang.helper.parseWithoutPosition
import co.kenrg.kagelang.parser.KageParserFacade
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class AstMappingTests {
    @Test fun mapSimpleFile() {
        val code = """var a = 1 + 2
                     |a = 7 * (2 / 3)"""
                .trimMargin("|")
        val ast = KageParserFacade.parseWithoutPosition(code).root
        val expectedAst = KageFile(listOf(
                VarDeclarationStatement("a", SumExpression(IntLiteralExpression("1"), IntLiteralExpression("2"))),
                AssignmentStatement("a", MultiplicationExpression(
                        IntLiteralExpression("7"),
                        DivisionExpression(
                                IntLiteralExpression("2"),
                                IntLiteralExpression("3"))))))
        assertEquals(expectedAst, ast)
    }

    @Test fun mapPrint() {
        val code = "print(a)"
        val ast = KageParserFacade.parseWithoutPosition(code).root
        val expectedAst = KageFile(listOf(PrintStatement(VarReferenceExpression("a"))))
        assertEquals(expectedAst, ast)
    }

    @Test fun mapSimpleFileWithPositions() {
        val code = """var a = 1 + 2
                     |a = 7 * (2 / 3)""".trimMargin("|")
        val ast = KageParserFacade.parse(code).root
        val expectedAst = KageFile(listOf(
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
                position(1, 0, 2, 20))  // Extra characters for '<EOF>'
        assertEquals(expectedAst, ast)
    }

    @Test fun transformsVariableNamesToUppercase() {
        val code = """var a = 1
                     |var b = a""".trimMargin("|")
        val expectedPreTransformedAst = KageFile(listOf(
                VarDeclarationStatement("a", IntLiteralExpression("1")),
                VarDeclarationStatement("b", VarReferenceExpression("a"))
        ))
        val preTransformedAst = KageParserFacade.parseWithoutPosition(code).root
        assertEquals(expectedPreTransformedAst, preTransformedAst)

        val expectedTransformedAst = KageFile(listOf(
                VarDeclarationStatement("A", IntLiteralExpression("1")),
                VarDeclarationStatement("B", VarReferenceExpression("A"))
        ))
        val transformedAst = preTransformedAst?.transform {
            when (it) {
                is VarDeclarationStatement -> VarDeclarationStatement(it.varName.toUpperCase(), it.value)
                is VarReferenceExpression -> VarReferenceExpression(it.varName.toUpperCase())
                else -> it
            }
        }
        assertEquals(expectedTransformedAst, transformedAst)
    }
}
