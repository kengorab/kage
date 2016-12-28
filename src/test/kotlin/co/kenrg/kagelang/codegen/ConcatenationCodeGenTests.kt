package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

/*
    Because of the way these codegen tests are set up, it's difficult to test val declaration on its own. The codegen
    tests run by wrapping the expressions in `print` statements, and then by generating and executing the resulting class
    and verifying the output. So this test suite will test val declarations AND binding references.
 */
class ConcatenationCodeGenTests : BaseTest() {

    @Test fun testConcatenatingTwoStringLiterals() {
        val file = KGFile(
                statements = listOf(
                        KGValDeclaration("a", KGBinary(stringLiteral("hello "), "++", stringLiteral("world"))),
                        KGPrint(KGBindingReference("a"))
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("hello world", output)
        }
    }

    @Test fun testConcatenatingTwoStringBindings() {
        val file = KGFile(
                statements = listOf(
                        KGValDeclaration("hello", stringLiteral("hello ")),
                        KGValDeclaration("world", stringLiteral("world")),
                        KGValDeclaration("helloWorld", KGBinary(KGBindingReference("hello"), "++", KGBindingReference("world"))),
                        KGPrint(KGBindingReference("helloWorld"))
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("hello world", output)
        }
    }

    @Test fun testConcatenatingStringBindingsAndLiteral() {
        val file = KGFile(
                statements = listOf(
                        KGValDeclaration("hello", stringLiteral("hello")),
                        KGValDeclaration("world", stringLiteral("world")),
                        KGValDeclaration("helloWorld", KGBinary(KGBinary(KGBindingReference("hello"), "++", stringLiteral(" ")), "++", KGBindingReference("world"))),
                        KGPrint(KGBindingReference("helloWorld"))
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("hello world", output)
        }
    }

    @Test fun testConcatenatingStringBindingsAndTwoLiterals() {
        val file = KGFile(
                statements = listOf(
                        KGValDeclaration("hello", stringLiteral("hello")),
                        KGValDeclaration("world", stringLiteral("world")),
                        KGValDeclaration("helloWorld", KGBinary(KGBinary(KGBinary(KGBindingReference("hello"), "++", stringLiteral(" ")), "++", KGBindingReference("world")), "++", stringLiteral("!"))),
                        KGPrint(KGBindingReference("helloWorld"))
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("hello world!", output)
        }
    }

    @Test fun testConcatenatingStringWithOtherType() {
        val file = KGFile(
                statements = listOf(
                        KGValDeclaration("trueVal", trueLiteral()),
                        KGValDeclaration("string", KGBinary(KGBinary(stringLiteral("Hello, "), "++", KGBindingReference("trueVal")), "++", stringLiteral("!"))),
                        KGPrint(KGBindingReference("string"))
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("Hello, true!", output)
        }
    }

    @Test fun testConcatenatingStringWithOtherTypes() {
        val file = KGFile(
                statements = listOf(
                        KGValDeclaration("true", trueLiteral()),
                        KGValDeclaration("three", intLiteral(3)),
                        KGValDeclaration("pi", KGBinary(intLiteral(3), "+", decLiteral(0.14))),
                        KGValDeclaration("string", KGBinary(KGBinary(KGBinary(KGBinary(stringLiteral("Hello, "), "++", KGBindingReference("three")), "++", stringLiteral(" ")), "++", KGBindingReference("pi")), "++", KGBindingReference("true"))),
                        KGPrint(KGBindingReference("string"))
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("Hello, 3 3.14true", output)
        }
    }
}
