package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class ArithmeticExpressionCodeGenTests : BaseTest() {

    @TestFactory
    @DisplayName("Compiling and executing a print of arithmetic addition (+)")
    fun testArithmeticAddition(): List<DynamicTest> {
        return listOf(
                Pair("1 + 2", "3"),
                Pair("(1 + 2) + 3", "6"),
                Pair("1 + (2 + 3)", "6"),

                Pair("1.3 + 2", "3.3"),
                Pair("1.3 + 2.0", "3.3"),
                Pair("(1 + 2.0) + 3", "6.0"),
                Pair("1.7 + (2.3 + 3)", "7.0")
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
    @DisplayName("Compiling and executing a print of arithmetic subtraction (-)")
    fun testArithmeticSubtraction(): List<DynamicTest> {
        return listOf(
                Pair("2 - 1", "1"),
                Pair("(3 - 2) - 1", "0"),
                Pair("3 - (2 - 3)", "4"),

                Pair("1.3 - 2", "-0.7"),
                Pair("2.0 - 1.3", "0.7"),
                Pair("(1 - 2.0) - 3", "-4.0"),
                Pair("1.7 - (2.4 - 3)", "2.3")
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
    @DisplayName("Compiling and executing a print of arithmetic multiplication (*)")
    fun testArithmeticMultiplication(): List<DynamicTest> {
        return listOf(
                Pair("2 * 1", "2"),
                Pair("(3 * 2) * 1", "6"),
                Pair("3 * (2 * 3)", "18"),

                Pair("1.3 * 2", "2.6"),
                Pair("2.0 * 1.3", "2.6"),
                Pair("(1 * 2.0) * 3", "6.0"),
                Pair("1.6 * (2.4 * 3)", "11.52")
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
    @DisplayName("Compiling and executing a print of arithmetic division (/)")
    fun testArithmeticDivision(): List<DynamicTest> {
        return listOf(
                Pair("4 / 2", "2.0"),
                Pair("(3 / 2) / 1", "1.5"),
                Pair("3 / (2 / 3)", "4.5"),

                Pair("1.3 / 2", "0.65"),
                Pair("2.0 / 1.3", "1.5384615384615383"),
                Pair("(1 / 2.0) / 3", "0.16666666666666666"),
                Pair("1.6 / (2.5 / 3)", "1.92")
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
    fun testCombinationOfArithmeticOperations(): List<DynamicTest> {
        return listOf(
                Pair("(3 + 1) * 3 / (4 - 1)", "4.0"),
                Pair("2 * (1 / 2.0)", "1.0")
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
