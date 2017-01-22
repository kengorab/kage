package co.kenrg.kagelang.tree

import co.kenrg.kagelang.codegen.*
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class TopLevelValDeclarationStatementTreeMakerTests {

    @Test fun testParseValDeclarationWithTypeAnnotation() {
        val kageFile = kageFileFromCode("val a: Int = 1")
        val expected = kageFileFromLines(KGValDeclaration("a", intLiteral(1), KGType.INT))
        assertEquals(expected, kageFile)
    }

    @Test fun testParseValDeclarationWithoutTypeAnnotation() {
        val kageFile = kageFileFromCode("val a = 1")
        val expected = kageFileFromLines(KGValDeclaration("a", intLiteral(1), null))
        assertEquals(expected, kageFile)
    }

    @TestFactory
    fun testParseAndTransformValDeclarationsToLiterals(): List<DynamicTest> {
        data class Case(val repr: String, val statement: KGTree.KGStatement)

        return listOf(
                Case("val a = 1", KGValDeclaration("a", intLiteral(1))),
                Case("val b = 3.14", KGValDeclaration("b", decLiteral(3.14))),
                Case("val c = true", KGValDeclaration("c", trueLiteral())),
                Case("val d = false", KGValDeclaration("d", falseLiteral())),
                Case("val e = \"hello world\"", KGValDeclaration("e", stringLiteral("hello world")))
        ).map { testCase ->
            val (repr, expr) = testCase

            dynamicTest("The statement `$repr` should be correctly mapped to its tree structure") {
                val kageFile = kageFileFromCode(repr)
                val expected = kageFileFromLines(expr)
                assertEquals(expected, kageFile)
            }
        }
    }

    @TestFactory
    fun testParseAndTransformValDeclarationsToExpressionsOfLiterals(): List<DynamicTest> {
        data class Case(val repr: String, val statement: KGTree.KGStatement)

        return listOf(
                Case("val sumOfInts = 1 + 3", KGValDeclaration("sumOfInts", KGBinary(intLiteral(1), "+", intLiteral(3)))),
                Case("val productOfDecimals = 3.14 * 2.0", KGValDeclaration("productOfDecimals", KGBinary(decLiteral(3.14), "*", decLiteral(2.0)))),
                Case("val conditionalAnd = true && false", KGValDeclaration("conditionalAnd", KGBinary(trueLiteral(), "&&", falseLiteral()))),
                Case("val conditionalOr = false || true", KGValDeclaration("conditionalOr", KGBinary(falseLiteral(), "||", trueLiteral()))),

                Case("val valWithParens = (1 + 2) + 3", KGValDeclaration("valWithParens", KGBinary(KGParenthesized(KGBinary(intLiteral(1), "+", intLiteral(2))), "+", intLiteral(3)))),
                Case("val valWithArithmeticUnary = -3", KGValDeclaration("valWithArithmeticUnary", KGUnary("-", intLiteral(3)))),
                Case("val valWithBooleanUnary = !true", KGValDeclaration("valWithBooleanUnary", KGUnary("!", trueLiteral())))
        ).map { testCase ->
            val (repr, expr) = testCase

            dynamicTest("The statement `$repr` should be correctly mapped to its tree structure") {
                val kageFile = kageFileFromCode(repr)
                val expected = kageFileFromLines(expr)
                assertEquals(expected, kageFile)
            }
        }
    }

    @TestFactory
    fun testParseAndTransformValDeclarationsToBindings(): List<DynamicTest> {
        data class Case(val repr: String, val statement: KGTree.KGStatement)

        return listOf(
                Case("val a = b", KGValDeclaration("a", KGBindingReference("b"))),
                Case("val a = b * 4", KGValDeclaration("a", KGBinary(KGBindingReference("b"), "*", intLiteral(4))))
        ).map { testCase ->
            val (repr, expr) = testCase

            dynamicTest("The statement `$repr` should be correctly mapped to its tree structure") {
                val kageFile = kageFileFromCode(repr)
                val expected = kageFileFromLines(expr)
                assertEquals(expected, kageFile)
            }
        }
    }
}