package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class TupleCodeGenTests {

    @Nested
    inner class Pair {

        @TestFactory
        fun testTupleExpression_basicPairs(): List<DynamicTest> {
            return listOf(
                    "(1, 2)",
                    "(1, 2.2)",
                    "(0.1, (2, 3))"
            ).map { testCase ->
                dynamicTest("Tuple `$testCase` should have proper type params") {
                    val code = """
                      fn main() = print($testCase)
                    """

                    val file = kageFileFromCode(code)
                    compileAndExecuteFileAnd(file) { output -> assertEquals(testCase, output) }
                }
            }
        }

        @Test fun testTupleExpression_strings() {
            val code = """
              fn main() = print(("asdf", "qwer"))
            """

            val file = kageFileFromCode(code)
            compileAndExecuteFileAnd(file) { output -> assertEquals("(asdf, qwer)", output) }
        }

        @Test fun testTupleExpression_customTypes() {
            val code = """
              type OneProp { someStr: String }
              fn main() = print((1, OneProp("asdf"))
            """

            val file = kageFileFromCode(code)
            compileAndExecuteFileAnd(file) { output -> assertEquals("(1, OneProp(someStr: \"asdf\"))", output) }
        }
    }
}