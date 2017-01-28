package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class ValDeclarationAndBindingReferenceCodeGenTests : BaseTest() {

    @TestFactory
    @DisplayName("Declaring a binding as expression of literals, then printing the binding")
    fun testStoringBindingAndReferencingBindingInPrint(): List<DynamicTest> {
        return listOf(
                Pair("val a = 1", "1"),
                Pair("val a = 3.14", "3.14"),
                Pair("val a = true", "true"),
                Pair("val a = \"hello world!\"", "hello world!"),

                Pair("val a = 1 + 3", "4"),
                Pair("val a = 1.4 - 3.0", "-1.6"),
                Pair("val a = true || false", "true")
        ).map { testCase ->
            val (valDecl, expected) = testCase

            dynamicTest("If `$valDecl`, printing `a` should output `$expected`") {
                val code = """
                  $valDecl
                  fn main() = print(a)
                """
                val file = kageFileFromCode(code)

                compileAndExecuteFileAnd(file) { output ->
                    assertEquals(expected, output)
                }
            }
        }
    }

    // Type annotations should have no factor into codegen, since all of the work is done during typechecking. But,
    // it's good to have just one test to verify that it doesn't mess anything up unexpectedly.
    @Test fun testTypeAnnotationsOnValDeclarations() {
        val code = """
          val a: Int = 1
          fn main() = print(a)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("1", output)
        }
    }

    @TestFactory
    @DisplayName("Declaring a binding as an expression of other bindings, then printing the binding")
    fun testPrintingBinaryExpressionOfBindings(): List<DynamicTest> {
        data class Case(val val1: String, val val2: String, val op: String, val expected: String)

        return listOf(
                Case("val a = 1", "val b = 2", "+", "3"),
                Case("val a = 1", "val b = 2.2", "+", "3.2"),
                Case("val a = 1.2", "val b = 2", "+", "3.2"),
                Case("val a = 1.2", "val b = 2.0", "+", "3.2"),

                Case("val a = true", "val b = false", "&&", "false"),
                Case("val a = true", "val b = false", "||", "true"),
                Case("val a = true", "val b = false || a", "||", "true"),

                Case("val a = \"Hello \"", "val b = \"World!\"", "++", "Hello World!")
        ).map { testCase ->
            val (val1, val2, op, expected) = testCase

            dynamicTest("If `$val1` and `$val2`, printing `a $op b` should output `$expected`") {
                val code = """
                  $val1
                  $val2
                  fn main() = print(a $op b)
                """
                val file = kageFileFromCode(code)

                compileAndExecuteFileAnd(file) { output ->
                    assertEquals(expected, output)
                }
            }
        }
    }
}

