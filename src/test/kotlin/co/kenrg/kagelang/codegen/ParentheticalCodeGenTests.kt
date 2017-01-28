package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class ParentheticalCodeGenTests : BaseTest() {

    @TestFactory
    @DisplayName("Testing boolean and arithmetic binary operations within parentheses")
    fun testParenthesizedBinaryExpressions(): List<DynamicTest> {
        return listOf(
                Pair("(true || false) && true", "true"),
                Pair("(1 + (3 * 2)) - 1", "6"),
                Pair("2 * (3) / 2.0", "3.0")
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