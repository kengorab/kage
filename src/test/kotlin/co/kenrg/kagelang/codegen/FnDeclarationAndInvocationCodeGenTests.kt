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
class FnDeclarationAndInvocationCodeGenTests : BaseTest() {

    @TestFactory
    @DisplayName("Declaring a no-params function whose body is an expression of literals, then printing the invocation")
    fun testDeclaringFunctionAndInvokingFunctionInPrint(): List<DynamicTest> {
        data class Case(val repr: String, val expr: KGExpression, val expectedOutput: String)

        return listOf(
                Case("fn abc() = 1", intLiteral(1), "1"),
                Case("fn abc() = 3.14", decLiteral(3.14), "3.14"),
                Case("fn abc() = true", trueLiteral(), "true"),
                Case("fn abc() = \"hello world!\"", stringLiteral("hello world"), "hello world"),

                Case("fn abc() = 1 + 3", KGBinary(intLiteral(1), "+", intLiteral(3)), "4"),
                Case("fn abc() = 1.4 - 3.0", KGBinary(decLiteral(1.4), "-", decLiteral(3.0)), "-1.6"),
                Case("fn abc() = true || false", KGBinary(trueLiteral(), "||", falseLiteral()), "true")
        ).map { testCase ->
            val (repr, expr, expected) = testCase

            dynamicTest("If `$repr`, printing `abc()` should output `$expected`") {
                val file = KGFile(
                        statements = listOf(
                                KGFnDeclaration("abc", expr),
                                KGPrint(KGInvocation(KGBindingReference("abc")))
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
    @DisplayName("Printing binary expressions of function invocations")
    fun testPrintingBinaryExpressionOfFunctionInvocations(): List<DynamicTest> {
        data class Case(val repr: String, val exprA: KGExpression, val exprB: KGExpression, val op: String, val expectedOutput: String)

        return listOf(
                Case("fn abc() = 1, fn xyz() = 2", intLiteral(1), intLiteral(2), "+", "3"),
                Case("fn abc() = 1, fn xyz() = 2.2", intLiteral(1), decLiteral(2.2), "+", "3.2"),
                Case("fn abc() = 1.2, fn xyz() = 2", decLiteral(1.2), intLiteral(2), "+", "3.2"),
                Case("fn abc() = 1.2, fn xyz() = 2.0", decLiteral(1.2), decLiteral(2.0), "+", "3.2"),

                Case("fn abc() = true, fn xyz() = false", trueLiteral(), falseLiteral(), "&&", "false"),
                Case("fn abc() = true, fn xyz() = false", trueLiteral(), falseLiteral(), "||", "true"),
                Case("fn abc() = true, fn xyz() = false || abc()", trueLiteral(), KGBinary(falseLiteral(), "||", KGInvocation(KGBindingReference("abc"))), "||", "true"),

                Case("fn abc() = \"Hello \", fn xyz() = \"World!\"", stringLiteral("Hello "), stringLiteral("World!"), "++", "Hello World!")
        ).map { testCase ->
            val (repr, exprA, exprB, op, expectedOutput) = testCase

            dynamicTest("If `$repr`, printing `abc() $op xyz()` should output `$expectedOutput`") {
                val file = KGFile(
                        statements = listOf(
                                KGFnDeclaration("abc", exprA),
                                KGFnDeclaration("xyz", exprB),
                                KGPrint(KGBinary(KGInvocation(KGBindingReference("abc")), op, KGInvocation(KGBindingReference("xyz"))))
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

