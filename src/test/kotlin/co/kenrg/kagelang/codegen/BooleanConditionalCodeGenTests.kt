package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory

class BooleanConditionalCodeGenTests : BaseTest() {

    @Nested
    @DisplayName("Conditional AND Expressions (&&)")
    inner class ConditionalAndTests {

        @TestFactory
        @DisplayName("Compiling and executing a print of simple, non-nested conditional AND statements")
        fun testConditionalAnd_simpleNonNested_boolLiteralsOnly(): List<DynamicTest> {
            return listOf(
                    Pair("true && true", "true"),
                    Pair("false && true", "false"),
                    Pair("true && false", "false"),
                    Pair("false && false", "false")
            ).map { testCase ->
                val (expr, expected) = testCase

                dynamicTest("Printing $expr should output $expected") {
                    val code = "fn main() = print($expr)"
                    val file = kageFileFromCode(code)
                    compileAndExecuteFileAnd(file) { output -> assertEquals(expected, output) }
                }
            }
        }

        @TestFactory
        @DisplayName("Compiling and executing a print of simple, nested conditional AND statements")
        fun testConditionalAnd_simpleNestedAndExpressions(): List<DynamicTest> {
            // This obviously doesn't capture every permutation of this, but I'm sure it's enough
            return listOf(
                    Pair("(true && true) && true", "true"),
                    Pair("(true && false) && true", "false"),
                    Pair("(true && true) && false", "false"),
                    Pair("true && (true && true)", "true"),
                    Pair("true && (false && true)", "false"),
                    Pair("false && (true && true)", "false")
            ).map { testCase ->
                val (expr, expected) = testCase

                dynamicTest("Printing $expr should output $expected") {
                    val code = "fn main() = print($expr)"
                    val file = kageFileFromCode(code)
                    compileAndExecuteFileAnd(file) { output -> assertEquals(expected, output) }
                }
            }
        }
    }

    @Nested
    @DisplayName("Conditional OR Expressions (||)")
    inner class ConditionalOrTests {

        @TestFactory
        @DisplayName("Compiling and executing a print of simple, non-nested conditional OR statements")
        fun testConditionalOr_simpleNonNested_boolLiteralsOnly(): List<DynamicTest> {
            return listOf(
                    Pair("true || true", "true"),
                    Pair("false || true", "true"),
                    Pair("true || false", "true"),
                    Pair("false || false", "false")
            ).map { testCase ->
                val (expr, expected) = testCase

                dynamicTest("Printing $expr should output $expected") {
                    val code = "fn main() = print($expr)"
                    val file = kageFileFromCode(code)
                    compileAndExecuteFileAnd(file) { output -> assertEquals(expected, output) }
                }
            }
        }

        @TestFactory
        @DisplayName("Compiling and executing a print of simple, nested conditional OR statements")
        fun testConditionalOr_simpleNestedOrExpressions(): List<DynamicTest> {
            // This obviously doesn't capture every permutation of this, but I'm sure it's enough
            return listOf(
                    Pair("(true || true) || true", "true"),
                    Pair("(true || false) || true", "true"),
                    Pair("(true || true) || false", "true"),
                    Pair("true || (true || true)", "true"),
                    Pair("true || (false || true)", "true"),
                    Pair("false || (true || true)", "true")
            ).map { testCase ->
                val (expr, expected) = testCase

                dynamicTest("Printing $expr should output $expected") {
                    val code = "fn main() = print($expr)"
                    val file = kageFileFromCode(code)
                    compileAndExecuteFileAnd(file) { output -> assertEquals(expected, output) }
                }
            }
        }
    }

    @Nested
    @DisplayName("Testing expressions which are combinations of Conditional AND and OR Expressions (&&, ||)")
    inner class ConditionalAndsOrsTests {

        @TestFactory
        @DisplayName("Compiling and executing a print of nested conditional AND and OR statements")
        fun testConditionalAnd_conditionalOr_nestedExpressions(): List<DynamicTest> {
            // This obviously doesn't capture every permutation of this, but I'm sure it's enough
            return listOf(
                    Pair("(false && true) || true", "true"),
                    Pair("(true && true) || false", "true"),
                    Pair("(false && true) || false", "false"),

                    Pair("(false || true) && true", "true"),
                    Pair("(true || false) && false", "false"),

                    Pair("(false || true) && (true || false)", "true"),
                    Pair("(false && true) || (true && false)", "false")
            ).map { testCase ->
                val (expr, expected) = testCase

                dynamicTest("Printing $expr should output $expected") {
                    val code = "fn main() = print($expr)"
                    val file = kageFileFromCode(code)
                    compileAndExecuteFileAnd(file) { output -> assertEquals(expected, output) }
                }
            }
        }
    }
}