package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGType.Companion.INT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ParenthesizedTypeCheckerTests {
    @Test fun typechecksLiteralInParen_returnsTypeOfLiteral() {
        val parenthesizedLiteral = KGParenthesized(KGLiteral(INT, 1))
        val result = TypeChecker.typeCheck(parenthesizedLiteral, randomTCNamespace())
        assertSucceedsAnd(result) { assertEquals(INT, it.type) }
    }

    @Test fun typechecksBinaryInParen_returnsTypeOfBinary() {
        val parenthesizedBinary = KGParenthesized(KGBinary(KGLiteral(INT, 1), "+", KGLiteral(INT, 2)))
        val result = TypeChecker.typeCheck(parenthesizedBinary, randomTCNamespace())
        assertSucceedsAnd(result) { assertEquals(INT, it.type) }
    }

    @Test fun typechecksLiteralInParen_parenExpressionTypeIsLiteralType() {
        val parenthesizedLiteral = KGParenthesized(KGLiteral(INT, 1))
        val result = TypeChecker.typeCheck(parenthesizedLiteral, randomTCNamespace())
        assertSucceedsAnd(result) { assertEquals(INT, parenthesizedLiteral.type) }
    }

    @Test fun typechecksBinaryInParen_parenExpressionTypeIsBinaryType() {
        val parenthesizedBinary = KGParenthesized(KGBinary(KGLiteral(INT, 1), "+", KGLiteral(INT, 2)))
        val result = TypeChecker.typeCheck(parenthesizedBinary, randomTCNamespace())
        assertSucceedsAnd(result) { assertEquals(INT, parenthesizedBinary.type) }
    }
}
