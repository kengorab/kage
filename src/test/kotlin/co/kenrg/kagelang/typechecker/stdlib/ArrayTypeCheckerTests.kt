package co.kenrg.kagelang.typechecker.stdlib

import co.kenrg.kagelang.codegen.decLiteral
import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.tree.types.KGType.PropType
import co.kenrg.kagelang.tree.types.StdLibTypes
import co.kenrg.kagelang.typechecker.TypeChecker
import co.kenrg.kagelang.typechecker.assertFails
import co.kenrg.kagelang.typechecker.assertSucceedsAnd
import co.kenrg.kagelang.typechecker.randomTCNamespace
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class ArrayTypeCheckerTests {

    @TestFactory
    fun testArrayExpression_twoItems_passesTypecheckingWithTypePairAndTypeParamsSet(): List<DynamicTest> {
        return listOf(
                Triple("[1, 2]", listOf(intLiteral(1), intLiteral(2)), KGType.INT),
                Triple("[1.1, 2.2]", listOf(decLiteral(1.1), decLiteral(2.2)), KGType.DEC),
                Triple("[\"asdf\", \"qwer\"]", listOf(stringLiteral("asdf"), stringLiteral("qwer")), KGType.STRING)
        ).map { testCase ->
            val (repr, expr, expectedInnerType) = testCase

            dynamicTest("Array `$repr` should have proper type param") {
                val arrayExpr = KGArray(expr)
                val result = TypeChecker.typeCheck(arrayExpr, randomTCNamespace())
                assertSucceedsAnd(result) {
                    val arrayType = KGType.stdLibType(StdLibTypes.Array, listOf(expectedInnerType))
                    Assertions.assertEquals(arrayType, arrayExpr.type)
                }
            }
        }
    }

    @Test fun testArrayExpression_nestedArrays_matchingTypes_passesTypechecking() {
        // [[1, 2], [3, 4]]
        val arrayExpr = KGArray(listOf(
                KGArray(listOf(intLiteral(1), intLiteral(2))),
                KGArray(listOf(intLiteral(3), intLiteral(4)))
        ))
        val result = TypeChecker.typeCheck(arrayExpr, randomTCNamespace())
        assertSucceedsAnd(result) {
            val innerArrayType = KGType.stdLibType(StdLibTypes.Array, listOf(KGType.INT))
            val arrayType = KGType.stdLibType(StdLibTypes.Array, listOf(innerArrayType))
            Assertions.assertEquals(arrayType, arrayExpr.type)
        }
    }

    @Test fun testArrayExpression_nestedArrays_differentTypes_failsTypechecking() {
        // [[1, 2], ["3", "4"]]
        val arrayExpr = KGArray(listOf(
                KGArray(listOf(intLiteral(1), intLiteral(2))),
                KGArray(listOf(stringLiteral("3"), stringLiteral("4")))
        ))
        val result = TypeChecker.typeCheck(arrayExpr, randomTCNamespace())
        assertFails(result)
    }

    @Test fun testArrayExpression_customTypes() {
        val ns = randomTCNamespace()

        val typeName = "OneProp"
        val type = KGType(
                typeName,
                className = "${ns.name}\$$typeName",
                props = mapOf(
                        "someStr" to PropType(KGType.STRING, false)
                )
        )

        ns.rootScope.types.put(typeName, type)

        val arrayExpr = KGArray(listOf(
                KGInvocation(KGBindingReference("OneProp"), listOf(stringLiteral("asdf"))),
                KGInvocation(KGBindingReference("OneProp"), listOf(stringLiteral("qwer")))
        ))
        val result = TypeChecker.typeCheck(arrayExpr, ns)
        assertSucceedsAnd(result) {
            val arrayType = KGType.stdLibType(StdLibTypes.Array, listOf(type))
            Assertions.assertEquals(arrayType, arrayExpr.type)
        }
    }
}