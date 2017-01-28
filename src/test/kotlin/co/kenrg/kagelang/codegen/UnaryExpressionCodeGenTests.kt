package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory

class UnaryExpressionCodeGenTests : BaseTest() {

    @Nested
    @DisplayName("Arithmetic negation unary operation (-)")
    inner class ArithmeticNegation {

        @TestFactory
        fun testArithmeticNegation(): List<DynamicTest> {
            return listOf(
                    Pair("-1", "-1"),
                    Pair("-1.3", "-1.3"),
                    Pair("8 - -3", "11"),
                    Pair("-(1 - 3)", "2")
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
    @DisplayName("Boolean negation unary operation (!)")
    inner class BooleanNegation {
        @TestFactory
        fun testBooleanNegation(): List<DynamicTest> {
            return listOf(
                    Pair("!true", "false"),
                    Pair("!false", "true"),
                    Pair("!(true && false)", "true"),
                    Pair("!(!true || true)", "false"),
                    Pair("!!true", "true"),
                    Pair("!!!false", "true")
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