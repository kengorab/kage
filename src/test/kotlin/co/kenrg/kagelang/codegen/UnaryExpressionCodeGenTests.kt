package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.tree.KGTree.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory

class UnaryExpressionCodeGenTests : BaseTest() {

    @Nested
    @DisplayName("Arithmetic negation unary operation (-)")
    inner class ArithmeticNegation() {

        @TestFactory
        fun testArithmeticNegation(): List<DynamicTest> {
            val cases = listOf(
                    Case("-1", KGUnary("-", intLiteral(1)), "-1"),
                    Case("-1.3", KGUnary("-", decLiteral(1.3)), "-1.3"),
                    Case("8 - -3", KGBinary(intLiteral(8), "-", KGUnary("-", intLiteral(3))), "11"),
                    Case("-(1 - 3)", KGUnary("-", KGParenthesized(KGBinary(intLiteral(1), "-", intLiteral(3)))), "2")
            )
            return generateTestsToCompileAndExecuteCases(cases)
        }
    }

    @Nested
    @DisplayName("Boolean negation unary operation (!)")
    inner class BooleanNegation() {
        @TestFactory
        fun testBooleanNegation(): List<DynamicTest> {
            val cases = listOf(
                    Case("!true", KGUnary("!", trueLiteral), "false"),
                    Case("!false", KGUnary("!", falseLiteral), "true"),
                    Case("!(true && false)", KGUnary("!", KGParenthesized(KGBinary(trueLiteral, "&&", falseLiteral))), "true"),
                    Case("!(!true || true)", KGUnary("!", KGParenthesized(KGBinary(KGUnary("!", falseLiteral), "||", trueLiteral))), "false"),
                    Case("!!true", KGUnary("!", KGUnary("!", trueLiteral)), "true"),
                    Case("!!!false", KGUnary("!", KGUnary("!", trueLiteral)), "true")
            )
            return generateTestsToCompileAndExecuteCases(cases)
        }
    }
}