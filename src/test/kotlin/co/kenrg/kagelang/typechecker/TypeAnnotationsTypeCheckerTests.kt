package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.decLiteral
import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.model.TypeIdentifier
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class TypeAnnotationsTypeCheckerTests {

    @TestFactory
    fun typecheckLiterals_typeAnnotationsCorrect_passesTypechecking(): List<DynamicTest> {
        return listOf(KGType.INT, KGType.DEC, KGType.BOOL, KGType.STRING).map { litType ->
            dynamicTest("Val declaration of type $litType with type annotation $litType should pass typechecking") {
                val valDecl = KGValDeclaration("a", randomKGLiteralOfType(litType), TypeIdentifier(litType.name))
                val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
                assertSucceeds(result)
            }
        }
    }

    @TestFactory
    fun typecheckExpressions_typeCheckAnnotationsCorrect_passesTypechecking(): List<DynamicTest> {
        val numericTypeTests = listOf(KGType.INT, KGType.DEC).map { type ->
            dynamicTest("Val declaration of expression of type $type, with type annotation $type should pass typechecking") {
                val valDecl = KGValDeclaration("a", KGBinary(randomKGLiteralOfType(type), "+", randomKGLiteralOfType(type)), TypeIdentifier(type.name))
                val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
                assertSucceeds(result)
            }
        }

        val booleanTypeTest =
                dynamicTest("Val declaration of expression of type BOOL, with type annotation BOOL should pass typechecking") {
                    val valDecl = KGValDeclaration("a", KGBinary(randomKGLiteralOfType(KGType.BOOL), "&&", randomKGLiteralOfType(KGType.BOOL)), TypeIdentifier("Bool"))
                    val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
                    assertSucceeds(result)
                }
        val stringTypeTest =
                dynamicTest("Val declaration of expression of type STRING, with type annotation STRING should pass typechecking") {
                    val valDecl = KGValDeclaration("a", KGBinary(randomKGLiteralOfType(KGType.STRING), "++", randomKGLiteralOfType(KGType.STRING)), TypeIdentifier("String"))
                    val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
                    assertSucceeds(result)
                }

        return numericTypeTests + booleanTypeTest + stringTypeTest
    }

    @TestFactory
    fun typecheckLiterals_typeAnnotationsIncorrect_failsTypechecking(): List<DynamicTest> {
        val types = listOf(KGType.INT, KGType.DEC, KGType.BOOL, KGType.STRING)
        return types.flatMap { litType ->
            val otherTypes = types.filterNot { t -> t == litType }
            otherTypes.map { badType ->
                dynamicTest("Val declaration of type $litType with type annotation $badType should fail typechecking") {
                    val valDecl = KGValDeclaration("a", randomKGLiteralOfType(litType), TypeIdentifier(badType.name))
                    val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
                    assertFails(result)
                }
            }
        }
    }

    @Nested
    inner class SuperTypes {

        @TestFactory
        fun typecheckSupertypes_AnyIsSupertypeOfAllLiteralTypes(): List<DynamicTest> {
            val types = listOf(KGType.INT, KGType.DEC, KGType.BOOL, KGType.STRING)
            return types.map { litType ->
                dynamicTest("Expression of type $litType should be assignable to val of type Any") {
                    val valDecl = KGValDeclaration("a", randomKGLiteralOfType(litType), TypeIdentifier("Any"))
                    val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
                    assertSucceeds(result)
                }
            }
        }

        @TestFactory
        fun typecheckSupertypes_AnyIsSupertypeOfParameterizedStdlibTypes(): List<DynamicTest> {
            val testCases = listOf(
                    Pair(KGInvocation(KGBindingReference("Pair"), listOf(intLiteral(3), intLiteral(4))), "Pair[Int, Int]"),
                    Pair(KGInvocation(KGBindingReference("Triple"), listOf(intLiteral(3), intLiteral(4), stringLiteral("asdf"))), "Triple[Int, Int, String]"),
                    Pair(KGInvocation(KGBindingReference("Some"), listOf(intLiteral(3))), "Some[Int]"),
                    Pair(KGInvocation(KGBindingReference("Some"), listOf(intLiteral(3))), "Maybe[Int]"),
                    Pair(KGInvocation(KGBindingReference("None"), listOf()), "None[Int]"),
                    Pair(KGInvocation(KGBindingReference("None"), listOf()), "Maybe[Int]")
            )
            return testCases.map { testCase ->
                val (expr, repr) = testCase
                dynamicTest("Expression of type $repr should be assignable to val of type Any") {
                    val valDecl = KGValDeclaration("a", expr, TypeIdentifier("Any"))
                    val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
                    assertSucceeds(result)
                }
            }
        }

        @TestFactory
        fun typecheckSupertypes_AnyIsValidSupertypeOfTypeParameters(): List<DynamicTest> {
            val testCases = listOf(
                    Triple(
                            KGInvocation(KGBindingReference("Pair"), listOf(intLiteral(3), intLiteral(4))),
                            "Pair[Int, Int]",
                            TypeIdentifier("Pair", listOf(TypeIdentifier("Any"), TypeIdentifier("Any")))
                    ),
                    Triple(
                            KGInvocation(KGBindingReference("Triple"), listOf(intLiteral(3), intLiteral(4), stringLiteral("asdf"))),
                            "Triple[Int, Int, String]",
                            TypeIdentifier("Triple", listOf(TypeIdentifier("Any"), TypeIdentifier("Any"), TypeIdentifier("Any")))
                    ),
                    Triple(
                            KGInvocation(KGBindingReference("Some"), listOf(intLiteral(3))),
                            "Some[Int]",
                            TypeIdentifier("Some", listOf(TypeIdentifier("Any")))
                    ),
                    Triple(
                            KGInvocation(KGBindingReference("Some"), listOf(intLiteral(3))),
                            "Maybe[Int]",
                            TypeIdentifier("Maybe", listOf(TypeIdentifier("Any")))
                    ),
                    Triple(
                            KGInvocation(KGBindingReference("None"), listOf()),
                            "None[Int]",
                            TypeIdentifier("None", listOf(TypeIdentifier("Any")))
                    ),
                    Triple(
                            KGInvocation(KGBindingReference("None"), listOf()),
                            "Maybe[Int]",
                            TypeIdentifier("Maybe", listOf(TypeIdentifier("Any")))
                    )
            )
            return testCases.map { testCase ->
                val (expr, repr, annotation) = testCase
                dynamicTest("Expression of type $repr should be assignable to val of type $annotation") {
                    val valDecl = KGValDeclaration("a", expr, annotation)
                    val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
                    assertSucceeds(result)
                }
            }
        }

        @TestFactory
        fun testSupertypes_MaybeIsSupertypeOfSomeAndNone(): List<DynamicTest> {
            val testCases = listOf(
                    Triple(
                            KGInvocation(KGBindingReference("Some"), listOf(intLiteral(3))),
                            "Some[Int]",
                            TypeIdentifier("Maybe", listOf(TypeIdentifier("Int")))
                    ),
                    Triple(
                            KGInvocation(KGBindingReference("Some"), listOf(intLiteral(3))),
                            "Some[Int]",
                            TypeIdentifier("Maybe", listOf(TypeIdentifier("Any")))
                    ),
                    Triple(
                            KGInvocation(KGBindingReference("None"), listOf()),
                            "None[Nothing]",
                            TypeIdentifier("Maybe", listOf(TypeIdentifier("Int")))
                    )
            )
            return testCases.map { testCase ->
                val (expr, repr, annotation) = testCase
                dynamicTest("Expression of type $repr should be assignable to val of type $annotation") {
                    val valDecl = KGValDeclaration("a", expr, annotation)
                    val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
                    assertSucceeds(result)
                }
            }
        }
    }

    @Test fun typecheckStringConcatenationOfNonStrings_typeAnnotationString_passesTypechecking() {
        val valDecl = KGValDeclaration("a", KGBinary(stringLiteral("Hello, "), "++", intLiteral(24)), TypeIdentifier("String"))
        val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
        assertSucceedsAnd(result) { assertEquals(KGType.UNIT, valDecl.type) }
    }

    @Test fun typecheckArithmeticBetweenIntAndDec_typeAnnotationInt_failsTypechecking() {
        val valDecl = KGValDeclaration("a", KGBinary(intLiteral(3), "+", decLiteral(1.24)), TypeIdentifier("Int"))
        val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
        assertFails(result)
    }

    @Test fun typecheckTypeAnnotations_annotationIsCustomTypeDefinedInNamespace_passesTypechecking() {
        val ns = randomTCNamespace()
        ns.rootScope.types.put("SomeType", KGType("SomeType", ""))
        val valDecl = KGValDeclaration("a", KGInvocation(KGBindingReference("SomeType")), TypeIdentifier("SomeType"))
        val result = TypeChecker.typeCheck(valDecl, ns)
        assertSucceeds(result)
    }

    @Test fun typecheckTypeAnnotations_typeIsStdlibPairType_passesTypechecking() {
        val valDecl = KGValDeclaration("a", KGTuple(listOf(intLiteral(1), intLiteral(2))), TypeIdentifier("Pair", listOf(TypeIdentifier("Int"), TypeIdentifier("Int"))))
        val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
        assertSucceeds(result)
    }

    @Test fun typecheckTypeAnnotations_typeIsStdlibTripleType_passesTypechecking() {
        val valDecl = KGValDeclaration("a", KGTuple(listOf(intLiteral(1), intLiteral(2), intLiteral(3))), TypeIdentifier("Triple", listOf(TypeIdentifier("Int"), TypeIdentifier("Int"), TypeIdentifier("Int"))))
        val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
        assertSucceeds(result)
    }

    @Test fun typecheckTypeAnnotations_typeIsStdlibSomeType_passesTypechecking() {
        val valDecl = KGValDeclaration("a", KGInvocation(KGBindingReference("Some"), listOf(intLiteral(1))), TypeIdentifier("Some", listOf(TypeIdentifier("Int"))))
        val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
        assertSucceeds(result)
    }

    @Test fun typecheckTypeAnnotations_typeIsStdlibSomeType_annotationIsMaybe_passesTypechecking() {
        val valDecl = KGValDeclaration("a", KGInvocation(KGBindingReference("Some"), listOf(intLiteral(1))), TypeIdentifier("Maybe", listOf(TypeIdentifier("Int"))))
        val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
        assertSucceeds(result)
    }

    @Test fun typecheckTypeAnnotations_typeIsStdlibNoneType_passesTypechecking() {
        val valDecl = KGValDeclaration("a", KGInvocation(KGBindingReference("None")), TypeIdentifier("None"))
        val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
        assertSucceeds(result)
    }

    @Test fun typecheckTypeAnnotations_typeIsStdlibNoneType_annotationIsMaybe_passesTypechecking() {
        val valDecl = KGValDeclaration("a", KGInvocation(KGBindingReference("None")), TypeIdentifier("Maybe", listOf(TypeIdentifier("Any"))))
        val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
        assertSucceeds(result)
    }
}