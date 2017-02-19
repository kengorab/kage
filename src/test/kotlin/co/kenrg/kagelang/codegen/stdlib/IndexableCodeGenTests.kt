package co.kenrg.kagelang.typechecker.stdlib

import co.kenrg.kagelang.codegen.BaseTest
import co.kenrg.kagelang.codegen.compileAndExecuteFileAnd
import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IndexableCodeGenTests : BaseTest() {
    @Test fun testIndexArrayByInt_indexWithinRange_printItemAtIndex() {
        val code = """
          fn main() = print(["asdf", "qwer"][0])
        """

        val file = kageFileFromCode(code)
        compileAndExecuteFileAnd(file) { output -> assertEquals("Some(asdf)", output) }
    }

    @Test fun testIndexArrayByInt_indexNotWithinRange_printNone() {
        val code = """
          fn main() = print(["asdf", "qwer"][2])
        """

        val file = kageFileFromCode(code)
        compileAndExecuteFileAnd(file) { output -> assertEquals("None()", output) }
    }

    @Test fun testIndexArrayByInt_nested() {
        val code = """
          fn main() =
            let
              val nestedArrays: Array[Array[Int]] = [[1, 2], [3, 4], [5, 6]]
            in
              print(nestedArrays[0])
        """

        val file = kageFileFromCode(code)
        compileAndExecuteFileAnd(file) { output -> assertEquals("Some([1, 2])", output) }
    }
}