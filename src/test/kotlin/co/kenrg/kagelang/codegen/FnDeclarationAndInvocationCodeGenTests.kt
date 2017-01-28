package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest

class FnDeclarationAndInvocationCodeGenTests : BaseTest() {

    @Nested
    inner class FunctionsWithParameters {
        @Test fun testFnDeclarationInvocationWithParameter() {
            val code = """
              fn abc(a: Int) = print(a + 2)
              fn main() = abc(1)
            """
            val file = kageFileFromCode(code)

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("3", output)
            }
        }

        @Test fun testFnDeclarationInvocationWithMultipleParameters() {
            val code = """
              fn abc(a: Int, b: Int): Int = a + b
              fn main() = print(abc(1, 3))
            """
            val file = kageFileFromCode(code)

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("4", output)
            }
        }

        @Test fun testFnDeclarationInvocationWithParams_notAllParamsUsed() {
            val code = """
              fn abc(a: Int, b: Int): Int = a + 1
              fn main() = print(abc(1, 1000))
            """
            val file = kageFileFromCode(code)

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("2", output)
            }
        }

        @Test fun testFnDeclarationInvocationWithParam_paramIsExpression() {
            val code = """
              fn exclaim(str: String): String = str ++ "!"
              fn main() = print(exclaim("Hello" ++ " World"))
            """
            val file = kageFileFromCode(code)

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("Hello World!", output)
            }
        }

        @Test fun testFnDeclarationInvocationWithParam_paramIsInvocation() {
            val code = """
              fn exclaim(str: String): String = str ++ "!"
              fn main() = print(exclaim(exclaim("Hello World")))
            """
            val file = kageFileFromCode(code)

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("Hello World!!", output)
            }
        }

        @Test fun testFnDeclarationInvocationWithParams_paramShadowsExtVal_usesParam() {
            val code = """
              val a = "Static val"
              fn exclaim(a: String): String = a ++ "!"
              fn main() = print(exclaim("Hello"))
            """
            val file = kageFileFromCode(code)

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("Hello!", output)
            }
        }

        @Test fun testFnDeclarationInvocation_polymorphism_sameNumberOfParams() {
            val code = """
              fn polyFunc(a: String, b: Int): String = "String, Int\n"
              fn polyFunc(a: String, b: String): String = "String, String\n"
              fn main() = print(polyFunc("Hello", 1) ++ polyFunc("Hello", "World"))
            """
            val file = kageFileFromCode(code)

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("String, Int\nString, String\n", output)
            }
        }

        @Test fun testFnDeclarationInvocation_polymorphism_differentNumberOfParams() {
            val code = """
              fn polyFunc(a: String): String = "String\n"
              fn polyFunc(a: String, b: String): String = "String, String\n"
              fn main() = print(polyFunc("Hello") ++ polyFunc("Hello", "World"))
            """
            val file = kageFileFromCode(code)

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("String\nString, String\n", output)
            }
        }
    }

    @TestFactory
    @DisplayName("Declaring a no-params function whose body is an expression of literals, then printing the invocation")
    fun testDeclaringFunctionAndInvokingFunctionInPrint(): List<DynamicTest> {
        val fnName = "abc"
        return listOf(
                Pair("fn $fnName() = 1", "1"),
                Pair("fn $fnName() = 3.14", "3.14"),
                Pair("fn $fnName() = true", "true"),
                Pair("fn $fnName() = \"hello world!\"", "hello world!"),

                Pair("fn $fnName() = 1 + 3", "4"),
                Pair("fn $fnName() = 1.4 - 3.0", "-1.6"),
                Pair("fn $fnName() = true || false", "true")
        ).map { testCase ->
            val (repr, expected) = testCase

            dynamicTest("If `$repr`, printing `$fnName()` should output `$expected`") {
                val code = """
                  $repr
                  fn main() = print($fnName())
                """
                val file = kageFileFromCode(code)

                compileAndExecuteFileAnd(file) { output ->
                    assertEquals(expected, output)
                }
            }
        }
    }

    @TestFactory
    @DisplayName("Printing binary expressions of function invocations")
    fun testPrintingBinaryExpressionOfFunctionInvocations(): List<DynamicTest> {
        data class Case(val fn1: String, val fn2: String, val op: String, val expectedOutput: String)

        val fnName1 = "abc"
        val fnName2 = "xyz"
        return listOf(
                Case("fn $fnName1() = 1", "fn $fnName2() = 2", "+", "3"),
                Case("fn $fnName1() = 1", "fn $fnName2() = 2.2", "+", "3.2"),
                Case("fn $fnName1() = 1.2", "fn $fnName2() = 2", "+", "3.2"),
                Case("fn $fnName1() = 1.2", "fn $fnName2() = 2.0", "+", "3.2"),

                Case("fn $fnName1() = true", "fn $fnName2() = false", "&&", "false"),
                Case("fn $fnName1() = true", "fn $fnName2() = false", "||", "true"),
                Case("fn $fnName1() = true", "fn $fnName2() = false || $fnName1()", "||", "true"),

                Case("fn $fnName1() = \"Hello \"", "fn $fnName2() = \"World!\"", "++", "Hello World!")
        ).map { testCase ->
            val (fn1, fn2, op, expectedOutput) = testCase

            dynamicTest("If `$fn1` and `$fn2`, printing `$fnName1() $op $fnName2()` should output `$expectedOutput`") {
                val code = """
                  $fn1
                  $fn2
                  fn main() = print($fnName1() $op $fnName2())
                """
                val file = kageFileFromCode(code)

                compileAndExecuteFileAnd(file) { output ->
                    assertEquals(expectedOutput, output)
                }
            }
        }
    }
}
