package co.kenrg.kagelang.tree

import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.tree.KGTree.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LetInExpressionTreeMakerTests {

    @Test fun testParseLetInExpression_oneBinding() {
        val kageFile = kageFileFromCode("let\nval a = 1\nin\n1 + 1")
        val expected = kageFileFromLines(KGLetIn(
                listOf(KGValDeclaration("a", intLiteral(1))),
                KGBinary(intLiteral(1), "+", intLiteral(1)))
        )
        assertEquals(expected, kageFile)
    }

    @Test fun testParseLetInExpression_manyBindings() {
        val kageFile = kageFileFromCode("let\nval a = 1\nval b = 3\nin\n1 + 1")
        val expected = kageFileFromLines(KGLetIn(
                listOf(KGValDeclaration("a", intLiteral(1)), KGValDeclaration("b", intLiteral(3))),
                KGBinary(intLiteral(1), "+", intLiteral(1)))
        )
        assertEquals(expected, kageFile)
    }

    @Test fun testParseLetInExpression_manyBindingsOfInts_useBindingsInBody() {
        val kageFile = kageFileFromCode("let\nval a = 1\nval b = 3\nin\na + b")
        val expected = kageFileFromLines(KGLetIn(
                listOf(KGValDeclaration("a", intLiteral(1)), KGValDeclaration("b", intLiteral(3))),
                KGBinary(KGBindingReference("a"), "+", KGBindingReference("b")))
        )
        assertEquals(expected, kageFile)
    }

    @Test fun testParseLetInExpression_manyBindingsNestedLetIn_useBindingsInBody() {
        val code = """let
                     |  val a = let
                     |      val b = 11
                     |    in
                     |      b + 1
                     |in
                     |  a * 2
                     |""".trimMargin("|")
        val kageFile = kageFileFromCode(code)
        val expected = kageFileFromLines(KGLetIn(
                listOf(KGValDeclaration("a", KGLetIn(
                        listOf(KGValDeclaration("b", intLiteral(11))),
                        KGBinary(KGBindingReference("b"), "+", intLiteral(1))))
                ),
                KGBinary(KGBindingReference("a"), "*", intLiteral(2)))
        )
        assertEquals(expected, kageFile)
    }

    @Test fun testParseLetInExpression_oneBinding_bodyIsStatement() {
        val kageFile = kageFileFromCode("let\nval a = 1\nin\nprint(a)")
        val expected = kageFileFromLines(KGLetIn(
                listOf(KGValDeclaration("a", intLiteral(1))),
                KGPrint(KGBindingReference("a")))
        )
        assertEquals(expected, kageFile)
    }
}