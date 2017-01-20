package co.kenrg.kagelang.tree

import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.tree.KGTree.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class IfElseTreeMakerTests {

    @TestFactory
    fun testParseSimpleIfExpression_noElse(): List<DynamicTest> {
        return listOf(
                "if 1 > 3 then \"hello\"",
                """if 1 > 3
                  |then "hello"
                  |""".trimMargin("|")
        ).map { code ->
            dynamicTest("`$code` should be correctly mapped to a tree") {
                val kageFile = kageFileFromCode(code)
                val expected = kageFileFromLines(KGIfElse(
                        KGBinary(intLiteral(1), ">", intLiteral(3)),
                        stringLiteral("hello")
                ))
                assertEquals(expected, kageFile)
            }
        }
    }

    @TestFactory
    fun testParseSimpleIfExpression_withElse(): List<DynamicTest> {
        return listOf(
                """if 1 > 3
                  |then "hello"
                  |else "goodbye"
                  |""".trimMargin("|"),
                """if 1 > 3
                  |then "hello"
                  |else "goodbye"
                  |""".trimMargin("|")
        ).map { code ->
            dynamicTest("`$code` should be correctly mapped to a tree") {
                val kageFile = kageFileFromCode(code)
                val expected = kageFileFromLines(KGIfElse(
                        KGBinary(intLiteral(1), ">", intLiteral(3)),
                        stringLiteral("hello"),
                        stringLiteral("goodbye")
                ))
                assertEquals(expected, kageFile)
            }
        }
    }

    @Test fun testParseIfExpression_withElse_nestedExpression() {
        val code = """if fnCall() == 12
                     |then
                     |  let
                     |    val a = "hello"
                     |  in
                     |    a + 1
                     |else
                     |  2
                     |""".trimMargin("|")
        val kageFile = kageFileFromCode(code)
        val expected = kageFileFromLines(KGIfElse(
                KGBinary(KGInvocation(KGBindingReference("fnCall")), "==", intLiteral(12)),
                KGLetIn(
                        listOf(KGValDeclaration("a", stringLiteral("hello"))),
                        KGBinary(KGBindingReference("a"), "+", intLiteral(1))
                ),
                intLiteral(2)
        ))
        assertEquals(expected, kageFile)
    }

    @Test fun testParseIfExpression_danglingElseProblem() {
        // The "dangling" else is paired with the closest if clause - the if 2 > 0 clause
        val code = """if 1 > 0
                     |then
                     |  if 2 > 0
                     |  then 3
                     |  else 4
                     |""".trimMargin("|")
        val kageFile = kageFileFromCode(code)
        val expected = kageFileFromLines(KGIfElse(
                KGBinary(intLiteral(1), ">", intLiteral(0)),
                KGIfElse(
                        KGBinary(intLiteral(2), ">", intLiteral(0)),
                        intLiteral(3),
                        intLiteral(4)
                )
        ))
        assertEquals(expected, kageFile)
    }

    @Test fun testParseIfExpression_nestedIfNoElse() {
        val code = """if 1 > 0
                     |then (
                     |  if 2 > 0
                     |  then 3
                     |)
                     |else 5
                     |""".trimMargin("|")
        val kageFile = kageFileFromCode(code)
        val expected = kageFileFromLines(KGIfElse(
                KGBinary(intLiteral(1), ">", intLiteral(0)),
                KGParenthesized(
                        KGIfElse(
                                KGBinary(intLiteral(2), ">", intLiteral(0)),
                                intLiteral(3)
                        )
                ),
                intLiteral(5)
        ))
        assertEquals(expected, kageFile)
    }

    @Test fun testParseIfExpression_nestedIfElse() {
        val code = """if 1 > 0
                     |  if 2 > 0
                     |    3
                     |  else
                     |    4
                     |else
                     |  5
                     |""".trimMargin("|")
        val kageFile = kageFileFromCode(code)
        val expected = kageFileFromLines(KGIfElse(
                KGBinary(intLiteral(1), ">", intLiteral(0)),
                KGIfElse(
                        KGBinary(intLiteral(2), ">", intLiteral(0)),
                        intLiteral(3),
                        intLiteral(4)
                ),
                intLiteral(5)
        ))
        assertEquals(expected, kageFile)
    }
}