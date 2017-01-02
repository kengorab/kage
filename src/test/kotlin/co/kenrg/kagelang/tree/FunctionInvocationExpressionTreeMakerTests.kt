package co.kenrg.kagelang.tree

import co.kenrg.kagelang.tree.KGTree.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FunctionInvocationExpressionTreeMakerTests {

    @Test fun testParseFunctionInvocation() {
        val kageFile = kageFileFromCode("print(returnOne())")
        val expected = kageFileFromStatements(KGPrint(KGInvocation(KGBindingReference("returnOne"))))
        assertEquals(expected, kageFile)
    }

    @Test fun testParseFunctionInvocation_invocationsShouldBeTreatedAsExpressions() {
        val kageFile = kageFileFromCode("print(one() + one())")
        val expected = kageFileFromStatements(KGPrint(KGBinary(
                KGInvocation(KGBindingReference("one")),
                "+",
                KGInvocation(KGBindingReference(("one")))))
        )
        assertEquals(expected, kageFile)
    }
}