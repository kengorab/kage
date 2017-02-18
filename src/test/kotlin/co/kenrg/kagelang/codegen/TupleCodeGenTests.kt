package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class TupleCodeGenTests : BaseTest() {

    @Nested
    inner class Pair {

        @TestFactory
        fun testTupleExpression_basicPairs(): List<DynamicTest> {
            return listOf(
                    Pair("(1, 2)", "(1, 2)"),
                    Pair("(1, 2.2)", "(1, 2.2)"),
                    Pair("(0.1, (2, 3))", "(0.1, (2, 3))"),
                    Pair("(\"asdf\", \"qwer\")", "(asdf, qwer)")
            ).map { testCase ->
                val (expr, result) = testCase
                dynamicTest("Tuple `$expr` should print contents") {
                    val code = """
                      fn main() = print($expr)
                    """

                    val file = kageFileFromCode(code)
                    compileAndExecuteFileAnd(file) { output -> assertEquals(result, output) }
                }
            }
        }

        @Test fun testTupleExpression_customTypes() {
            val code = """
              type OneProp { someStr: String }
              fn main() = print((1, OneProp("asdf"))
            """

            val file = kageFileFromCode(code)
            compileAndExecuteFileAnd(file) { output -> assertEquals("(1, OneProp(someStr: \"asdf\"))", output) }
        }

        @TestFactory
        fun testTupleExpression_printFirstAndSecondProps(): List<DynamicTest> {
            return listOf(
                    Pair("(1, 2)", listOf("1", "2")),
                    Pair("(1, 2.2)", listOf("1", "2.2")),
                    Pair("(0.1, (2, 3))", listOf("0.1", "(2, 3)")),
                    Pair("(\"asdf\", \"qwer\")", listOf("asdf", "qwer"))
            ).flatMap { testCase ->
                val (expr, results) = testCase
                listOf("first", "second").mapIndexed { i, prop ->
                    dynamicTest("Printing `$expr.$prop` should print ${results[i]}") {
                        val code = """
                          fn main() = print($expr.$prop)
                        """

                        val file = kageFileFromCode(code)
                        compileAndExecuteFileAnd(file) { output -> assertEquals(results[i], output) }
                    }
                }
            }
        }
    }

    @Nested
    inner class Triple {

        @TestFactory
        fun testTupleExpression_basicPairs(): List<DynamicTest> {
            return listOf(
                    Pair("(1, 2, 1)", "(1, 2, 1)"),
                    Pair("(1, 2.2, 0)", "(1, 2.2, 0)"),
                    Pair("(0.1, (2, 3), (4, 3, 0.1))", "(0.1, (2, 3), (4, 3, 0.1))"),
                    Pair("(\"asdf\", \"qwer\", \"zxcv\")", "(asdf, qwer, zxcv)")
            ).map { testCase ->
                val (expr, result) = testCase
                dynamicTest("Triple `$expr` should print contents") {
                    val code = """
                      fn main() = print($expr)
                    """

                    val file = kageFileFromCode(code)
                    compileAndExecuteFileAnd(file) { output -> assertEquals(result, output) }
                }
            }
        }

        @TestFactory
        fun testTupleExpression_printFirstSecondAndThirdProps(): List<DynamicTest> {
            return listOf(
                    Pair("(1, 2, 1)", listOf("1", "2", "1")),
                    Pair("(1, 2.2, 0)", listOf("1", "2.2", "0")),
                    Pair("(0.1, (2, 3), (4, 3, 0.1))", listOf("0.1", "(2, 3)", "(4, 3, 0.1)")),
                    Pair("(\"asdf\", \"qwer\", \"zxcv\")", listOf("asdf", "qwer", "zxcv"))
            ).flatMap { testCase ->
                val (expr, results) = testCase
                listOf("first", "second", "third").mapIndexed { i, prop ->
                    dynamicTest("Printing `$expr.$prop` should print ${results[i]}") {
                        val code = """
                          fn main() = print($expr.$prop)
                        """

                        val file = kageFileFromCode(code)
                        compileAndExecuteFileAnd(file) { output -> assertEquals(results[i], output) }
                    }
                }
            }
        }

        @Test fun testTupleExpression_customTypes() {
            val code = """
              type OneProp { someStr: String }
              fn main() = print((1, OneProp("asdf"), "qwer")
            """

            val file = kageFileFromCode(code)
            compileAndExecuteFileAnd(file) { output -> assertEquals("(1, OneProp(someStr: \"asdf\"), qwer)", output) }
        }
    }

    @Nested
    inner class Tuple4_Tuple5_Tuple6 {

        @Test
        fun testTuple_4Items() {
            val code = """
              fn main() = print((1, 2, 1, 2))
            """

            val file = kageFileFromCode(code)
            compileAndExecuteFileAnd(file) { output -> assertEquals("(1, 2, 1, 2)", output) }
        }

        @Test
        fun testTuple_5Items() {
            val code = """
              fn main() = print((1, 2, 1, 2, 1))
            """

            val file = kageFileFromCode(code)
            compileAndExecuteFileAnd(file) { output -> assertEquals("(1, 2, 1, 2, 1)", output) }
        }

        @Test
        fun testTuple_6Items() {
            val code = """
              fn main() = print((1, 2, 1, 2, 1, 2))
            """

            val file = kageFileFromCode(code)
            compileAndExecuteFileAnd(file) { output -> assertEquals("(1, 2, 1, 2, 1, 2)", output) }
        }
    }
}