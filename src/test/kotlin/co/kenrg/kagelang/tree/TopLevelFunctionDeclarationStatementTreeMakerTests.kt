package co.kenrg.kagelang.tree

import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.kageFileFromCode
import co.kenrg.kagelang.kageFileFromLines
import co.kenrg.kagelang.model.FnParameter
import co.kenrg.kagelang.model.TypeIdentifier
import co.kenrg.kagelang.tree.KGTree.KGBinary
import co.kenrg.kagelang.tree.KGTree.KGFnDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TopLevelFunctionDeclarationStatementTreeMakerTests {

    @Test fun testParseFnDeclaration_bodyIsLiteral() {
        val kageFile = kageFileFromCode("fn returnOne() = 1")
        val expected = kageFileFromLines(KGFnDeclaration("returnOne", intLiteral(1)))
        assertEquals(expected, kageFile)
    }

    @Test fun testParseFnDeclaration_bodyIsBinaryExpression() {
        val kageFile = kageFileFromCode("fn returnSum() = 1 + 2")
        val expected = kageFileFromLines(KGFnDeclaration("returnSum", KGBinary(intLiteral(1), "+", intLiteral(2))))
        assertEquals(expected, kageFile)
    }

    @Test fun testParseFnDeclaration_returnTypeAnnotation() {
        val kageFile = kageFileFromCode("fn returnSum(): Int = 1 + 2")
        val expected = kageFileFromLines(KGFnDeclaration("returnSum", KGBinary(intLiteral(1), "+", intLiteral(2)), retTypeAnnotation = TypeIdentifier("Int")))
        assertEquals(expected, kageFile)
    }

    @Test fun testParseFnDeclaration_oneParamWithTypeAnnotation_returnTypeAnnotation() {
        val kageFile = kageFileFromCode("fn returnSum(a: Int): Int = 1 + 2")
        val expected = kageFileFromLines(
                KGFnDeclaration(
                        "returnSum",
                        KGBinary(intLiteral(1), "+", intLiteral(2)),
                        listOf(FnParameter("a", TypeIdentifier("Int"))),
                        TypeIdentifier("Int")
                )
        )
        assertEquals(expected, kageFile)
    }

    @Test fun testParseFnDeclaration_manyParamsWithTypeAnnotations_returnTypeAnnotation() {
        val kageFile = kageFileFromCode("fn returnSum(a: Int, b: String, c: Bool): Int = 1 + 2")
        val expected = kageFileFromLines(
                KGFnDeclaration(
                        "returnSum",
                        KGBinary(intLiteral(1), "+", intLiteral(2)),
                        listOf(
                                FnParameter("a", TypeIdentifier("Int")),
                                FnParameter("b", TypeIdentifier("String")),
                                FnParameter("c", TypeIdentifier("Bool"))
                        ),
                        TypeIdentifier("Int")
                )
        )
        assertEquals(expected, kageFile)
    }
}