package co.kenrg.kagelang.tree

import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.kageFileFromCode
import co.kenrg.kagelang.kageFileFromLines
import co.kenrg.kagelang.tree.KGTree.KGParenthesized
import co.kenrg.kagelang.tree.KGTree.KGTuple
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TupleTreeMakerTests {

    @Test fun testTupleExpression_oneItem_transformedIntoParenExpressionNotTupleOfOne() {
        val code = "(1)"
        val kageFile = kageFileFromCode(code)
        val expected = kageFileFromLines(KGParenthesized(intLiteral(1)))
        assertEquals(expected, kageFile)
    }

    @Test fun testTupleExpression_twoItems() {
        val code = "(1, 2)"
        val kageFile = kageFileFromCode(code)
        val expected = kageFileFromLines(KGTuple(listOf(intLiteral(1), intLiteral(2))))
        assertEquals(expected, kageFile)
    }

    @Test fun testTupleExpression_threeItems() {
        val code = "(1, 2, \"asdf\")"
        val kageFile = kageFileFromCode(code)
        val expected = kageFileFromLines(KGTuple(listOf(intLiteral(1), intLiteral(2), stringLiteral("asdf"))))
        assertEquals(expected, kageFile)
    }

    @Test fun testTupleExpression_nested() {
        val code = "(1, 2, (1, 2))"
        val kageFile = kageFileFromCode(code)
        val expected = kageFileFromLines(
                KGTuple(
                        listOf(
                                intLiteral(1),
                                intLiteral(2),
                                KGTuple(listOf(intLiteral(1), intLiteral(2)))
                        )
                )
        )
        assertEquals(expected, kageFile)
    }
}