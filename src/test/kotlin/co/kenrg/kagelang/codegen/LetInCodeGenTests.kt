package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class LetInCodeGenTests : BaseTest() {

    @TestFactory
    fun testLetInExpression(): List<DynamicTest> {
        return listOf(
                Pair("let\nval a = 1\nin\nprint(a)", "1"),
                Pair("let\nval a = 1\nval b = a + 1\nin\nprint(b + 1)", "3")
        ).map { testCase ->
            val (repr, expected) = testCase

            dynamicTest("Running `fn main() = $repr`, should output `$expected`") {
                val code = """
                  fn main() = $repr
                """
                val file = kageFileFromCode(code)

                compileAndExecuteFileAnd(file) { output ->
                    assertEquals(expected, output)
                }
            }
        }
    }

    @Test fun testLetInExpression_bindingUsesNamespaceVal_outputsExpected() {
        val code = """
          val nsVal = 1
          fn main() =
            let
              val a = 1 + nsVal
            in
              print(a)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("2", output)
        }
    }

    @Test fun testLetInExpression_bodyUsesNamespaceVal_outputsExpected() {
        val code = """
          val nsVal = 1
          fn main() =
            let
              val a = 1
            in
              print(a + nsVal)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("2", output)
        }
    }

    @Test fun testLetInExpression_bindingUsesNamespaceFn_outputsExpected() {
        val code = """
          fn nsFn() = 1
          fn main() =
            let
              val a = nsFn()
            in
              print(a)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("1", output)
        }
    }

    @Test fun testLetInExpression_bodyUsesNamespaceFn_outputsExpected() {
        val code = """
          fn nsFn() = 1
          fn main() =
            let
              val a = 1
            in
              print(a + nsFn())
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("2", output)
        }
    }

    @Test fun testLetInExpression_valBindingReusesNameFromOuterScope_bodyUsesLocalBindingInsteadOfOuterScope() {
        val code = """
          val a = 200
          fn main() =
            let
              val a = 1
            in
              print(a + 1)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            // If `a` were read from outer scope instead of let-scope, the result would be 201.
            assertEquals("2", output)
        }
    }

    // TODO - Once functions are backed by an Object, this should be possible
//    @Test fun testLetInExpression_fnBindingReusesNameFromOuterScope_bodyUsesLocalBindingInsteadOfOuterScope() {
//        val code = """
//          fn a() = 200
//          fn main() =
//            let
//              fn a() = 1
//            in
//              print(a() + 1)
//        """
//        val file = kageFileFromCode(code)
//
//        compileAndExecuteFileAnd(file) { output ->
//            // If `a` were read from outer scope instead of let-scope, the result would be 201.
//            assertEquals("2", output)
//        }
//    }
}

