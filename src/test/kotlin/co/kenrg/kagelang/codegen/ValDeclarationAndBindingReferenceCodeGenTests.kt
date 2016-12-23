package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.*

/*
    Because of the way these codegen tests are set up, it's difficult to test val declaration on its own. The codegen
    tests run by wrapping the expressions in `print` statements, and then by generating and executing the resulting class
    and verifying the output. So this test suite will test val declarations AND binding references.
 */
class ValDeclarationAndBindingReferenceCodeGenTests : BaseTest() {

    @TestFactory
    @DisplayName("Declaring a binding as expression of literals, then printing the binding")
    fun testStoringBindingAndReferencingBindingInPrint(): List<DynamicTest> {
        data class Case(val repr: String, val expr: KGExpression, val expectedOutput: String)

        return listOf(
                Case("val a = 1", intLiteral(1), "1"),
                Case("val a = 3.14", decLiteral(3.14), "3.14"),
                Case("val a = true", trueLiteral, "true"),

                Case("val a = 1 + 3", KGBinary(intLiteral(1), "+", intLiteral(3)), "4"),
                Case("val a = 1.4 - 3.0", KGBinary(decLiteral(1.4), "-", decLiteral(3.0)), "-1.6"),
                Case("val a = true || false", KGBinary(trueLiteral, "||", falseLiteral), "true")
        ).map { testCase ->
            val (repr, expr, expected) = testCase

            dynamicTest("If `$repr`, printing `a` should output `$expected`") {
                val file = KGFile(
                        statements = listOf(
                                KGValDeclaration("a", expr),
                                KGPrint(KGBindingReference("a"))
                        ),
                        bindings = HashMap()
                )

                compileAndExecuteFileAnd(file) { output ->
                    assertEquals(expected, output)
                }
            }
        }
    }

    @TestFactory
    @DisplayName("Declaring a binding as an expression of other bindings, then printing the binding")
    fun testPrintingBinaryExpressionOfBindings(): List<DynamicTest> {
        data class Case(val repr: String, val exprA: KGExpression, val exprB: KGExpression, val op: String, val expectedOutput: String)

        return listOf(
                Case("val a = 1, val b = 2", intLiteral(1), intLiteral(2), "+", "3"),
                Case("val a = 1, val b = 2.2", intLiteral(1), decLiteral(2.2), "+", "3.2"),
                Case("val a = 1.2, val b = 2", decLiteral(1.2), intLiteral(2), "+", "3.2"),
                Case("val a = 1.2, val b = 2.0", decLiteral(1.2), decLiteral(2.0), "+", "3.2"),

                Case("val a = true, val b = false", trueLiteral, falseLiteral, "&&", "false"),
                Case("val a = true, val b = false", trueLiteral, falseLiteral, "||", "true"),
                Case("val a = true, val b = false || a", trueLiteral, KGBinary(falseLiteral, "||", KGBindingReference("a")), "||", "true")
        ).map { testCase ->
            val (repr, exprA, exprB, op, expectedOutput) = testCase

            dynamicTest("If `$repr`, printing `a $op b` should output `$expectedOutput`") {
                val file = KGFile(
                        statements = listOf(
                                KGValDeclaration("a", exprA),
                                KGValDeclaration("b", exprB),
                                KGPrint(KGBinary(KGBindingReference("a"), op, KGBindingReference("b")))
                        ),
                        bindings = HashMap()
                )

                compileAndExecuteFileAnd(file) { output ->
                    assertEquals(expectedOutput, output)
                }
            }
        }
    }
}

