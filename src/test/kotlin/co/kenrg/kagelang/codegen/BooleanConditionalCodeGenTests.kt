package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.tree.KGTree.KGBinary
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory

// Note: All of the parentheses in the string representations of expressions below are for the benefit of the reader.
// The parenthetical grouping is implicit in the construction of the Binary expressions, and would be constructed during
// the parsing/lexing phase.
class BooleanConditionalCodeGenTests : BaseTest() {

    @Nested
    @DisplayName("Conditional AND Expressions (&&)")
    inner class ConditionalAndTests {

        @TestFactory
        @DisplayName("Compiling and executing a print of simple, non-nested conditional AND statements")
        fun testConditionalAnd_simpleNonNested_boolLiteralsOnly(): List<DynamicTest> {
            val cases = listOf(
                    Case("true && true", KGBinary(trueLiteral(), "&&", trueLiteral()), "true"),
                    Case("false && true", KGBinary(falseLiteral(), "&&", trueLiteral()), "false"),
                    Case("true && false", KGBinary(trueLiteral(), "&&", falseLiteral()), "false"),
                    Case("false && false", KGBinary(falseLiteral(), "&&", falseLiteral()), "false")
            )
            return generateTestsToCompileAndExecuteCases(cases)
        }

        @TestFactory
        @DisplayName("Compiling and executing a print of simple, nested conditional AND statements")
        fun testConditionalAnd_simpleNestedAndExpressions(): List<DynamicTest> {
            // This obviously doesn't capture every permutation of this, but I'm sure it's enough
            val cases = listOf(
                    Case("(true && true) && true", KGBinary(KGBinary(trueLiteral(), "&&", trueLiteral()), "&&", trueLiteral()), "true"),
                    Case("(true && false) && true", KGBinary(KGBinary(trueLiteral(), "&&", falseLiteral()), "&&", trueLiteral()), "false"),
                    Case("(true && true) && false", KGBinary(KGBinary(trueLiteral(), "&&", trueLiteral()), "&&", falseLiteral()), "false"),
                    Case("true && (true && true)", KGBinary(trueLiteral(), "&&", KGBinary(trueLiteral(), "&&", trueLiteral())), "true"),
                    Case("true && (false && true)", KGBinary(trueLiteral(), "&&", KGBinary(falseLiteral(), "&&", trueLiteral())), "false"),
                    Case("false && (true && true)", KGBinary(falseLiteral(), "&&", KGBinary(trueLiteral(), "&&", trueLiteral())), "false")
            )
            return generateTestsToCompileAndExecuteCases(cases)
        }
    }

    @Nested
    @DisplayName("Conditional OR Expressions (||)")
    inner class ConditionalOrTests {

        @TestFactory
        @DisplayName("Compiling and executing a print of simple, non-nested conditional OR statements")
        fun testConditionalOr_simpleNonNested_boolLiteralsOnly(): List<DynamicTest> {
            val cases = listOf(
                    Case("true || true", KGBinary(trueLiteral(), "||", trueLiteral()), "true"),
                    Case("false || true", KGBinary(falseLiteral(), "||", trueLiteral()), "true"),
                    Case("true || false", KGBinary(trueLiteral(), "||", falseLiteral()), "true"),
                    Case("false || false", KGBinary(falseLiteral(), "||", falseLiteral()), "false")
            )
            return generateTestsToCompileAndExecuteCases(cases)
        }

        @TestFactory
        @DisplayName("Compiling and executing a print of simple, nested conditional OR statements")
        fun testConditionalOr_simpleNestedOrExpressions(): List<DynamicTest> {
            // This obviously doesn't capture every permutation of this, but I'm sure it's enough
            val cases = listOf(
                    Case("(true || true) || true", KGBinary(KGBinary(trueLiteral(), "||", trueLiteral()), "||", trueLiteral()), "true"),
                    Case("(true || false) || true", KGBinary(KGBinary(trueLiteral(), "||", falseLiteral()), "||", trueLiteral()), "true"),
                    Case("(true || true) || false", KGBinary(KGBinary(trueLiteral(), "||", trueLiteral()), "||", falseLiteral()), "true"),
                    Case("true || (true || true)", KGBinary(trueLiteral(), "||", KGBinary(trueLiteral(), "||", trueLiteral())), "true"),
                    Case("true || (false || true)", KGBinary(trueLiteral(), "||", KGBinary(falseLiteral(), "||", trueLiteral())), "true"),
                    Case("false || (true || true)", KGBinary(falseLiteral(), "||", KGBinary(trueLiteral(), "||", trueLiteral())), "true")
            )
            return generateTestsToCompileAndExecuteCases(cases)
        }
    }

    @Nested
    @DisplayName("Testing expressions which are combinations of Conditional AND and OR Expressions (&&, ||)")
    inner class ConditionalAndsOrsTests {

        @TestFactory
        @DisplayName("Compiling and executing a print of nested conditional AND and OR statements")
        fun testConditionalAnd_conditionalOr_nestedExpressions(): List<DynamicTest> {
            // This obviously doesn't capture every permutation of this, but I'm sure it's enough
            val cases = listOf(
                    Case("(false && true) || true", KGBinary(KGBinary(falseLiteral(), "&&", trueLiteral()), "||", trueLiteral()), "true"),
                    Case("(true && true) || false", KGBinary(KGBinary(trueLiteral(), "&&", trueLiteral()), "||", falseLiteral()), "true"),
                    Case("(false && true) || false", KGBinary(KGBinary(falseLiteral(), "&&", trueLiteral()), "||", falseLiteral()), "false"),

                    Case("(false || true) && true", KGBinary(KGBinary(falseLiteral(), "||", trueLiteral()), "&&", trueLiteral()), "true"),
                    Case("(true || false) && false", KGBinary(KGBinary(trueLiteral(), "||", falseLiteral()), "&&", falseLiteral()), "false"),

                    Case("(false || true) && (true || false)", KGBinary(KGBinary(falseLiteral(), "||", trueLiteral()), "&&", KGBinary(trueLiteral(), "||", falseLiteral())), "true"),
                    Case("(false && true) || (true && false)", KGBinary(KGBinary(falseLiteral(), "&&", trueLiteral()), "||", KGBinary(trueLiteral(), "&&", falseLiteral())), "false")
            )
            return generateTestsToCompileAndExecuteCases(cases)
        }
    }
}