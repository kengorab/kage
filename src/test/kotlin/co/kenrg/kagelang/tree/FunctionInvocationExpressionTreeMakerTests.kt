package co.kenrg.kagelang.tree

import co.kenrg.kagelang.tree.KGTree.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FunctionInvocationExpressionTreeMakerTests {

    @Test fun testParseFunctionInvocation() {
        val kageFile = kageFileFromCode("returnOne()")
        val expected = kageFileFromLines(KGInvocation(KGBindingReference("returnOne")))
        assertEquals(expected, kageFile)
    }

    @Test fun testParseFunctionInvocation_invocationsShouldBeTreatedAsExpressions() {
        val kageFile = kageFileFromCode("one() + one()")
        val expected = kageFileFromLines(KGBinary(
                KGInvocation(KGBindingReference("one")),
                "+",
                KGInvocation(KGBindingReference(("one"))))
        )
        assertEquals(expected, kageFile)
    }
}