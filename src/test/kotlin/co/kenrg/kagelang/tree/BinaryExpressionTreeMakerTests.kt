package co.kenrg.kagelang.tree

import co.kenrg.kagelang.codegen.*
import co.kenrg.kagelang.tree.KGTree.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class BinaryExpressionTreeMakerTests {

    @TestFactory
    fun testParseAndTransformSimpleArithmeticBinaryExpressions(): List<DynamicTest> {
        data class Case(val repr: String, val expr: KGTree.KGExpression)

        return listOf(
                Case("1 + 1", KGBinary(intLiteral(1), "+", intLiteral(1))),
                Case("1 - 1", KGBinary(intLiteral(1), "-", intLiteral(1))),
                Case("1.1 + 1.3", KGBinary(decLiteral(1.1), "+", decLiteral(1.3))),
                Case("1.3 - 1.1", KGBinary(decLiteral(1.3), "-", decLiteral(1.1))),
                Case("1 * 1", KGBinary(intLiteral(1), "*", intLiteral(1))),
                Case("1 / 1", KGBinary(intLiteral(1), "/", intLiteral(1))),
                Case("-1 / 2", KGBinary(KGUnary("-", intLiteral(1)), "/", intLiteral(2))),
                Case("1.1 * 1.3", KGBinary(decLiteral(1.1), "*", decLiteral(1.3))),
                Case("1.3 / 1.1", KGBinary(decLiteral(1.3), "/", decLiteral(1.1)))
        ).map { testCase ->
            val (repr, expr) = testCase

            dynamicTest("The expression `$repr` should be correctly mapped to its tree structure") {
                val kageFile = kageFileFromCode(repr)
                val expected = kageFileFromLines(expr)
                assertEquals(expected, kageFile)
            }
        }
    }

    @TestFactory
    fun testParseAndTransformArithmeticOrderOfOperations(): List<DynamicTest> {
        data class Case(val repr: String, val expr: KGTree.KGExpression)

        return listOf(
                Case("1 + 2 * 3", KGBinary(intLiteral(1), "+", KGBinary(intLiteral(2), "*", intLiteral(3)))),
                Case("1 * 2 + 3 * 2", KGBinary(KGBinary(intLiteral(1), "*", intLiteral(2)), "+", KGBinary(intLiteral(3), "*", intLiteral(2)))),
                Case("(1 - -1) * 2", KGBinary(KGParenthesized(KGBinary(intLiteral(1), "-", KGUnary("-", intLiteral(1)))), "*", intLiteral(2)))
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
    fun testParseAndTransformBooleanBinaryExpressions(): List<DynamicTest> {
        data class Case(val repr: String, val expr: KGTree.KGExpression)

        return listOf(
                Case("true && true", KGBinary(trueLiteral(), "&&", trueLiteral())),
                Case("false || false", KGBinary(falseLiteral(), "||", falseLiteral())),
                Case("!true || !false", KGBinary(KGUnary("!", trueLiteral()), "||", KGUnary("!", falseLiteral()))),
                Case("!(true && false)", KGUnary("!", KGParenthesized(KGBinary(trueLiteral(), "&&", falseLiteral()))))
        ).map { testCase ->
            val (repr, expr) = testCase

            dynamicTest("The expression `$repr` should be correctly mapped to its tree structure") {
                val kageFile = kageFileFromCode(repr)
                val expected = kageFileFromLines(expr)
                assertEquals(expected, kageFile)
            }
        }
    }

    @Test fun testStringConcatenation() {
        val kageFile = kageFileFromCode("\"hello \" ++ \"world\"")
        val expected = kageFileFromLines(KGBinary(stringLiteral("hello "), "++", stringLiteral("world")))
        assertEquals(expected, kageFile)
    }

    @Test fun testMultipleStringConcatenations() {
        val kageFile = kageFileFromCode("\"hello\" ++ \" \" ++ \"world\"")
        val expected = kageFileFromLines(KGBinary(KGBinary(stringLiteral("hello"), "++", stringLiteral(" ")), "++", stringLiteral("world")))
        assertEquals(expected, kageFile)
    }

    @TestFactory
    fun testParseAndTransformBooleanComparisons(): List<DynamicTest> {
        data class Case(val repr: String, val expr: KGTree.KGExpression)

        return listOf(">", ">=", "<", "<=").flatMap { op ->
            listOf(
                    Case(
                            "3 $op 1",
                            KGBinary(intLiteral(3), op, intLiteral(1))
                    ),
                    Case(
                            "1 * 3 $op 1 / 2.0",
                            KGBinary(KGBinary(intLiteral(1), "*", intLiteral(3)), op, KGBinary(intLiteral(1), "/", decLiteral(2.0)))
                    ),
                    Case(
                            "3 $op 1 && 3 $op 2",
                            KGBinary(KGBinary(intLiteral(3), op, intLiteral(1)), "&&", KGBinary(intLiteral(3), op, intLiteral(2)))
                    ),
                    Case(
                            "3 $op 1 || 3 $op 2",
                            KGBinary(KGBinary(intLiteral(3), op, intLiteral(1)), "||", KGBinary(intLiteral(3), op, intLiteral(2)))
                    )
            ).map { testCase ->
                val (repr, expr) = testCase

                dynamicTest("`$repr` should be correctly transformed into a tree") {
                    val kageFile = kageFileFromCode(repr)
                    val expected = kageFileFromLines(expr)
                    assertEquals(expected, kageFile)
                }
            }
        }
    }
}