package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.model.FnParameter
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
}

