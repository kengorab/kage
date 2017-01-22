package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.tree.types.KGType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class LiteralTypeCheckerTests {

    @TestFactory
    @DisplayName("Literals should be evaluated to their intrinsic type")
    fun typecheckLiterals(): List<DynamicTest> {
        return listOf(KGType.INT, KGType.DEC, KGType.BOOL, KGType.STRING).flatMap { litType ->
            listOf(
                    dynamicTest("Literal of type $litType should typecheck to type $litType") {
                        val literal = randomKGLiteralOfType(litType)
                        val result = TypeChecker.typeCheck(literal, randomTCNamespace())
                        assertSucceedsAnd(result) { assertEquals(litType, it.type) }
                    },
                    dynamicTest("Literal of type $litType be attributed with type $litType") {
                        val literal = randomKGLiteralOfType(litType)
                        val result = TypeChecker.typeCheck(literal, randomTCNamespace())
                        assertSucceedsAnd(result) { assertEquals(litType, literal.type) }
                    }
            )
        }
    }
}
