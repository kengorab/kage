package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGTypeTag.INT
import co.kenrg.kagelang.tree.types.KGTypeTag.UNIT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PrintTypeCheckerTests {
    @Test fun typechecksPrintStatement_returnsUnit() {
        val printStatement = KGPrint(KGBinary(KGLiteral(INT, 1), "+", KGLiteral(INT, 1)))
        val result = TypeChecker.typeCheck(printStatement, randomTCNamespace())
        assertSucceedsAnd(result) { assertEquals(UNIT, it.type) }
    }

    @Test fun testPrintStatement_innerExpressionFailsTypecheck_failsTypecheck() {
        val printStatement = KGPrint(KGBinary(KGLiteral(INT, 1), "&&", KGLiteral(INT, 1)))
        val result = TypeChecker.typeCheck(printStatement, randomTCNamespace())
        assertFails(result)
    }
}