package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.kageFileFromCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class DotExpressionCodeGenTests : BaseTest() {

    @Nested
    inner class PropAccessor {
        @Test fun testDotExpression_printPropOfTypeInstance() {
            val code = """
              type Person { name: String }
              fn main() =
                let
                  val p = Person("Ken")
                in
                  print(p.name)
            """
            val file = kageFileFromCode(code)

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("Ken", output)
            }
        }

        @Test fun testDotExpression_printPropOfTypeInstance_twoLevels() {
            val code = """
              type Name { first: String, last: String }
              type Person { name: Name }
              fn main() =
                let
                  val p = Person(Name("Ken", "Gorab"))
                in
                  print(p.name.first)
            """
            val file = kageFileFromCode(code)

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("Ken", output)
            }
        }

        @Test fun testDotExpression_printPropOfTypeInstance_threeLevels() {
            val code = """
                type Name { firstName: String, lastName: String }
                type Person { name: Name, age: Int }
                type Language { name: String, creator: Person }

                fn main() =
                  let
                    val l = Language("kage", Person(Name("Ken", "Gorab"), 25))
                  in
                    print(l.creator.name.lastName)
            """
            val file = kageFileFromCode(code)

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("Gorab", output)
            }
        }

        @TestFactory
        fun testDotExpression_binaryOperationsWithPropAccessor(): List<DynamicTest> {
            data class Case(val printArg: String, val expected: String)

            return listOf(
                    Case("p1.name != p2.name", "true"),
                    Case("p1.name == p2.name", "false"),
                    Case("p1.name.firstName == p2.name.firstName", "false"),
                    Case("p1.name.lastName == p2.name.lastName", "true"),
                    Case("p1.age == p2.age", "false"),
                    Case("p1.age > p2.age", "true"),
                    Case("p1.age <= p2.age", "false")
            ).map { testCase ->
                val (printArg, expected) = testCase

                dynamicTest("Printing `$printArg` should result in $expected") {
                    val code = """
                        type Name { firstName: String, lastName: String }
                        type Person { name: Name, age: Int }

                        fn main() =
                          let
                            val p1 = Person(Name("Ken", "Gorab"), 25)
                            val p2 = Person(Name("Zack", "Gorab"), 22)
                          in
                            print($printArg)
                    """
                    val file = kageFileFromCode(code)
                    compileAndExecuteFileAnd(file) { output -> assertEquals(expected, output) }
                }
            }
        }
    }
}
