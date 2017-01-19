package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.KGBinary
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory

class ComparisonCodeGenTests : BaseTest() {

    @Nested
    @DisplayName("Greater Than and Greater Than or Equal To Expressions (>, >=)")
    inner class GreaterThanTests {

        @TestFactory
        @DisplayName("Compiling and executing a print of numeric greater-than (>) expressions")
        fun testGreaterThan_numericLiterals(): List<DynamicTest> {
            val cases = listOf(
                    // Test false, because of equivalence
                    Case("1 > 1", KGBinary(intLiteral(1), ">", intLiteral(1)), "false"),
                    Case("1.0 > 1", KGBinary(decLiteral(1.0), ">", intLiteral(1)), "false"),
                    Case("1 > 1.0", KGBinary(intLiteral(1), ">", decLiteral(1.0)), "false"),
                    Case("1.0 > 1.0", KGBinary(decLiteral(1.0), ">", decLiteral(1.0)), "false"),

                    Case("1 > 0", KGBinary(intLiteral(1), ">", intLiteral(0)), "true"),
                    Case("-4 > -1", KGBinary(intLiteral(-4), ">", intLiteral(-1)), "false"),
                    Case("100 > 1.0", KGBinary(intLiteral(100), ">", decLiteral(1.0)), "true"),
                    Case("-5.42 > 100", KGBinary(decLiteral(-5.42), ">", intLiteral(100)), "false"),
                    Case("-5.42 > 100.43", KGBinary(decLiteral(-5.42), ">", decLiteral(100.43)), "false"),

                    Case("1 + 2 > 0 - 1", KGBinary(KGBinary(intLiteral(1), "+", intLiteral(2)), ">", KGBinary(intLiteral(0), "-", intLiteral(1))), "true"),
                    Case("-1 / 2 > 5 * 100.43", KGBinary(KGBinary(KGTree.KGUnary("-", intLiteral(1)), "/", intLiteral(2)), ">", KGBinary(intLiteral(5), "*", decLiteral(100.43))), "false")
            )
            return generateTestsToCompileAndExecuteCases(cases)
        }

        @TestFactory
        @DisplayName("Compiling and executing a print of numeric greater-than-or-equal-to (>=) expressions")
        fun testGreaterThanOrEqualTo_numericLiterals(): List<DynamicTest> {
            val cases = listOf(
                    Case("1 >= 1", KGBinary(intLiteral(1), ">=", intLiteral(1)), "true"),
                    Case("1.0 >= 1", KGBinary(decLiteral(1.0), ">=", intLiteral(1)), "true"),
                    Case("1 >= 1.0", KGBinary(intLiteral(1), ">=", decLiteral(1.0)), "true"),
                    Case("1.0 >= 1.0", KGBinary(decLiteral(1.0), ">=", decLiteral(1.0)), "true"),

                    Case("1 >= 0", KGBinary(intLiteral(1), ">=", intLiteral(0)), "true"),
                    Case("-4 >= -1", KGBinary(intLiteral(-4), ">=", intLiteral(-1)), "false"),
                    Case("100 >= 1.0", KGBinary(intLiteral(100), ">=", decLiteral(1.0)), "true"),
                    Case("-5.42 >= 100", KGBinary(decLiteral(-5.42), ">=", intLiteral(100)), "false"),
                    Case("-5.42 >= 100.43", KGBinary(decLiteral(-5.42), ">=", decLiteral(100.43)), "false"),

                    Case("1 + 2 >= 0 - 1", KGBinary(KGBinary(intLiteral(1), "+", intLiteral(2)), ">=", KGBinary(intLiteral(0), "-", intLiteral(1))), "true"),
                    Case("-1 / 2 >= 5 * 100.43", KGBinary(KGBinary(KGTree.KGUnary("-", intLiteral(1)), "/", intLiteral(2)), ">=", KGBinary(intLiteral(5), "*", decLiteral(100.43))), "false")
            )
            return generateTestsToCompileAndExecuteCases(cases)
        }

        @TestFactory
        @DisplayName("Compiling and executing a print of Comparable (only String, for now) greater-than (>) expressions")
        fun testGreaterThan_Comparables(): List<DynamicTest> {
            val cases = listOf(
                    // Test false, because of equivalence
                    Case("\"hello\" > \"hello\"", KGBinary(stringLiteral("hello"), ">", stringLiteral("hello")), "false"),

                    Case("\"Hello\" > \"hello\"", KGBinary(stringLiteral("Hello"), ">", stringLiteral("hello")), "false"),
                    Case("\"abc\" ++ \"d\" > \"abc\"", KGBinary(KGBinary(stringLiteral("abc"), "++", stringLiteral("d")), ">", stringLiteral("abc")), "true")
            )
            return generateTestsToCompileAndExecuteCases(cases)
        }

        @TestFactory
        @DisplayName("Compiling and executing a print of Comparable (only String, for now) greater-than-or-equal-to (>=) expressions")
        fun testGreaterThanOrEqualTo_Comparables(): List<DynamicTest> {
            val cases = listOf(
                    Case("\"hello\" >= \"hello\"", KGBinary(stringLiteral("hello"), ">=", stringLiteral("hello")), "true"),

                    Case("\"Hello\" >= \"hello\"", KGBinary(stringLiteral("Hello"), ">=", stringLiteral("hello")), "false"),
                    Case("\"abc\" ++ \"d\" >= \"abc\"", KGBinary(KGBinary(stringLiteral("abc"), "++", stringLiteral("d")), ">=", stringLiteral("abc")), "true")
            )
            return generateTestsToCompileAndExecuteCases(cases)
        }
    }

