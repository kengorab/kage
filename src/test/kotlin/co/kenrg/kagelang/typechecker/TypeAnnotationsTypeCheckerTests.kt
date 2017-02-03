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
}