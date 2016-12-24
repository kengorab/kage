package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGTypeTag
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UnaryTypeCheckerTests {

    @Nested
    @DisplayName("Arithmetic negation unary operation (-)")
    inner class ArithmeticNegation() {

        @Test fun typechecksArithmeticNegation_givenInt_passes() {
            val negatedInt = KGUnary("-", KGLiteral(KGTypeTag.INT, 1))
            val result = TypeChecker.typeCheck(negatedInt)
            assertSucceedsAnd(result) { assertEquals(KGTypeTag.INT, it.type) }
        }

        @Test fun typechecksArithmeticNegation_givenInt_unaryExprTypeIsInt() {
            val negatedInt = KGUnary("-", KGLiteral(KGTypeTag.INT, 1))
            val result = TypeChecker.typeCheck(negatedInt)
            assertSucceedsAnd(result) { assertEquals(KGTypeTag.INT, negatedInt.type) }
        }

        @Test fun typechecksArithmeticNegation_givenDec_passes() {
            val negatedInt = KGUnary("-", KGLiteral(KGTypeTag.DEC, 1.34))
            val result = TypeChecker.typeCheck(negatedInt)
            assertSucceedsAnd(result) { assertEquals(KGTypeTag.DEC, it.type) }
        }

        @Test fun typechecksArithmeticNegation_givenDec_unaryExprTypeIsDec() {
            val negatedDec = KGUnary("-", KGLiteral(KGTypeTag.DEC, 1.34))
            val result = TypeChecker.typeCheck(negatedDec)
            assertSucceedsAnd(result) { assertEquals(KGTypeTag.DEC, negatedDec.type) }
        }

        @Test fun typecheckArithmeticNegation_parenthesizedNumericExpression_passes() {
            val negatedParens = KGUnary("-", KGParenthesized(KGBinary(KGLiteral(KGTypeTag.INT, 1), "+", KGLiteral(KGTypeTag.INT, 2))))
            val result = TypeChecker.typeCheck(negatedParens)
            assertSucceedsAnd(result) { assertEquals(KGTypeTag.INT, it.type) }
        }

        @Test fun typecheckArithmeticNegation_parenthesizedNumericExpression_unaryExprTypeIsParenType() {
            val negatedParens = KGUnary("-", KGBinary(KGLiteral(KGTypeTag.DEC, 1.0), "+", KGLiteral(KGTypeTag.INT, 2)))
            val result = TypeChecker.typeCheck(negatedParens)
            assertSucceedsAnd(result) { assertEquals(KGTypeTag.DEC, negatedParens.type) }
        }

        @Test fun typecheckArithmeticNegation_givenBool_fails() {
            val negatedInt = KGUnary("-", KGLiteral(KGTypeTag.BOOL, true))
            val result = TypeChecker.typeCheck(negatedInt)
            assertFails(result)
        }

        @Test fun typecheckArithmeticNegation_givenString_fails() {
            val negatedInt = KGUnary("-", KGLiteral(KGTypeTag.STRING, "hello world"))
            val result = TypeChecker.typeCheck(negatedInt)
            assertFails(result)
        }
    }

    @Nested
    @DisplayName("Boolean negation unary operation (!)")
    inner class BooleanNegation() {

        @Test fun typechecksBooleanNegation_givenBool_passes() {
            val negatedTrue = KGUnary("!", KGLiteral(KGTypeTag.BOOL, true))
            val result = TypeChecker.typeCheck(negatedTrue)
            assertSucceedsAnd(result) { assertEquals(KGTypeTag.BOOL, it.type) }
        }

        @Test fun typechecksBooleanNegation_givenBool_unaryExprTypeIsBool() {
            val negatedTrue = KGUnary("!", KGLiteral(KGTypeTag.BOOL, true))
            val result = TypeChecker.typeCheck(negatedTrue)
            assertSucceedsAnd(result) { assertEquals(KGTypeTag.BOOL, negatedTrue.type) }
        }

        @Test fun typecheckBooleanNegation_givenInt_fails() {
            val negatedInt = KGUnary("!", KGLiteral(KGTypeTag.INT, 1))
            val result = TypeChecker.typeCheck(negatedInt)
            assertFails(result)
        }

        @Test fun typecheckBooleanNegation_givenDec_fails() {
            val negatedDec = KGUnary("!", KGLiteral(KGTypeTag.DEC, 1.3))
            val result = TypeChecker.typeCheck(negatedDec)
            assertFails(result)
        }

        @Test fun typecheckBooleanNegation_givenString_fails() {
            val negatedDec = KGUnary("!", KGLiteral(KGTypeTag.STRING, "hello world"))
            val result = TypeChecker.typeCheck(negatedDec)
            assertFails(result)
        }
    }
}