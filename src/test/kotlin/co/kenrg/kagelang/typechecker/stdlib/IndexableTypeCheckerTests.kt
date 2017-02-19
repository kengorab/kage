package co.kenrg.kagelang.typechecker.stdlib

import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.tree.KGTree.KGArray
import co.kenrg.kagelang.tree.KGTree.KGIndex
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.tree.types.StdLibType
import co.kenrg.kagelang.typechecker.TypeChecker
import co.kenrg.kagelang.typechecker.assertFails
import co.kenrg.kagelang.typechecker.assertSucceedsAnd
import co.kenrg.kagelang.typechecker.randomTCNamespace
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IndexableTypeCheckerTests {
    @Test fun testIndex_nonIndexableType_failsTypechecking() {
        val indexExpr = KGIndex(intLiteral(3), intLiteral(0))
        val result = TypeChecker.typeCheck(indexExpr, randomTCNamespace())
        assertFails(result)
    }

    @Test fun testIndexArray_byString_failsTypechecking() {
        val arrayExpr = KGArray(listOf(intLiteral(1), intLiteral(2)))
        val indexExpr = KGIndex(arrayExpr, stringLiteral("invalidIndex"))
        val result = TypeChecker.typeCheck(indexExpr, randomTCNamespace())
        assertFails(result)
    }

    @Test fun testIndexArray_byInt_passesTypecheckingWithMaybeOfArrayItemType() {
        val arrayExpr = KGArray(listOf(intLiteral(1), intLiteral(2)))
        val indexExpr = KGIndex(arrayExpr, intLiteral(0))
        val result = TypeChecker.typeCheck(indexExpr, randomTCNamespace())
        assertSucceedsAnd(result) {
            assertEquals(KGType.stdLibType(StdLibType.Maybe, listOf(KGType.INT)), indexExpr.type)
        }
    }
}