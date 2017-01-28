package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class IfThenElseCodeGenTests : BaseTest() {

    @Test fun testIfThenElseExpression_noElse_printInThen() {
        val code = """
          fn main() =
            if true
            then print("Hello World")
        """
        val file = kageFileFromCode(code)
        compileAndExecuteFileAnd(file) { output ->
            assertEquals("Hello World", output)
        }
    }

    @Test fun testIfThenElseExpression_printInThenAndElse_condIsTrue() {
        val code = """
          fn main() =
            if true
            then print("true statement")
            else print("false statement")
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("true statement", output)
        }
    }

    @Test fun testIfThenElseExpression_printInThenAndElse_condIsFalse() {
        val code = """
          fn main() =
            if false
            then print("true statement")
            else print("false statement")
        """
        val file = kageFileFromCode(code)
        compileAndExecuteFileAnd(file) { output ->
            assertEquals("false statement", output)
        }
    }

    @Test fun testIfThenElseExpression_bothBranchesReturnInt_condTrue_printResultOfExpression() {
        val code = """
          val number = if true then 1 else -1
          fn main() = print(number)
        """
        val file = kageFileFromCode(code)
        compileAndExecuteFileAnd(file) { output ->
            assertEquals("1", output)
        }
    }

    @Test fun testIfThenElseExpression_nestedIfThenElseExpressions() {
        val code = """
          val number = if true
            then (
              if false then 1 else -1
            )
            else 4
          fn main() = print(number)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("-1", output)
        }
    }

    @TestFactory
    fun testIfThenElseExpression_conditionIsConditional(): List<DynamicTest> {
        return listOf(
                Pair("3 > 1", "true"),
                Pair("3 >= 1", "true"),
                Pair("3 < 1", "false"),
                Pair("3 <= 1", "false"),
                Pair("3 == 1", "false"),
                Pair("3 != 1", "true")
        ).map {
            val (cond, expected) = it
            dynamicTest("The expression `if $cond then \"true\" else \"false\" should print $expected") {
                val code = """
                  fn main() = print(if $cond then "true" else "false")
                """
                val file = kageFileFromCode(code)

                compileAndExecuteFileAnd(file) { output -> assertEquals(expected, output) }
            }
        }
    }

    @Test fun testIfThenElseExpression_thenAndElseHaveLetInExpressions() {
        val code = """
            fn func() =
              if 1 > 3
                then
                  let
                    val a = 123
                  in
                    print(a)
                else
                  let
                    val b = 456
                  in
                    print(b)

            fn main() = func()
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output -> assertEquals("456", output) }
    }

    // TODO - When binding names are allowed to be duplicated between different branches, this test should pass.
    @Test fun testIfThenElseExpression_thenAndElseHaveLetInExpressions_letBindingsUseSameVariables() {
        val code = """
            fn func() =
              if 1 > 3
              then
                let
                  val a = 123
                in
                  print(a)
              else
                let
                  val a = 456
                in
                  print(a)

            fn main() = func()
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output -> assertEquals("456", output) }
    }
}
