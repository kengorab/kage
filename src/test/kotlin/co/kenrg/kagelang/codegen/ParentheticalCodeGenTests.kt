package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.tree.KGTree.KGBinary
import co.kenrg.kagelang.tree.KGTree.KGParenthesized
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

// Note: Whereas in ArithmeticExpressionCodeGenTests and BooleanConditionalCodeGenTests the parentheses were symbolic,
// here they refer to actual Parenthetical trees.
class ParentheticalCodeGenTests : BaseTest() {

    @TestFactory
    @DisplayName("Testing boolean and arithmetic binary operations within parentheses")
    fun testParenthesizedBinaryExpressions(): List<DynamicTest> {
        val cases = listOf(
                Case("(true || false) && true", KGBinary(KGParenthesized(KGBinary(trueLiteral, "||", falseLiteral)), "&&", trueLiteral), "true"),
                Case("(1 + (3 * 2)) - 1", KGBinary(KGParenthesized(KGBinary(intLiteral(1), "+", KGParenthesized(KGBinary(intLiteral(3), "*", intLiteral(2))))), "-", intLiteral(1)), "6"),
                Case("2 * (3) / 2.0", KGBinary(KGBinary(intLiteral(2), "*", KGParenthesized(intLiteral(3))), "/", decLiteral(2.0)), "3.0")
        )
        return generateTestsToCompileAndExecuteCases(cases)
    }
}