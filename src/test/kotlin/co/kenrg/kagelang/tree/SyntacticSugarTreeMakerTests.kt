package co.kenrg.kagelang.tree

import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.kageFileFromCode
import co.kenrg.kagelang.kageFileFromLines
import co.kenrg.kagelang.model.TypeIdentifier
import co.kenrg.kagelang.model.TypedName
import co.kenrg.kagelang.tree.KGTree.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SyntacticSugarTreeMakerTests {

    @Nested
    inner class Tuples {

        @Nested
        inner class ValDecl {

            @Test fun testPairTransformation() {
                val kageFile = kageFileFromCode("val a: (String, Int) = (\"Hello\", 1)")
                val expected = kageFileFromLines(KGValDeclaration(
                        "a",
                        KGTuple(
                                listOf(stringLiteral("Hello"), intLiteral(1))
                        ),
                        TypeIdentifier("Pair", listOf(
                                TypeIdentifier("String"), TypeIdentifier("Int")
                        ))
                ))
                assertEquals(expected, kageFile)
            }

            @Test fun testTripleTransformationNestedTuple() {
                val kageFile = kageFileFromCode("val a: (String, Int, (Int, String)) = (\"Hello\", 1, (2, \"2\"))")
                val expected = kageFileFromLines(KGValDeclaration(
                        "a",
                        KGTuple(
                                listOf(
                                        stringLiteral("Hello"),
                                        intLiteral(1),
                                        KGTuple(listOf(intLiteral(2), stringLiteral("2")))
                                )
                        ),
                        TypeIdentifier("Triple", listOf(
                                TypeIdentifier("String"),
                                TypeIdentifier("Int"),
                                TypeIdentifier("Pair", listOf(TypeIdentifier("Int"), TypeIdentifier("String")))
                        ))
                ))
                assertEquals(expected, kageFile)
            }

            @Test fun testTupleMoreThan6Items_fails() {
                assertThrows<UnsupportedOperationException>(UnsupportedOperationException::class.java) {
                    kageFileFromCode("val a: (Int, Int, Int, Int, Int, Int, Int) = (1, 2, 3, 4, 5, 6, 7)")
                }
            }
        }

        @Nested
        inner class FnDecl {

            @Test fun testPairTransformation() {
                val kageFile = kageFileFromCode("fn a(): (String, Int) = (\"Hello\", 1)")
                val expected = kageFileFromLines(KGFnDeclaration(
                        "a",
                        KGTuple(
                                listOf(stringLiteral("Hello"), intLiteral(1))
                        ),
                        listOf(),
                        TypeIdentifier("Pair", listOf(
                                TypeIdentifier("String"), TypeIdentifier("Int")
                        ))
                ))
                assertEquals(expected, kageFile)
            }

            @Test fun testTripleTransformationNestedTuple() {
                val kageFile = kageFileFromCode("fn a(): (String, Int, (Int, String)) = (\"Hello\", 1, (2, \"2\"))")
                val expected = kageFileFromLines(KGFnDeclaration(
                        "a",
                        KGTuple(
                                listOf(
                                        stringLiteral("Hello"),
                                        intLiteral(1),
                                        KGTuple(listOf(intLiteral(2), stringLiteral("2")))
                                )
                        ),
                        listOf(),
                        TypeIdentifier("Triple", listOf(
                                TypeIdentifier("String"),
                                TypeIdentifier("Int"),
                                TypeIdentifier("Pair", listOf(TypeIdentifier("Int"), TypeIdentifier("String")))
                        ))
                ))
                assertEquals(expected, kageFile)
            }

            @Test fun testTupleMoreThan6Items_fails() {
                assertThrows<UnsupportedOperationException>(UnsupportedOperationException::class.java) {
                    kageFileFromCode("fn a(): (Int, Int, Int, Int, Int, Int, Int) = (1, 2, 3, 4, 5, 6, 7)")
                }
            }
        }

        @Nested
        inner class TypeDecl {

            @Test fun testPairTransformation() {
                val kageFile = kageFileFromCode("type OneProp { a: (String, Int) }")
                val expected = kageFileFromLines(KGTypeDeclaration(
                        "OneProp",
                        listOf(TypedName("a", TypeIdentifier("Pair", listOf(
                                TypeIdentifier("String"), TypeIdentifier("Int"))
                        )))
                ))
                assertEquals(expected, kageFile)
            }

            @Test fun testTripleTransformationNestedTuple() {
                val kageFile = kageFileFromCode("type OneProp { a: (String, Int, (Int, Int)) }")
                val expected = kageFileFromLines(KGTypeDeclaration(
                        "OneProp",
                        listOf(TypedName("a", TypeIdentifier("Triple", listOf(
                                TypeIdentifier("String"),
                                TypeIdentifier("Int"),
                                TypeIdentifier("Pair", listOf(
                                        TypeIdentifier("Int"),
                                        TypeIdentifier("Int")
                                )))
                        )))
                ))
                assertEquals(expected, kageFile)
            }

            @Test fun testTupleMoreThan6Items_fails() {
                assertThrows<UnsupportedOperationException>(UnsupportedOperationException::class.java) {
                    kageFileFromCode("type OneProp { a: (Int, Int, Int, Int, Int, Int, Int) }")
                }
            }
        }
    }
}