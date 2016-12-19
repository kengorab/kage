package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.tree.KGTree.KGBinary
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

// Note: All of the parentheses in the string representations of expressions below are for the benefit of the reader.
// The parenthetical grouping is implicit in the construction of the Binary expressions, and would be constructed during
// the parsing/lexing phase.
class ArithmeticExpressionCodeGenTests : BaseTest() {

    @TestFactory
    @DisplayName("Compiling and executing a print of arithmetic addition (+)")
    fun testArithmeticAddition(): List<DynamicTest> {
        val cases = listOf(
                Case("1 + 2", KGBinary(intLiteral(1), "+", intLiteral(2)), "3"),
                Case("(1 + 2) + 3", KGBinary(KGBinary(intLiteral(1), "+", intLiteral(2)), "+", intLiteral(3)), "6"),
                Case("1 + (2 + 3)", KGBinary(intLiteral(1), "+", KGBinary(intLiteral(2), "+", intLiteral(3))), "6"),

                Case("1.3 + 2", KGBinary(decLiteral(1.3), "+", intLiteral(2)), "3.3"),
                Case("1.3 + 2.0", KGBinary(decLiteral(1.3), "+", decLiteral(2.0)), "3.3"),
                Case("(1 + 2.0) + 3", KGBinary(KGBinary(intLiteral(1), "+", decLiteral(2.0)), "+", intLiteral(3)), "6.0"),
                Case("1.7 + (2.3 + 3)", KGBinary(decLiteral(1.7), "+", KGBinary(decLiteral(2.3), "+", intLiteral(3))), "7.0")
        )
        return generateTestsToCompileAndExecuteCases(cases)
    }

    @TestFactory
    @DisplayName("Compiling and executing a print of arithmetic subtraction (-)")
    fun testArithmeticSubtraction(): List<DynamicTest> {
        val cases = listOf(
                Case("2 - 1", KGBinary(intLiteral(2), "-", intLiteral(1)), "1"),
                Case("(3 - 2) - 1", KGBinary(KGBinary(intLiteral(3), "-", intLiteral(2)), "-", intLiteral(1)), "0"),
                Case("3 - (2 - 3)", KGBinary(intLiteral(3), "-", KGBinary(intLiteral(2), "-", intLiteral(3))), "4"),

                Case("1.3 - 2", KGBinary(decLiteral(1.3), "-", intLiteral(2)), "-0.7"),
                Case("2.0 - 1.3", KGBinary(decLiteral(2.0), "-", decLiteral(1.3)), "0.7"),
                Case("(1 - 2.0) - 3", KGBinary(KGBinary(intLiteral(1), "-", decLiteral(2.0)), "-", intLiteral(3)), "-4.0"),
                Case("1.7 - (2.4 - 3)", KGBinary(decLiteral(1.7), "-", KGBinary(decLiteral(2.4), "-", intLiteral(3))), "2.3")
        )
        return generateTestsToCompileAndExecuteCases(cases)
    }

    @TestFactory
    @DisplayName("Compiling and executing a print of arithmetic multiplication (*)")
    fun testArithmeticMultiplication(): List<DynamicTest> {
        val cases = listOf(
                Case("2 * 1", KGBinary(intLiteral(2), "*", intLiteral(1)), "2"),
                Case("(3 * 2) * 1", KGBinary(KGBinary(intLiteral(3), "*", intLiteral(2)), "*", intLiteral(1)), "6"),
                Case("3 * (2 * 3)", KGBinary(intLiteral(3), "*", KGBinary(intLiteral(2), "*", intLiteral(3))), "18"),

                Case("1.3 * 2", KGBinary(decLiteral(1.3), "*", intLiteral(2)), "2.6"),
                Case("2.0 * 1.3", KGBinary(decLiteral(2.0), "*", decLiteral(1.3)), "2.6"),
                Case("(1 * 2.0) * 3", KGBinary(KGBinary(intLiteral(1), "*", decLiteral(2.0)), "*", intLiteral(3)), "6.0"),
                Case("1.6 * (2.4 * 3)", KGBinary(decLiteral(1.6), "*", KGBinary(decLiteral(2.4), "*", intLiteral(3))), "11.52")
        )
        return generateTestsToCompileAndExecuteCases(cases)
    }

    @TestFactory
    @DisplayName("Compiling and executing a print of arithmetic division (/)")
    fun testArithmeticDivision(): List<DynamicTest> {
        val cases = listOf(
                Case("4 / 2", KGBinary(intLiteral(4), "/", intLiteral(2)), "2.0"),
                Case("(3 / 2) / 1", KGBinary(KGBinary(intLiteral(3), "/", intLiteral(2)), "/", intLiteral(1)), "1.5"),
                Case("3 / (2 / 3)", KGBinary(intLiteral(3), "/", KGBinary(intLiteral(2), "/", intLiteral(3))), "4.5"),

                Case("1.3 / 2", KGBinary(decLiteral(1.3), "/", intLiteral(2)), "0.65"),
                Case("2.0 / 1.3", KGBinary(decLiteral(2.0), "/", decLiteral(1.3)), "1.5384615384615383"),
                Case("(1 / 2.0) / 3", KGBinary(KGBinary(intLiteral(1), "/", decLiteral(2.0)), "/", intLiteral(3)), "0.16666666666666666"),
                Case("1.6 / (2.5 / 3)", KGBinary(decLiteral(1.6), "/", KGBinary(decLiteral(2.5), "/", intLiteral(3))), "1.92")
        )
        return generateTestsToCompileAndExecuteCases(cases)
    }

    @TestFactory
    fun testCombinationOfArithmeticOperations(): List<DynamicTest> {
        val cases = listOf(
                Case("(3 + 1) * 3 / (4 - 1)",
                        KGBinary(
                                KGBinary(KGBinary(intLiteral(3), "+", intLiteral(1)), "*", intLiteral(3)),
                                "/",
                                KGBinary(intLiteral(4), "-", intLiteral(1))),
                        "4.0"),
                Case("2 * (1 / 2.0)", KGBinary(intLiteral(2), "*", KGBinary(intLiteral(1), "/", decLiteral(2.0))), "1.0")
        )
        return generateTestsToCompileAndExecuteCases(cases)
    }
}
