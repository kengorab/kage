package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.decLiteral
import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.tree.KGTree.KGBinary
import co.kenrg.kagelang.tree.KGTree.KGValDeclaration
import co.kenrg.kagelang.tree.types.KGTypeTag
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class TypeAnnotationsTypeCheckerTests {

    @TestFactory
    fun typecheckLiterals_typeAnnotationsCorrect_passesTypechecking(): List<DynamicTest> {
        return listOf(KGTypeTag.INT, KGTypeTag.DEC, KGTypeTag.BOOL, KGTypeTag.STRING).map { litType ->
            dynamicTest("Val declaration of type $litType with type annotation $litType should pass typechecking") {
                val valDecl = KGValDeclaration("a", randomKGLiteralOfType(litType), litType)
                val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
                assertSucceeds(result)
            }
        }
    }

    @TestFactory
    fun typecheckExpressions_typeCheckAnnotationsCorrect_passesTypechecking(): List<DynamicTest> {
        val numericTypeTests = listOf(KGTypeTag.INT, KGTypeTag.DEC).map { type ->
            dynamicTest("Val declaration of expression of type $type, with type annotation $type should pass typechecking") {
                val valDecl = KGValDeclaration("a", KGBinary(randomKGLiteralOfType(type), "+", randomKGLiteralOfType(type)), type)
                val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
                assertSucceeds(result)
            }
        }

        val booleanTypeTest =
                dynamicTest("Val declaration of expression of type BOOL, with type annotation BOOL should pass typechecking") {
                    val valDecl = KGValDeclaration("a", KGBinary(randomKGLiteralOfType(KGTypeTag.BOOL), "&&", randomKGLiteralOfType(KGTypeTag.BOOL)), KGTypeTag.BOOL)
                    val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
                    assertSucceeds(result)
                }
        val stringTypeTest =
                dynamicTest("Val declaration of expression of type STRING, with type annotation STRING should pass typechecking") {
                    val valDecl = KGValDeclaration("a", KGBinary(randomKGLiteralOfType(KGTypeTag.STRING), "++", randomKGLiteralOfType(KGTypeTag.STRING)), KGTypeTag.STRING)
                    val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
                    assertSucceeds(result)
                }

        return numericTypeTests + booleanTypeTest + stringTypeTest
    }

    @TestFactory
    fun typecheckLiterals_typeAnnotationsIncorrect_failsTypechecking(): List<DynamicTest> {
        val types = listOf(KGTypeTag.INT, KGTypeTag.DEC, KGTypeTag.BOOL, KGTypeTag.STRING)
        return types.flatMap { litType ->
            val otherTypes = types.filterNot { t -> t == litType }
            otherTypes.map { badType ->
                dynamicTest("Val declaration of type $litType with type annotation $badType should fail typechecking") {
                    val valDecl = KGValDeclaration("a", randomKGLiteralOfType(litType), badType)
                    val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
                    assertFails(result)
                }
            }
        }
    }

    @Test fun typecheckStringConcatenationOfNonStrings_typeAnnotationString_passesTypechecking() {
        val valDecl = KGValDeclaration("a", KGBinary(stringLiteral("Hello, "), "++", intLiteral(24)), KGTypeTag.STRING)
        val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
        assertSucceeds(result)
    }

    @Test fun typecheckArithmeticBetweenIntAndDec_typeAnnotationInt_failsTypechecking() {
        val valDecl = KGValDeclaration("a", KGBinary(intLiteral(3), "+", decLiteral(1.24)), KGTypeTag.INT)
        val result = TypeChecker.typeCheck(valDecl, randomTCNamespace())
        assertFails(result)
    }
}