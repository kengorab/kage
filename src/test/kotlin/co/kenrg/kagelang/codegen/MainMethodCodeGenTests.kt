package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MainMethodCodeGenTests : BaseTest() {

    @Test fun testMainMethodDeclaration_doesNotRequireArgsParam() {
        val code = """
          val args = ["asdf"]
          fn main() = print(args)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("[asdf]", output)
        }
    }

    @Test fun testMainMethodDeclaration_receivesCommandLineInput() {
        val code = """
          fn main(args: Array[String]) = print(args)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileWithInputAnd(file, "hello") { output ->
            assertEquals("[hello]", output)
        }
    }

    @Test fun testMainMethodDeclaration_receivesMultipleCommandLineInput_separatedBySpace() {
        val code = """
          fn main(args: Array[String]) = print(args)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileWithInputAnd(file, "this is some input") { output ->
            assertEquals("[this, is, some, input]", output)
        }
    }

    @Test fun testMainMethodDeclaration_argsIsRenamedToAnything() {
        val code = """
          val args = "1234"
          fn main(params: Array[String]) = print(params)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileWithInputAnd(file, "hello") { output ->
            assertEquals("[hello]", output)
        }
    }

    @Test fun testMainMethodDeclaration_argsIsAnArrayOfStrings() {
        val code = """
          fn main(args: Array[String]) =
            let
              val _args: Array[String] = args
            in
              print(_args.size)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileWithInputAnd(file, "hello world") { output ->
            assertEquals("2", output)
        }
    }
}
