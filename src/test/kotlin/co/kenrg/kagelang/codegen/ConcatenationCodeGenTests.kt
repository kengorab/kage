package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConcatenationCodeGenTests : BaseTest() {

    @Test fun testConcatenatingTwoStringLiterals() {
        val code = """
          val a = "hello " ++ "world"
          fn main() = print(a)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("hello world", output)
        }
    }

    @Test fun testConcatenatingTwoStringBindings() {
        val code = """
          val hello = "hello "
          val world = "world"
          val helloWorld = hello ++ world
          fn main() = print(helloWorld)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("hello world", output)
        }
    }

    @Test fun testConcatenatingStringBindingsAndLiteral() {
        val code = """
          val hello = "hello"
          val world = "world"
          val helloWorld = hello ++ " " ++ world
          fn main() = print(helloWorld)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("hello world", output)
        }
    }

    @Test fun testConcatenatingStringBindingsAndTwoLiterals() {
        val code = """
          val hello = "hello"
          val world = "world"
          val helloWorld = hello ++ " " ++ world ++ "!"
          fn main() = print(helloWorld)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("hello world!", output)
        }
    }

    @Test fun testConcatenatingStringWithOtherType() {
        val code = """
          val trueVal = true
          val string = "Hello, " ++ trueVal ++ "!"
          fn main() = print(string)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("Hello, true!", output)
        }
    }

    @Test fun testConcatenatingStringWithOtherTypes() {
        val code = """
          val trueVal = true
          val three = 3
          val pi = 3 + 0.14
          val string = "Hello, " ++ three ++ " " ++ pi ++ trueVal
          fn main() = print(string)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("Hello, 3 3.14true", output)
        }
    }
}