    @Nested
    @DisplayName("Less Than and Less Than Or Equal To Expressions (<, <=)")
    inner class LessThanTests {

        @TestFactory
        @DisplayName("Compiling and executing a print of numeric less-than (<) expressions")
        fun testLessThan_simpleNonNested_numericLiteralsOnly(): List<DynamicTest> {
            val cases = listOf(
                    // Test false, because of equivalence
                    Case("1 < 1", KGBinary(intLiteral(1), "<", intLiteral(1)), "false"),
                    Case("1.0 < 1", KGBinary(decLiteral(1.0), "<", intLiteral(1)), "false"),
                    Case("1 < 1.0", KGBinary(intLiteral(1), "<", decLiteral(1.0)), "false"),
                    Case("1.0 < 1.0", KGBinary(decLiteral(1.0), "<", decLiteral(1.0)), "false"),

                    Case("1 < 0", KGBinary(intLiteral(1), "<", intLiteral(0)), "false"),
                    Case("-4 < -1", KGBinary(intLiteral(-4), "<", intLiteral(-1)), "true"),
                    Case("100 < 1.0", KGBinary(intLiteral(100), "<", decLiteral(1.0)), "false"),
                    Case("-5.42 < 100", KGBinary(decLiteral(-5.42), "<", intLiteral(100)), "true"),
                    Case("-5.42 < 100.43", KGBinary(decLiteral(-5.42), "<", decLiteral(100.43)), "true"),

                    Case("1 + 2 < 0 - 1", KGBinary(KGBinary(intLiteral(1), "+", intLiteral(2)), "<", KGBinary(intLiteral(0), "-", intLiteral(1))), "false"),
                    Case("-1 / 2 < 5 * 100.43", KGBinary(KGBinary(KGTree.KGUnary("-", intLiteral(1)), "/", intLiteral(2)), "<", KGBinary(intLiteral(5), "*", decLiteral(100.43))), "true")
            )
            return generateTestsToCompileAndExecuteCases(cases)
        }

        @TestFactory
        @DisplayName("Compiling and executing a print of numeric less-than-or-equal-to (<, <=) expressions")
        fun testLessThanOrEqualTo_simpleNonNested_numericLiteralsOnly(): List<DynamicTest> {
            val cases = listOf(
                    // Test false, because of equivalence
                    Case("1 <= 1", KGBinary(intLiteral(1), "<=", intLiteral(1)), "true"),
                    Case("1.0 <= 1", KGBinary(decLiteral(1.0), "<=", intLiteral(1)), "true"),
                    Case("1 <= 1.0", KGBinary(intLiteral(1), "<=", decLiteral(1.0)), "true"),
                    Case("1.0 <= 1.0", KGBinary(decLiteral(1.0), "<=", decLiteral(1.0)), "true"),

                    Case("1 <= 0", KGBinary(intLiteral(1), "<=", intLiteral(0)), "false"),
                    Case("-4 <= -1", KGBinary(intLiteral(-4), "<=", intLiteral(-1)), "true"),
                    Case("100 <= 1.0", KGBinary(intLiteral(100), "<=", decLiteral(1.0)), "false"),
                    Case("-5.42 <= 100", KGBinary(decLiteral(-5.42), "<=", intLiteral(100)), "true"),
                    Case("-5.42 <= 100.43", KGBinary(decLiteral(-5.42), "<=", decLiteral(100.43)), "true"),

                    Case("1 + 2 <= 0 - 1", KGBinary(KGBinary(intLiteral(1), "+", intLiteral(2)), "<=", KGBinary(intLiteral(0), "-", intLiteral(1))), "false"),
                    Case("-1 / 2 <= 5 * 100.43", KGBinary(KGBinary(KGTree.KGUnary("-", intLiteral(1)), "/", intLiteral(2)), "<=", KGBinary(intLiteral(5), "*", decLiteral(100.43))), "true")
            )
            return generateTestsToCompileAndExecuteCases(cases)
        }

        @TestFactory
        @DisplayName("Compiling and executing a print of Comparable (only String, for now) less-than (<) expressions")
        fun testLessThan_Comparables(): List<DynamicTest> {
            val cases = listOf(
                    // Test false, because of equivalence
                    Case("\"hello\" < \"hello\"", KGBinary(stringLiteral("hello"), "<", stringLiteral("hello")), "false"),

                    Case("\"Hello\" < \"hello\"", KGBinary(stringLiteral("Hello"), "<", stringLiteral("hello")), "true"),
                    Case("\"abc\" ++ \"d\" < \"abc\"", KGBinary(KGBinary(stringLiteral("abc"), "++", stringLiteral("d")), "<", stringLiteral("abc")), "false")
            )
            return generateTestsToCompileAndExecuteCases(cases)
        }

        @TestFactory
        @DisplayName("Compiling and executing a print of Comparable (only String, for now) less-than-or-equal-to (<=) expressions")
        fun testLessThanOrEqualTo_Comparables(): List<DynamicTest> {
            val cases = listOf(
                    Case("\"hello\" <= \"hello\"", KGBinary(stringLiteral("hello"), "<=", stringLiteral("hello")), "true"),

                    Case("\"Hello\" <= \"hello\"", KGBinary(stringLiteral("Hello"), "<=", stringLiteral("hello")), "true"),
                    Case("\"abc\" ++ \"d\" <= \"abc\"", KGBinary(KGBinary(stringLiteral("abc"), "++", stringLiteral("d")), "<=", stringLiteral("abc")), "false")
            )
            return generateTestsToCompileAndExecuteCases(cases)
        }
    }
}