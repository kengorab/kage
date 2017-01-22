package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.falseLiteral
import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.codegen.trueLiteral
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGType
import org.apache.commons.lang3.RandomUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class IfThenElseTypeCheckerTests {

    @Test fun testIfThenElseExpression_conditionIsNotBool_failsTypechecking() {
        // if 3 then "not allowed"
        val ifThenExpr = KGIfThenElse(
                intLiteral(3),
                stringLiteral("not allowed")
        )
        val result = TypeChecker.typeCheck(ifThenExpr, randomTCNamespace())
        assertFails(result)
    }

    @Test fun testIfThenElseExpression_conditionIsBool_noElseAndThenIsNotUnit_failsTypechecking() {
        // if true then "not allowed"
        val ifThenExpr = KGIfThenElse(
                trueLiteral(),
                stringLiteral("not allowed")
        )
        val result = TypeChecker.typeCheck(ifThenExpr, randomTCNamespace())
        assertFails(result)
    }

    @Test fun testIfThenElseExpression_conditionIsBool_noElseAndThenIsUnit_passesTypecheckingWithTypeUnit() {
        // if true then print("okay")
        val ifThenExpr = KGIfThenElse(
                trueLiteral(),
                KGPrint(stringLiteral("okay"))
        )
        val result = TypeChecker.typeCheck(ifThenExpr, randomTCNamespace())
        assertSucceedsAnd(result) { assertEquals(KGType.UNIT, ifThenExpr.type) }
    }

    @Test fun testIfThenElseExpression_conditionIsBool_elseAndThenAreUnit_passesTypecheckingWithTypeUnit() {
        // if true then print("true") else print("false")
        val ifThenExpr = KGIfThenElse(
                trueLiteral(),
                KGPrint(stringLiteral("true")),
                KGPrint(stringLiteral("false"))
        )
        val result = TypeChecker.typeCheck(ifThenExpr, randomTCNamespace())
        assertSucceedsAnd(result) { assertEquals(KGType.UNIT, ifThenExpr.type) }
    }

    @TestFactory
    fun testIfThenElseExpression_conditionIsBool_elseIsNotSameTypeAsThen_failsTypechecking(): List<DynamicTest> {
        val types = listOf(KGType.INT, KGType.DEC, KGType.BOOL, KGType.STRING)
        return types.map { type ->
            val cond = randomKGLiteralOfType(KGType.BOOL)
            val thenBlock = randomKGLiteralOfType(type)

            val badType = (types - type)[RandomUtils.nextInt(0, types.size - 1)]
            val elseBlock = randomKGLiteralOfType(badType)

            dynamicTest("Expression `if $cond then $thenBlock else $elseBlock should fail typechecking") {
                val ifThenExpr = KGIfThenElse(cond, thenBlock, elseBlock)
                val result = TypeChecker.typeCheck(ifThenExpr, randomTCNamespace())
                assertFails(result)
            }
        }
    }

    @TestFactory
    fun testIfThenElseExpression_conditionIsBool_elseIsSameTypeAsThen_passesTypecheckingWithType(): List<DynamicTest> {
        return listOf(KGType.INT, KGType.DEC, KGType.BOOL, KGType.STRING).map { type ->
            val cond = randomKGLiteralOfType(KGType.BOOL)
            val thenBlock = randomKGLiteralOfType(type)
            val elseBlock = randomKGLiteralOfType(type)

            dynamicTest("Expression `if $cond then $thenBlock else $elseBlock should pass with type $type") {
                val ifThenExpr = KGIfThenElse(cond, thenBlock, elseBlock)
                val result = TypeChecker.typeCheck(ifThenExpr, randomTCNamespace())
                assertSucceedsAnd(result) { assertEquals(type, ifThenExpr.type) }
            }
        }
    }

    @Test fun testIfThenElseExpression_nestedIfThenElse() {
        // if true
        // then (
        //   if false
        //   then "hi"
        //   else "bye"
        // )
        // else "hello"
        val ifThenExpr = KGIfThenElse(
                trueLiteral(),
                KGParenthesized(KGIfThenElse(
                        falseLiteral(),
                        stringLiteral("hi"),
                        stringLiteral("bye")
                )),
                stringLiteral("hello")
        )
        val result = TypeChecker.typeCheck(ifThenExpr, randomTCNamespace())
        assertSucceedsAnd(result) { assertEquals(KGType.STRING, ifThenExpr.type) }
    }
}