package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TypeDeclarationAndCreationCodeGenTests : BaseTest() {

    @Test fun testTypeDeclarationAndInstantiation_emptyType_printAnInstance() {
        val code = """
          type Empty
          fn main() = print(Empty())
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("Empty()", output)
        }
    }

    @Test fun testTypeDeclarationAndInstantiation_emptyType_storeInBinding() {
        val code = """
          type Empty
          fn main() =
            let
              val e: Empty = Empty()
            in
              print(e)
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("Empty()", output)
        }
    }

    @Test fun testTypeDeclarationAndInstantiation_emptyType_passAsParameter() {
        val code = """
          type Empty
          fn printEmpty(e: Empty) = print(e)
          fn main() =
            printEmpty(Empty())
        """
        val file = kageFileFromCode(code)

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("Empty()", output)
        }
    }

    @Nested
    inner class TypeEqualsMethod {
        @Test fun testEmptyType_equalsMethod() {
            val code = """
              type Empty
              fn main() =
                let
                  val e1 = Empty()
                  val e2 = Empty()
                in
                  print(e1 == e2)
            """
            val file = kageFileFromCode(code)

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("true", output)
            }
        }

        @Test fun testTypeWithProps_equalsMethod() {
            val code = """
              type Person { name: String, age: Int }
              fn main() =
                let
                  val p1 = Person("Meg", 24)
                  val p2 = Person("Meg", 25)
                in
                  print(p1 == p2)
            """
            val file = kageFileFromCode(code)

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("false", output)
            }
        }

        @Test fun testTypeWithProps_equalsMethod_deepEquivalenceChecking() {
            val code = """
              type Name { firstName: String, lastName: String }
              type Person { name: Name, age: Int }
              fn main() =
                let
                  val p1 = Person(Name("Meg", "Muccilli"), 24)
                  val p2 = Person(Name("Meg", "Muccilli"), 24)
                in
                  print(p1 == p2)
            """
            val file = kageFileFromCode(code)

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("true", output)
            }
        }
    }

    @Nested
    inner class TypeToStringMethod {
        @Test fun testTypeWithProps_toString() {
            val code = """
              type Person { name: String, age: Int }
              fn main() =
                print(Person("Meg", 24))
            """
            val file = kageFileFromCode(code)

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("""Person(name: "Meg", age: 24)""", output)
            }
        }

        @Test fun testTypeWithProps_propIsCustomType_toStringCallsToStringOnCustomType() {
            val code = """
              type Name { firstName: String, lastName: String }
              type Person { name: Name, age: Int, married: Bool }
              fn main() =
                print(Person(Name("Meg", "Muccilli"), 24, true))
            """
            val file = kageFileFromCode(code)

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("""Person(name: Name(firstName: "Meg", lastName: "Muccilli"), age: 24, married: true)""", output)
            }
        }
    }
}
