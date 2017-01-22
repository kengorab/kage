package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.model.FnParameter
import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import java.util.*

class FnDeclarationAndInvocationCodeGenTests : BaseTest() {

    @Nested
    inner class FunctionsWithParameters {
        @Test fun testFnDeclarationInvocationWithParameter() {
            // fn abc(a: Int) = print(a + 2)
            // fn main(...) = abc(1)
            val file = KGFile(
                    statements = listOf(
                            KGFnDeclaration("abc", KGPrint(KGBinary(KGBindingReference("a"), "+", intLiteral(2))), listOf(FnParameter("a", "Int"))),
                            wrapInMainMethod(KGInvocation(KGBindingReference("abc"), listOf(intLiteral(1))))
                    ),
                    bindings = HashMap()
            )

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("3", output)
            }
        }

        @Test fun testFnDeclarationInvocationWithMultipleParameters() {
            // fn abc(a: Int, b: Int): Int = a + b
            // fn main(...) = print(abc(1, 3))
            val file = KGFile(
                    statements = listOf(
                            KGFnDeclaration(
                                    "abc",
                                    KGBinary(KGBindingReference("a"), "+", KGBindingReference("b")),
                                    listOf(FnParameter("a", "Int"), FnParameter("b", "Int")),
                                    "Int"
                            ),
                            wrapInMainMethod(KGPrint(KGInvocation(
                                    KGBindingReference("abc"),
                                    listOf(
                                            intLiteral(1),
                                            intLiteral(3)
                                    )))
                            )
                    ),
                    bindings = HashMap()
            )

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("4", output)
            }
        }

        @Test fun testFnDeclarationInvocationWithParams_notAllParamsUsed() {
            // fn abc(a: Int, b: Int): Int = a + 1
            // fn main(...) = print(abc(1, 1000))
            val file = KGFile(
                    statements = listOf(
                            KGFnDeclaration(
                                    "abc",
                                    KGBinary(KGBindingReference("a"), "+", intLiteral(1)),
                                    listOf(FnParameter("a", "Int"), FnParameter("b", "Int")),
                                    "Int"
                            ),
                            wrapInMainMethod(KGPrint(KGInvocation(
                                    KGBindingReference("abc"),
                                    listOf(
                                            intLiteral(1),
                                            intLiteral(1000)
                                    )))
                            )
                    ),
                    bindings = HashMap()
            )

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("2", output)
            }
        }

        @Test fun testFnDeclarationInvocationWithParam_paramIsExpression() {
            // fn exclaim(str: String): String = str ++ "!"
            // fn main(...) = print(exclaim("Hello" ++ " World"))
            val file = KGFile(
                    statements = listOf(
                            KGFnDeclaration(
                                    "exclaim",
                                    KGBinary(KGBindingReference("str"), "++", stringLiteral("!")),
                                    listOf(FnParameter("str", "String")),
                                    "String"
                            ),
                            wrapInMainMethod(KGPrint(KGInvocation(
                                    KGBindingReference("exclaim"),
                                    listOf(
                                            KGBinary(stringLiteral("Hello"), "++", stringLiteral(" World"))
                                    )))
                            )
                    ),
                    bindings = HashMap()
            )

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("Hello World!", output)
            }
        }

        @Test fun testFnDeclarationInvocationWithParam_paramIsInvocation() {
            // fn exclaim(str: String): String = str ++ "!"
            // fn main(...) = print(exclaim(exclaim("Hello World")))
            val file = KGFile(
                    statements = listOf(
                            KGFnDeclaration(
                                    "exclaim",
                                    KGBinary(KGBindingReference("str"), "++", stringLiteral("!")),
                                    listOf(FnParameter("str", "String")),
                                    "String"
                            ),
                            wrapInMainMethod(KGPrint(
                                    KGInvocation(
                                            KGBindingReference("exclaim"),
                                            listOf(
                                                    KGInvocation(
                                                            KGBindingReference("exclaim"),
                                                            listOf(stringLiteral("Hello World"))
                                                    )
                                            )
                                    ))
                            )
                    ),
                    bindings = HashMap()
            )

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("Hello World!!", output)
            }
        }

        @Test fun testFnDeclarationInvocationWithParams_paramShadowsExtVal_usesParam() {
            // val a = "Static val"
            // fn exclaim(a: String): String = a ++ "!"
            // fn main(...) = print(exclaim("Hello"))
            val file = KGFile(
                    statements = listOf(
                            KGValDeclaration("a", stringLiteral("Static val")),
                            KGFnDeclaration(
                                    "exclaim",
                                    KGBinary(KGBindingReference("a"), "++", stringLiteral("!")),
                                    listOf(FnParameter("a", "String")),
                                    "String"
                            ),
                            wrapInMainMethod(KGPrint(KGInvocation(
                                    KGBindingReference("exclaim"),
                                    listOf(stringLiteral("Hello"))))
                            )
                    ),
                    bindings = HashMap()
            )

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("Hello!", output)
            }
        }

        @Test fun testFnDeclarationInvocation_polymorphism_sameNumberOfParams() {
            // fn polyFunc(a: String, b: Int): String = "String, Int\n"
            // fn polyFunc(a: String, b: String): String = "String, String\n"
            // fn main(...) = print(exclaim("Hello", 1))
            val file = KGFile(
                    statements = listOf(
                            KGFnDeclaration(
                                    "exclaim",
                                    stringLiteral("String, Int\n"),
                                    listOf(FnParameter("a", "String"), FnParameter("b", "Int")),
                                    "String"
                            ),
                            KGFnDeclaration(
                                    "exclaim",
                                    stringLiteral("String, String\n"),
                                    listOf(FnParameter("a", "String"), FnParameter("b", "String")),
                                    "String"
                            ),
                            wrapInMainMethod(KGPrint(KGBinary(
                                    KGInvocation(
                                            KGBindingReference("exclaim"),
                                            listOf(stringLiteral("Hello"), intLiteral(1))),
                                    "++",
                                    KGInvocation(
                                            KGBindingReference("exclaim"),
                                            listOf(stringLiteral("Hello"), stringLiteral("World")))
                            )))
                    ),
                    bindings = HashMap()
            )

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("String, Int\nString, String\n", output)
            }
        }

        @Test fun testFnDeclarationInvocation_polymorphism_differentNumberOfParams() {
            // fn polyFunc(a: String): String = "String\n"
            // fn polyFunc(a: String, b: String): String = "String, String\n"
            // fn main(...) = print(exclaim("Hello", 1))
            val file = KGFile(
                    statements = listOf(
                            KGFnDeclaration(
                                    "exclaim",
                                    stringLiteral("String\n"),
                                    listOf(FnParameter("a", "String")),
                                    "String"
                            ),
                            KGFnDeclaration(
                                    "exclaim",
                                    stringLiteral("String, String\n"),
                                    listOf(FnParameter("a", "String"), FnParameter("b", "String")),
                                    "String"
                            ),
                            wrapInMainMethod(KGPrint(KGBinary(
                                    KGInvocation(
                                            KGBindingReference("exclaim"),
                                            listOf(stringLiteral("Hello"))),
                                    "++",
                                    KGInvocation(
                                            KGBindingReference("exclaim"),
                                            listOf(stringLiteral("Hello"), stringLiteral("World")))
                            )))
                    ),
                    bindings = HashMap()
            )

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("String\nString, String\n", output)
            }
        }
    }

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
                                wrapInMainMethod(KGPrint(KGInvocation(KGBindingReference("abc"))))
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
                                wrapInMainMethod(KGPrint(
                                        KGBinary(KGInvocation(KGBindingReference("abc")), op, KGInvocation(KGBindingReference("xyz")))
                                ))
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

