package co.kenrg.kagelang.tree

import co.kenrg.kagelang.codegen.decLiteral
import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.kageFileFromCode
import co.kenrg.kagelang.kageFileFromLines
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

    @Test fun testParseFunctionInvocation_oneArgument() {
        val kageFile = kageFileFromCode("returnOne(1)")
        val expected = kageFileFromLines(KGInvocation(KGBindingReference("returnOne"), listOf(intLiteral(1))))
        assertEquals(expected, kageFile)
    }

    @Test fun testParseFunctionInvocation_twoArguments() {
        val kageFile = kageFileFromCode("returnOne(1, \"asdf\")")
        val expected = kageFileFromLines(KGInvocation(KGBindingReference("returnOne"), listOf(intLiteral(1), stringLiteral("asdf"))))
        assertEquals(expected, kageFile)
    }

    @Test fun testParseFunctionInvocation_threeArguments() {
        val kageFile = kageFileFromCode("returnOne(1, \"asdf\", 1.2)")
        val expected = kageFileFromLines(KGInvocation(KGBindingReference("returnOne"), listOf(intLiteral(1), stringLiteral("asdf"), decLiteral(1.2))))
        assertEquals(expected, kageFile)
    }
}