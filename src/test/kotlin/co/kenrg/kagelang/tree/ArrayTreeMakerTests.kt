package co.kenrg.kagelang.tree

import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.kageFileFromCode
import co.kenrg.kagelang.kageFileFromLines
import co.kenrg.kagelang.tree.KGTree.KGArray
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ArrayTreeMakerTests {

    @Test fun testArrayExpression_oneItem() {
        val code = "[1]"
        val kageFile = kageFileFromCode(code)
        val expected = kageFileFromLines(KGArray(listOf(intLiteral(1))))
        assertEquals(expected, kageFile)
    }

    @Test fun testArrayExpression_twoItems() {
        val code = "[1, 2]"
        val kageFile = kageFileFromCode(code)
        val expected = kageFileFromLines(KGArray(listOf(intLiteral(1), intLiteral(2))))
        assertEquals(expected, kageFile)
    }

    @Test fun testArrayExpression_threeItems() {
        val code = "[\"a\", \"b\", \"c\"]"
        val kageFile = kageFileFromCode(code)
        val expected = kageFileFromLines(KGArray(listOf(stringLiteral("a"), stringLiteral("b"), stringLiteral("c"))))
        assertEquals(expected, kageFile)
    }

    @Test fun testArrayExpression_nested() {
        val code = "[[1, 2], [3, 4]]"
        val kageFile = kageFileFromCode(code)
        val expected = kageFileFromLines(
                KGArray(
                        listOf(
                                KGArray(listOf(intLiteral(1), intLiteral(2))),
                                KGArray(listOf(intLiteral(3), intLiteral(4)))
                        )
                )
        )
        assertEquals(expected, kageFile)
    }
}