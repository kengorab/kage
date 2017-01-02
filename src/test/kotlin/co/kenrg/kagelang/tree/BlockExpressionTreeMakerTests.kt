package co.kenrg.kagelang.tree

import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.tree.KGTree.KGBlock
import co.kenrg.kagelang.tree.KGTree.KGPrint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BlockExpressionTreeMakerTests {

    @Nested
    inner class ParseAndTransformBlocksOfConstantExpressions {

        @Test fun testEmptyBlock() {
            val kageFile = kageFileFromCode("print({ })")
            val expected = kageFileFromStatements(KGPrint(KGBlock(listOf())))
            assertEquals(expected, kageFile)
        }

        @Test fun testEmptyBlockNewlines() {
            val code = "{\n\n\n}"
            val kageFile = kageFileFromCode("print($code)")
            val expected = kageFileFromStatements(KGPrint(KGBlock(listOf())))
            assertEquals(expected, kageFile)
        }

        @Test fun testSingleExpressionSingleLineBlock() {
            val kageFile = kageFileFromCode("print({ 1 })")
            val expected = kageFileFromStatements(KGPrint(KGBlock(listOf(intLiteral(1)))))
            assertEquals(expected, kageFile)
        }

        @Test fun testSingleExpressionMultiLineBlock() {
            val code = "{\n 1 \n}"
            val tree = KGBlock(listOf(intLiteral(1)))
            val kageFile = kageFileFromCode("print($code)")
            val expected = kageFileFromStatements(KGPrint(tree))
            assertEquals(expected, kageFile)
        }

        @Test fun testTwoExpressionsMultiLineBlock() {
            val code = """{
                            1
                            "hello"
                          }"""
            val tree = KGBlock(listOf(
                    intLiteral(1),
                    stringLiteral("hello")
            ))
            val kageFile = kageFileFromCode("print($code)")
            val expected = kageFileFromStatements(KGPrint(tree))
            assertEquals(expected, kageFile)
        }

        @Test fun testBinaryExpressionsMultiLineBlock() {
            val code = """{
                            1 + 3
                            "hello" ++ " world"
                          }"""
            val tree = KGBlock(listOf(
                    KGTree.KGBinary(intLiteral(1), "+", intLiteral(3)),
                    KGTree.KGBinary(stringLiteral("hello"), "++", stringLiteral(" world"))
            ))
            val kageFile = kageFileFromCode("print($code)")
            val expected = kageFileFromStatements(KGPrint(tree))
            assertEquals(expected, kageFile)
        }

        @Test fun testNestedBlock() {
            val code = """{
                            1 + {
                              1 + 3
                            }
                          }"""
            val tree = KGBlock(listOf(
                    KGTree.KGBinary(
                            intLiteral(1),
                            "+",
                            KGBlock(listOf(KGTree.KGBinary(intLiteral(1), "+", intLiteral(3))))
                    )
            ))
            val kageFile = kageFileFromCode("print($code)")
            val expected = kageFileFromStatements(KGPrint(tree))
            assertEquals(expected, kageFile)
        }
    }

    @Nested
    inner class ParseAndTransformBlocksOfStatements {

        @Test fun testSingleExpressionMultiLineBlock() {
            val code = """{\n print("hello") \n}"""
            val tree = KGBlock(listOf(KGPrint(stringLiteral("hello"))))
            val kageFile = kageFileFromCode("print($code)")
            val expected = kageFileFromStatements(KGPrint(tree))
            assertEquals(expected, kageFile)
        }

        @Test fun testTwoExpressionsMultiLineBlock() {
            val code = """{
                            1
                            "hello"
                          }"""
            val tree = KGBlock(listOf(
                    intLiteral(1),
                    stringLiteral("hello")
            ))
            val kageFile = kageFileFromCode("print($code)")
            val expected = kageFileFromStatements(KGPrint(tree))
            assertEquals(expected, kageFile)
        }
    }
}