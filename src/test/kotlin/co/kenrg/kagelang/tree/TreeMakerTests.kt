package co.kenrg.kagelang.tree

import co.kenrg.kagelang.codegen.*
import co.kenrg.kagelang.parser.KageAntlrParserFacade
import co.kenrg.kagelang.parser.toStream
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGTypeTag
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

/*
    Unless specifically (in the title of the test) testing the KGPrint statement, the usage of printing
    in these tests is due to the fact that it is the simplest top-level statement available. In order to
    test the rest of the expressions get parsed and mapped to trees correctly, we need to wrap it at the
    top level in a print. This should be completely transparent in the tests though.
 */
class TreeMakerTests {

    @Nested
    inner class PrintStatement() {
        @TestFactory
        fun testParseAndTransformPrintStatement_literals(): List<DynamicTest> {
            data class Case(val repr: String, val statement: KGTree.KGStatement)

            return listOf(
                    Case("print(1)", KGPrint(intLiteral(1))),
                    Case("print(3.14)", KGPrint(decLiteral(3.14))),
                    Case("print(true)", KGPrint(trueLiteral())),
                    Case("print(false)", KGPrint(falseLiteral())),
                    Case("print(\"hello world\")", KGPrint(stringLiteral("hello world")))
            ).map { testCase ->
                val (repr, expr) = testCase

                dynamicTest("The statement `$repr` should be correctly mapped to its tree structure") {
                    val kageFile = kageFileFromCode(repr)
                    val expected = kageFileFromStatements(expr)
                    assertEquals(expected, kageFile)
                }
            }
        }
    }

    @Nested
    inner class TopLevelValDeclarationStatement() {

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
                    val expected = kageFileFromStatements(expr)
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
                    val expected = kageFileFromStatements(expr)
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
                    val expected = kageFileFromStatements(expr)
                    assertEquals(expected, kageFile)
                }
            }
        }
    }


    @Nested
    inner class BinaryExpressions() {
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
                    Case("1.1 * 1.3", KGBinary(decLiteral(1.1), "*", decLiteral(1.3))),
                    Case("1.3 / 1.1", KGBinary(decLiteral(1.3), "/", decLiteral(1.1)))
            ).map { testCase ->
                val (repr, expr) = testCase

                dynamicTest("The expression `$repr` should be correctly mapped to its tree structure") {
                    val kageFile = kageFileFromCode("print($repr)")
                    val expected = kageFileFromStatements(KGPrint(expr))
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
                    val kageFile = kageFileFromCode("print($repr)")
                    val expected = kageFileFromStatements(KGPrint(expr))
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
                    val kageFile = kageFileFromCode("print($repr)")
                    val expected = kageFileFromStatements(KGPrint(expr))
                    assertEquals(expected, kageFile)
                }
            }
        }

        @Test fun testStringConcatenation() {
            val kageFile = kageFileFromCode("val a = \"hello \" ++ \"world\"")
            val expected = kageFileFromStatements(KGValDeclaration("a", KGBinary(stringLiteral("hello "), "++", stringLiteral("world"))))
            assertEquals(expected, kageFile)
        }

        @Test fun testMultipleStringConcatenations() {
            val kageFile = kageFileFromCode("val a = \"hello\" ++ \" \" ++ \"world\"")
            val expected = kageFileFromStatements(
                    KGValDeclaration(
                            "a",
                            KGBinary(KGBinary(stringLiteral("hello"), "++", stringLiteral(" ")), "++", stringLiteral("world")))
            )
            assertEquals(expected, kageFile)
        }
    }

    private fun kageFileFromStatements(vararg statements: KGTree.KGStatement, bindings: Map<String, KGTypeTag> = mapOf()) =
            KGFile(statements.toList(), bindings)

    private fun kageFileFromCode(code: String, considerPosition: Boolean = false): KGFile {
        val antlrParsingResult = KageAntlrParserFacade.parse(code.toStream())
        val antlrRoot = antlrParsingResult.root!!
        return TreeMaker(considerPosition).toKageFile(antlrRoot)
    }
}