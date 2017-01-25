package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.model.FnParameter
import co.kenrg.kagelang.model.TypedName
import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

class TypeDeclarationAndCreationCodeGenTests : BaseTest() {

    @Test fun testTypeDeclarationAndInstantiation_emptyType_printAnInstance() {
        // type Empty
        // fn main(...) = print(Empty())
        val file = KGFile(
                statements = listOf(
                        KGTypeDeclaration("Empty"),
                        wrapInMainMethod(KGPrint(KGInvocation(KGBindingReference("Empty"))))
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("Empty()", output)
        }
    }

    @Test fun testTypeDeclarationAndInstantiation_emptyType_storeInBinding() {
        // type Empty
        // fn main(...) =
        //   let
        //     val e: Empty = Empty()
        //   in
        //     print(e)
        val file = KGFile(
                statements = listOf(
                        KGTypeDeclaration("Empty"),
                        wrapInMainMethod(
                                KGLetIn(
                                        listOf(KGValDeclaration("e", KGInvocation(KGBindingReference("Empty")), "Empty")),
                                        KGPrint(KGBindingReference("e"))
                                )
                        )
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("Empty()", output)
        }
    }

    @Test fun testTypeDeclarationAndInstantiation_emptyType_passAsParameter() {
        // type Empty
        // fn printEmpty(e: Empty) = print(e)
        // fn main(...) =
        //   printEmpty(Empty())
        val file = KGFile(
                statements = listOf(
                        KGTypeDeclaration("Empty"),
                        KGFnDeclaration("printEmpty", KGPrint(KGBindingReference("e")), listOf(FnParameter("e", "Empty"))),
                        wrapInMainMethod(
                                KGInvocation(KGBindingReference("printEmpty"), listOf(KGInvocation(KGBindingReference("Empty"))))
                        )
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("Empty()", output)
        }
    }

    @Nested
    inner class TypeEqualsMethod {
        @Test fun testEmptyType_equalsMethod() {
            // type Empty
            // fn main(...) =
            //   let
            //     val e1 = Empty()
            //     val e2 = Empty()
            //   in
            //     print(e1 == e2)
            val file = KGFile(
                    statements = listOf(
                            KGTypeDeclaration("Empty"),
                            wrapInMainMethod(KGLetIn(
                                    listOf(
                                            KGValDeclaration("e1", KGInvocation(KGBindingReference("Empty"))),
                                            KGValDeclaration("e2", KGInvocation(KGBindingReference("Empty")))
                                    ),
                                    KGPrint(KGBinary(KGBindingReference("e1"), "==", KGBindingReference("e2")))
                            ))
                    ),
                    bindings = HashMap()
            )

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("true", output)
            }
        }

        @Test fun testTypeWithProps_equalsMethod() {
            // type Person { name: String, age: Int }
            // fn main(...) =
            //   let
            //     val p1 = Person("Meg", 24)
            //     val p2 = Person("Meg", 25)
            //   in
            //     print(p1 == p2)
            val file = KGFile(
                    statements = listOf(
                            KGTypeDeclaration("Person", listOf(TypedName("name", "String"), TypedName("age", "Int"))),
                            wrapInMainMethod(
                                    KGLetIn(
                                            listOf(
                                                    KGValDeclaration("p1", KGInvocation(KGBindingReference("Person"), listOf(stringLiteral("Meg"), intLiteral(24)))),
                                                    KGValDeclaration("p2", KGInvocation(KGBindingReference("Person"), listOf(stringLiteral("Meg"), intLiteral(25))))
                                            ),
                                            KGPrint(KGBinary(KGBindingReference("p1"), "==", KGBindingReference("p2")))
                                    )
                            )
                    ),
                    bindings = HashMap()
            )

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("false", output)
            }
        }

        @Test fun testTypeWithProps_equalsMethod_deepEquivalenceChecking() {
            // type Name { firstName: String, lastName: String }
            // type Person { name: String, age: Int }
            // fn main(...) =
            //   let
            //     val p1 = Person(Name("Meg", "Muccilli"), 24)
            //     val p2 = Person(Name("Meg", "Muccilli"), 24)
            //   in
            //     print(p1 == p2)
            val file = KGFile(
                    statements = listOf(
                            KGTypeDeclaration("Name", listOf(TypedName("firstName", "String"), TypedName("lastName", "String"))),
                            KGTypeDeclaration("Person", listOf(TypedName("name", "Name"), TypedName("age", "Int"))),
                            wrapInMainMethod(
                                    KGLetIn(
                                            listOf(
                                                    KGValDeclaration(
                                                            "p1",
                                                            KGInvocation(KGBindingReference("Person"), listOf(
                                                                    KGInvocation(KGBindingReference("Name"), listOf(stringLiteral("Meg"), stringLiteral("Muccilli"))),
                                                                    intLiteral(24)
                                                            ))
                                                    ),
                                                    KGValDeclaration(
                                                            "p2",
                                                            KGInvocation(KGBindingReference("Person"), listOf(
                                                                    KGInvocation(KGBindingReference("Name"), listOf(stringLiteral("Meg"), stringLiteral("Muccilli"))),
                                                                    intLiteral(24)
                                                            ))
                                                    )
                                            ),
                                            KGPrint(KGBinary(KGBindingReference("p1"), "==", KGBindingReference("p2")))
                                    )
                            )
                    ),
                    bindings = HashMap()
            )

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("true", output)
            }
        }
    }

    @Nested
    inner class TypeToStringMethod {
        @Test fun testTypeWithProps_toString() {
            // type Person { name: String, age: Int }
            // fn main(...) =
            //   print(Person("Meg", 24))
            val file = KGFile(
                    statements = listOf(
                            KGTypeDeclaration("Person", listOf(TypedName("name", "String"), TypedName("age", "Int"))),
                            wrapInMainMethod(
                                    KGPrint(KGInvocation(KGBindingReference("Person"), listOf(stringLiteral("Meg"), intLiteral(24))))
                            )
                    ),
                    bindings = HashMap()
            )

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("""Person(name: "Meg", age: 24)""", output)
            }
        }

        @Test fun testTypeWithProps_propIsCustomType_toStringCallsToStringOnCustomType() {
            // type Name { firstName: String, lastName: String }
            // type Person { name: Name, age: Int, married: Bool }
            // fn main(...) =
            //   print(Person(Name("Meg", "Muccilli"), 24, true))
            val file = KGFile(
                    statements = listOf(
                            KGTypeDeclaration(
                                    "Name",
                                    listOf(TypedName("firstName", "String"), TypedName("lastName", "String"))
                            ),
                            KGTypeDeclaration(
                                    "Person",
                                    listOf(TypedName("name", "Name"), TypedName("age", "Int"), TypedName("married", "Bool"))
                            ),
                            wrapInMainMethod(KGPrint(
                                    KGInvocation(
                                            KGBindingReference("Person"),
                                            listOf(
                                                    KGInvocation(
                                                            KGBindingReference("Name"),
                                                            listOf(
                                                                    stringLiteral("Meg"),
                                                                    stringLiteral("Muccilli")
                                                            )
                                                    ),
                                                    intLiteral(24),
                                                    trueLiteral()
                                            )
                                    )
                            ))
                    ),
                    bindings = HashMap()
            )

            compileAndExecuteFileAnd(file) { output ->
                assertEquals("""Person(name: Name(firstName: "Meg", lastName: "Muccilli"), age: 24, married: true)""", output)
            }
        }
    }
}
