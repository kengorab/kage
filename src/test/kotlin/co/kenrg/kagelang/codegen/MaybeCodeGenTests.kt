package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class MaybeCodeGenTests : BaseTest() {

    @Nested
    inner class None {

        @Test fun testNone_printsNone() {
            val code = """
              fn main() =
                let
                  val a: None = None()
                in
                  print(a)
            """

            val file = kageFileFromCode(code)
            compileAndExecuteFileAnd(file) { output -> assertEquals("None()", output) }
        }
    }

    @Nested
    inner class Some {
        @TestFactory
        fun testSome_print(): List<DynamicTest> {
            return listOf(
                    Pair("val a: Some[Int] = Some(12)", "Some(12)"),
                    Pair("val a: Some[Dec] = Some(1.2)", "Some(1.2)"),
                    Pair("val a: Some[Bool] = Some(true)", "Some(true)"),
                    Pair("val a: Some[String] = Some(\"asdf\")", "Some(asdf)"),
                    Pair("val a: Some[Some[Int]] = Some(Some(1))", "Some(Some(1))"),
                    Pair("val a: Some[None] = Some(None())", "Some(None())")
            ).map { testCase ->
                val (valDecl, result) = testCase
                dynamicTest("If `$valDecl`, `print(a)` should be '$result'") {
                    val code = """
                      fn main() =
                        let
                          $valDecl
                        in
                          print(a)
                    """

                    val file = kageFileFromCode(code)
                    compileAndExecuteFileAnd(file) { output -> assertEquals(result, output) }
                }
            }
        }

        @TestFactory
        fun testSome_printValue(): List<DynamicTest> {
            return listOf(
                    Pair("Some(12)", "12"),
                    Pair("Some(1.2)", "1.2"),
                    Pair("Some(true)", "true"),
                    Pair("Some(\"asdf\")", "asdf"),
                    Pair("Some(Some(1))", "Some(1)"),
                    Pair("Some(None())", "None()")
            ).map { testCase ->
                val (valDecl, result) = testCase
                dynamicTest("Printing `$valDecl.value` should be '$result'") {
                    val code = """
                      fn main() =
                        print($valDecl.value)
                    """

                    val file = kageFileFromCode(code)
                    compileAndExecuteFileAnd(file) { output -> assertEquals(result, output) }
                }
            }
        }
    }
}