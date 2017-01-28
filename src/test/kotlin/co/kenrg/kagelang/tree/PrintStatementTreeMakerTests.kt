package co.kenrg.kagelang.tree

import co.kenrg.kagelang.codegen.*
import co.kenrg.kagelang.kageFileFromCode
import co.kenrg.kagelang.kageFileFromLines
import co.kenrg.kagelang.tree.KGTree.KGPrint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class PrintStatementTreeMakerTests {
    @TestFactory
    fun testParseAndTransformPrintStatement_literals(): List<DynamicTest> {
        data class Case(val repr: String, val statement: KGTree.KGStatement)

        return listOf(
                Case("print(1)", KGPrint(intLiteral(1))),
                Case("print(3.14)", KGPrint(decLiteral(3.14))),
                Case("print(true)", KGPrint(trueLiteral())),
                Case("print(false)", KGPrint(falseLiteral())),
                Case("print(\"hello world\")", KGPrint(stringLiteral("hello world")))
        ).map { testCase ->
            val (repr, expr) = testCase

            dynamicTest("The statement `$repr` should be correctly mapped to its tree structure") {
                val kageFile = kageFileFromCode(repr)
                val expected = kageFileFromLines(expr)
                assertEquals(expected, kageFile)
            }
        }
    }
}