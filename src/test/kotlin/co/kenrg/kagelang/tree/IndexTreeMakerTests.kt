package co.kenrg.kagelang.tree

import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.kageFileFromCode
import co.kenrg.kagelang.kageFileFromLines
import co.kenrg.kagelang.tree.KGTree.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class IndexTreeMakerTests {

    @Nested
    inner class IntegerIndex {

        @Test fun testIndexExpression_arrayLiteral() {
            val code = "[1, 2, 3][1]"
            val kageFile = kageFileFromCode(code)
            val expected = kageFileFromLines(
                    KGIndex(KGArray(listOf(intLiteral(1), intLiteral(2), intLiteral(3))), intLiteral(1))
            )
            assertEquals(expected, kageFile)
        }

        @Test fun testIndexExpression_bindingReference() {
            val code = "someArr[0]"
            val kageFile = kageFileFromCode(code)
            val expected = kageFileFromLines(
                    KGIndex(KGBindingReference("someArr"), intLiteral(0))
            )
            assertEquals(expected, kageFile)
        }

        @Test fun testIndexExpression_tupleLiteral() {
            val code = "(1, 2)[0]"
            val kageFile = kageFileFromCode(code)
            val expected = kageFileFromLines(
                    KGIndex(KGTuple(listOf(intLiteral(1), intLiteral(2))), intLiteral(0))
            )
            assertEquals(expected, kageFile)
        }
    }

    @Nested
    inner class StringIndex {

        @Test fun testIndexExpression_arrayLiteral() {
            val code = "[1, 2, 3][\"0\"]"
            val kageFile = kageFileFromCode(code)
            val expected = kageFileFromLines(
                    KGIndex(KGArray(listOf(intLiteral(1), intLiteral(2), intLiteral(3))), stringLiteral("0"))
            )
            assertEquals(expected, kageFile)
        }

        @Test fun testIndexExpression_bindingReference() {
            val code = "someVal[\"0\"]"
            val kageFile = kageFileFromCode(code)
            val expected = kageFileFromLines(
                    KGIndex(KGBindingReference("someVal"), stringLiteral("0"))
            )
            assertEquals(expected, kageFile)
        }

        @Test fun testIndexExpression_tupleLiteral() {
            val code = "(1, 2)[\"0\"]"
            val kageFile = kageFileFromCode(code)
            val expected = kageFileFromLines(
                    KGIndex(KGTuple(listOf(intLiteral(1), intLiteral(2))), stringLiteral("0"))
            )
            assertEquals(expected, kageFile)
        }
    }

    @Nested
    inner class ExpressionIndex {

        @Test fun testIndexExpression_bindingReference_indexIsSumOfLiterals() {
            val code = "someVal[1 + 1]"
            val kageFile = kageFileFromCode(code)
            val expected = kageFileFromLines(
                    KGIndex(KGBindingReference("someVal"), KGBinary(intLiteral(1), "+", intLiteral(1)))
            )
            assertEquals(expected, kageFile)
        }

        @Test fun testIndexExpression_bindingReference_indexIsIndexExpr() {
            val code = "someVal[otherVal[1]]"
            val kageFile = kageFileFromCode(code)
            val expected = kageFileFromLines(
                    KGIndex(KGBindingReference("someVal"),
                            KGIndex(KGBindingReference("otherVal"), intLiteral(1))
                    )
            )
            assertEquals(expected, kageFile)
        }
    }
}