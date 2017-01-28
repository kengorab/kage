package co.kenrg.kagelang.tree

import co.kenrg.kagelang.codegen.decLiteral
import co.kenrg.kagelang.kageFileFromCode
import co.kenrg.kagelang.kageFileFromLines
import co.kenrg.kagelang.tree.KGTree.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DotExpressionTreeMakerTests {

    @Test fun testDotExpression_ignoresDecimalNumbers() {
        val kageFile = kageFileFromCode("0.14")
        val expected = kageFileFromLines(
                decLiteral(0.14)
        )
        assertEquals(expected, kageFile)
    }

    @Test fun testDotExpression_oneLevelDeep() {
        val kageFile = kageFileFromCode("parent.child")
        val expected = kageFileFromLines(
                KGDot(KGBindingReference("parent"), "child")
        )
        assertEquals(expected, kageFile)
    }

    @Test fun testDotExpression_twoLevelsDeep() {
        val kageFile = kageFileFromCode("grandparent.parent.child")
        val expected = kageFileFromLines(
                KGDot(KGDot(KGBindingReference("grandparent"), "parent"), "child")
        )
        assertEquals(expected, kageFile)
    }

    @Test fun testDotExpression_usedInBinaryExpression() {
        val kageFile = kageFileFromCode("a.b == b.c")
        val expected = kageFileFromLines(
                KGBinary(KGDot(KGBindingReference("a"), "b"), "==", KGDot(KGBindingReference("b"), "c"))
        )
        assertEquals(expected, kageFile)
    }

    @Test fun testDotExpression_passedAsAParameter() {
        val kageFile = kageFileFromCode("someFunc(a.b, b.c)")
        val expected = kageFileFromLines(KGInvocation(
                KGBindingReference("someFunc"),
                listOf(KGDot(KGBindingReference("a"), "b"), KGDot(KGBindingReference("b"), "c"))
        ))
        assertEquals(expected, kageFile)
    }

    @Test fun testDotExpression_invokedAsAFunction() {
        val kageFile = kageFileFromCode("a.b.c()")
        val expected = kageFileFromLines(KGInvocation(
                KGDot(KGDot(KGBindingReference("a"), "b"), "c"),
                listOf()
        ))
        assertEquals(expected, kageFile)
    }
}