package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.model.FnParameter
import co.kenrg.kagelang.model.TypedName
import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree.*
import org.junit.jupiter.api.Assertions.assertEquals
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

    @Test fun testTypeDeclarationAndInstantiation_emptyType_equalsMethod() {
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

    @Test fun testTypeDeclarationAndInstantiation_typeWithProps_toString() {
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

    @Test fun testTypeDeclarationAndInstantiation_typeWithProps_propIsCustomType_toStringCallsToStringOnCustomType() {
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
                                                                stringLiteral("Ryan")
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
            assertEquals("""Person(name: Name(firstName: "Meg", lastName: "Ryan"), age: 24, married: true)""", output)
        }
    }
}
