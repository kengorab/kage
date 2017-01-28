package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory

class ComparisonCodeGenTests : BaseTest() {

    @Nested
    @DisplayName("Greater Than and Greater Than or Equal To Expressions (>, >=)")
    inner class GreaterThanTests {

        @TestFactory
        @DisplayName("Compiling and executing a print of numeric greater-than (>) expressions")
        fun testGreaterThan_numericLiterals(): List<DynamicTest> {
            return listOf(
                    // Cover the case of equivalence
                    Pair("1 > 1", "false"),
                    Pair("1.0 > 1", "false"),
                    Pair("1 > 1.0", "false"),
                    Pair("1.0 > 1.0", "false"),

                    Pair("1 > 0", "true"),
                    Pair("-4 > -1", "false"),
                    Pair("100 > 1.0", "true"),
                    Pair("-5.42 > 100", "false"),
                    Pair("-5.42 > 100.43", "false"),

                    Pair("1 + 2 > 0 - 1", "true"),
                    Pair("-1 / 2 > 5 * 100.43", "false")
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
        @DisplayName("Compiling and executing a print of numeric greater-than-or-equal-to (>=) expressions")
        fun testGreaterThanOrEqualTo_numericLiterals(): List<DynamicTest> {
            return listOf(
                    Pair("1 >= 1", "true"),
                    Pair("1.0 >= 1", "true"),
                    Pair("1 >= 1.0", "true"),
                    Pair("1.0 >= 1.0", "true"),

                    Pair("1 >= 0", "true"),
                    Pair("-4 >= -1", "false"),
                    Pair("100 >= 1.0", "true"),
                    Pair("-5.42 >= 100", "false"),
                    Pair("-5.42 >= 100.43", "false"),

                    Pair("1 + 2 >= 0 - 1", "true"),
                    Pair("-1 / 2 >= 5 * 100.43", "false")
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
        @DisplayName("Compiling and executing a print of Comparable (only String, for now) greater-than (>) expressions")
        fun testGreaterThan_Comparables(): List<DynamicTest> {
            return listOf(
                    // Cover the case of equivalence
                    Pair("\"hello\" > \"hello\"", "false"),

                    Pair("\"Hello\" > \"hello\"", "false"),
                    Pair("\"abc\" ++ \"d\" > \"abc\"", "true")
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
        @DisplayName("Compiling and executing a print of Comparable (only String, for now) greater-than-or-equal-to (>=) expressions")
        fun testGreaterThanOrEqualTo_Comparables(): List<DynamicTest> {
            return listOf(
                    Pair("\"hello\" >= \"hello\"", "true"),

                    Pair("\"Hello\" >= \"hello\"", "false"),
                    Pair("\"abc\" ++ \"d\" >= \"abc\"", "true")
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
    @DisplayName("Less Than and Less Than Or Equal To Expressions (<, <=)")
    inner class LessThanTests {

        @TestFactory
        @DisplayName("Compiling and executing a print of numeric less-than (<) expressions")
        fun testLessThan_simpleNonNested_numericLiteralsOnly(): List<DynamicTest> {
            return listOf(
                    // Cover the case of equivalence
                    Pair("1 < 1", "false"),
                    Pair("1.0 < 1", "false"),
                    Pair("1 < 1.0", "false"),
                    Pair("1.0 < 1.0", "false"),

                    Pair("1 < 0", "false"),
                    Pair("-4 < -1", "true"),
                    Pair("100 < 1.0", "false"),
                    Pair("-5.42 < 100", "true"),
                    Pair("-5.42 < 100.43", "true"),

                    Pair("1 + 2 < 0 - 1", "false"),
                    Pair("-1 / 2 < 5 * 100.43", "true")
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
        @DisplayName("Compiling and executing a print of numeric less-than-or-equal-to (<=) expressions")
        fun testLessThanOrEqualTo_simpleNonNested_numericLiteralsOnly(): List<DynamicTest> {
            return listOf(
                    // Cover the case of equivalence
                    Pair("1 <= 1", "true"),
                    Pair("1.0 <= 1", "true"),
                    Pair("1 <= 1.0", "true"),
                    Pair("1.0 <= 1.0", "true"),

                    Pair("1 <= 0", "false"),
                    Pair("-4 <= -1", "true"),
                    Pair("100 <= 1.0", "false"),
                    Pair("-5.42 <= 100", "true"),
                    Pair("-5.42 <= 100.43", "true"),

                    Pair("1 + 2 <= 0 - 1", "false"),
                    Pair("-1 / 2 <= 5 * 100.43", "true")
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
        @DisplayName("Compiling and executing a print of Comparable (only String, for now) less-than (<) expressions")
        fun testLessThan_Comparables(): List<DynamicTest> {
            return listOf(
                    // Cover the case of equivalence
                    Pair("\"hello\" < \"hello\"", "false"),

                    Pair("\"Hello\" < \"hello\"", "true"),
                    Pair("\"abc\" ++ \"d\" < \"abc\"", "false")
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
        @DisplayName("Compiling and executing a print of Comparable (only String, for now) less-than-or-equal-to (<=) expressions")
        fun testLessThanOrEqualTo_Comparables(): List<DynamicTest> {
            return listOf(
                    Pair("\"hello\" <= \"hello\"", "true"),

                    Pair("\"Hello\" <= \"hello\"", "true"),
                    Pair("\"abc\" ++ \"d\" <= \"abc\"", "false")
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
    @DisplayName("Equals and Not-equals Expressions (==, !=)")
    inner class EqAndNeqTests {

        @TestFactory
        @DisplayName("Compiling and executing a print of numeric equals (==) expressions")
        fun testEquals_simpleNonNested_numericLiteralsOnly(): List<DynamicTest> {
            return listOf(
                    Pair("1 == 1", "true"),
                    Pair("1.0 == 1", "true"),
                    Pair("1 == 1.0", "true"),
                    Pair("1.0 == 1.0", "true"),

                    Pair("1 == 0", "false"),
                    Pair("-4 == -1", "false"),
                    Pair("100 == 1.0", "false"),
                    Pair("-5.42 == 100", "false"),
                    Pair("-5.42 == 100.43", "false"),

                    Pair("1 + 2 == 0 - 1", "false"),
                    Pair("-1 / 2 == 5 * 100.43", "false")
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
        @DisplayName("Compiling and executing a print of Comparable (only String, for now) equals (==) expressions")
        fun testEquals_Comparables(): List<DynamicTest> {
            return listOf(
                    Pair("\"hello\" == \"hello\"", "true"),

                    Pair("\"Hello\" == \"hello\"", "false"),
                    Pair("\"abc\" ++ \"d\" == \"abc\"", "false")
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
        @DisplayName("Compiling and executing a print of numeric not-equals (!=) expressions")
        fun testNotEquals_simpleNonNested_numericLiteralsOnly(): List<DynamicTest> {
            return listOf(
                    Pair("1 != 1", "false"),
                    Pair("1.0 != 1", "false"),
                    Pair("1 != 1.0", "false"),
                    Pair("1.0 != 1.0", "false"),

                    Pair("1 != 0", "true"),
                    Pair("-4 != -1", "true"),
                    Pair("100 != 1.0", "true"),
                    Pair("-5.42 != 100", "true"),
                    Pair("-5.42 != 100.43", "true"),

                    Pair("1 + 2 != 0 - 1", "true"),
                    Pair("-1 / 2 != 5 * 100.43", "true")
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
        @DisplayName("Compiling and executing a print of Comparable (only String, for now) not-equals (!=) expressions")
        fun testNotEquals_Comparables(): List<DynamicTest> {
            return listOf(
                    Pair("\"hello\" != \"hello\"", "false"),

                    Pair("\"Hello\" != \"hello\"", "true"),
                    Pair("\"abc\" ++ \"d\" != \"abc\"", "true")
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