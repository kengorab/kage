package co.kenrg.kagelang.codegen.stdlib

import co.kenrg.kagelang.codegen.compileAndExecuteFileAnd
import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class ArrayCodeGenTests {

    @TestFactory
    fun testArrayExpression_primitives(): List<DynamicTest> {
        return listOf(
                "[1, 2]",
                "[1.1, 2.2, 3.3]",
                "[true, false, false, true]",
                "[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24]"
        ).map { testCase ->
            dynamicTest("Array `$testCase` should print properly") {
                val code = """
                      fn main() = print($testCase)
                    """

                val file = kageFileFromCode(code)
                compileAndExecuteFileAnd(file) { output -> assertEquals(testCase, output) }
            }
        }
    }

    // TODO - When Array[Any] is supported, this test should pass
//    @Test fun testArrayExpression_emptyArray_sizeShouldBeZero() {
//        val code = """
//          fn main() = print([].size)
//        """
//
//        val file = kageFileFromCode(code)
//        compileAndExecuteFileAnd(file) { output -> assertEquals("0", output) }
//    }

    @Test fun testArrayExpression_nonEmptyArray_sizeShouldBeNumberOfItems() {
        val code = """
          fn main() = print(["asdf", "qwer"].size)
        """

        val file = kageFileFromCode(code)
        compileAndExecuteFileAnd(file) { output -> assertEquals("2", output) }
    }

    @Test fun testArrayExpression_strings() {
        val code = """
          fn main() = print(["asdf", "qwer"])
        """

        val file = kageFileFromCode(code)
        compileAndExecuteFileAnd(file) { output -> assertEquals("[asdf, qwer]", output) }
    }

    @Test fun testArrayExpression_customTypes() {
        val code = """
          type OneProp { someStr: String }
          fn main() = print([OneProp("asdf"), OneProp("qwer")])
        """

        val file = kageFileFromCode(code)
        compileAndExecuteFileAnd(file) { output ->
            assertEquals("[OneProp(someStr: \"asdf\"), OneProp(someStr: \"qwer\")]", output)
        }
    }

    @Test fun testArrayExpression_nestedArrays() {
        val code = """
          fn main() = print([
            [1, 2, 3],
            [4, 5, 6],
            [7, 8, 9]
          ])
        """

        val file = kageFileFromCode(code)
        compileAndExecuteFileAnd(file) { output ->
            assertEquals("[[1, 2, 3], [4, 5, 6], [7, 8, 9]]", output)
        }
    }
}