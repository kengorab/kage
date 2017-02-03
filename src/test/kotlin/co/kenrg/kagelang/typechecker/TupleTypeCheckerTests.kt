package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.decLiteral
import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.tree.types.StdLibTypes
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DynamicTest.dynamicTest

class TupleTypeCheckerTests {

    @Nested
    inner class Pair {

        @TestFactory
        fun testTupleExpression_twoItems_passesTypecheckingWithTypePairAndTypeParamsSet(): List<DynamicTest> {
            return listOf(
                    Triple("(1, 2)", listOf(intLiteral(1), intLiteral(2)), listOf(KGType.INT, KGType.INT)),
                    Triple("(1, 2.2)", listOf(intLiteral(1), decLiteral(2.2)), listOf(KGType.INT, KGType.DEC)),
                    Triple("(0.1, \"asdf\")", listOf(decLiteral(0.1), stringLiteral("asdf")), listOf(KGType.DEC, KGType.STRING))
            ).map { testCase ->
                val (repr, expr, expectedTypeParams) = testCase

                dynamicTest("Tuple `$repr` should have proper type params") {
                    val tupleExpr = KGTuple(expr)
                    val result = TypeChecker.typeCheck(tupleExpr, randomTCNamespace())
                    assertSucceedsAnd(result) {
                        val tupleType = KGType.stdLibType(StdLibTypes.Pair)
                                .copy(typeParams = expectedTypeParams)
                        Assertions.assertEquals(tupleType, tupleExpr.type)
                    }
                }
            }
        }

        @Test fun testTupleExpression_nestedTuples() {
            // (1, (2, 3))
            val tupleExpr = KGTuple(listOf(
                    intLiteral(1),
                    KGTuple(listOf(intLiteral(2), intLiteral(3)))
            ))
            val result = TypeChecker.typeCheck(tupleExpr, randomTCNamespace())
            assertSucceedsAnd(result) {
                val innerTupleType = KGType.stdLibType(StdLibTypes.Pair)
                        .copy(typeParams = listOf(KGType.INT, KGType.INT))
                val tupleType = KGType.stdLibType(StdLibTypes.Pair)
                        .copy(typeParams = listOf(KGType.INT, innerTupleType))
                Assertions.assertEquals(tupleType, tupleExpr.type)
            }
        }

        @Test fun testTupleExpression_customTypes() {
            val ns = randomTCNamespace()

            val typeName = "OneProp"
            val type = KGType(
                    typeName,
                    className = "${ns.name}\$$typeName",
                    props = mapOf(
                            "someStr" to KGType.STRING
                    )
            )

            ns.rootScope.types.put(typeName, type)

            val tupleExpr = KGTuple(listOf(
                    intLiteral(1),
                    KGInvocation(KGBindingReference("OneProp"), listOf(stringLiteral("asdf")))
            ))
            val result = TypeChecker.typeCheck(tupleExpr, ns)
            assertSucceedsAnd(result) {
                val tupleType = KGType.stdLibType(StdLibTypes.Pair)
                        .copy(typeParams = listOf(KGType.INT, type))
                Assertions.assertEquals(tupleType, tupleExpr.type)
            }
        }
    }

    @Nested
    inner class Triple {

        @TestFactory
        fun testTupleExpression_threeItems_passesTypecheckingWithTypeTripleAndTypeParamsSet(): List<DynamicTest> {
            return listOf(
                    Triple("(1, 2, 1)", listOf(intLiteral(1), intLiteral(2), intLiteral(1)), listOf(KGType.INT, KGType.INT, KGType.INT)),
                    Triple("(1, 2.2, 0)", listOf(intLiteral(1), decLiteral(2.2), intLiteral(0)), listOf(KGType.INT, KGType.DEC, KGType.INT)),
                    Triple("(0.1, \"asdf\", 3)", listOf(decLiteral(0.1), stringLiteral("asdf"), intLiteral(3)), listOf(KGType.DEC, KGType.STRING, KGType.INT))
            ).map { testCase ->
                val (repr, expr, expectedTypeParams) = testCase

                dynamicTest("Triple `$repr` should have proper type params") {
                    val tupleExpr = KGTuple(expr)
                    val result = TypeChecker.typeCheck(tupleExpr, randomTCNamespace())
                    assertSucceedsAnd(result) {
                        val tupleType = KGType.stdLibType(StdLibTypes.Triple)
                                .copy(typeParams = expectedTypeParams)
                        Assertions.assertEquals(tupleType, tupleExpr.type)
                    }
                }
            }
        }

        @Test fun testTupleExpression_nestedTuples() {
            // (1, (2, 3), (1, 2, 3))
            val tupleExpr = KGTuple(listOf(
                    intLiteral(1),
                    KGTuple(listOf(intLiteral(2), intLiteral(3))),
                    KGTuple(listOf(intLiteral(1), intLiteral(2), intLiteral(3)))
            ))
            val result = TypeChecker.typeCheck(tupleExpr, randomTCNamespace())
            assertSucceedsAnd(result) {
                val innerTupleType = KGType.stdLibType(StdLibTypes.Pair)
                        .copy(typeParams = listOf(KGType.INT, KGType.INT))
                val innerTripleType = KGType.stdLibType(StdLibTypes.Triple)
                        .copy(typeParams = listOf(KGType.INT, KGType.INT, KGType.INT))
                val tupleType = KGType.stdLibType(StdLibTypes.Triple)
                        .copy(typeParams = listOf(KGType.INT, innerTupleType, innerTripleType))
                Assertions.assertEquals(tupleType, tupleExpr.type)
            }
        }

        @Test fun testTupleExpression_customTypes() {
            val ns = randomTCNamespace()

            val typeName = "OneProp"
            val type = KGType(
                    typeName,
                    className = "${ns.name}\$$typeName",
                    props = mapOf(
                            "someStr" to KGType.STRING
                    )
            )

            ns.rootScope.types.put(typeName, type)

            val tupleExpr = KGTuple(listOf(
                    intLiteral(1),
                    KGInvocation(KGBindingReference("OneProp"), listOf(stringLiteral("asdf")))
            ))
            val result = TypeChecker.typeCheck(tupleExpr, ns)
            assertSucceedsAnd(result) {
                val tupleType = KGType.stdLibType(StdLibTypes.Pair)
                        .copy(typeParams = listOf(KGType.INT, type))
                Assertions.assertEquals(tupleType, tupleExpr.type)
            }
        }
    }

    @Nested
    inner class Tuple4_Tuple5_Tuple6 {

        @Test
        fun testTuple_4Items_passesTypecheckingWithTypeAndTypeParamsSet() {
            val tupleExpr = KGTuple(listOf(
                    intLiteral(1), intLiteral(2), intLiteral(1), intLiteral(2)
            ))
            val result = TypeChecker.typeCheck(tupleExpr, randomTCNamespace())
            assertSucceedsAnd(result) {
                val tupleType = KGType.stdLibType(StdLibTypes.Tuple4)
                        .copy(typeParams = listOf(KGType.INT, KGType.INT, KGType.INT, KGType.INT))
                Assertions.assertEquals(tupleType, tupleExpr.type)
            }
        }

        @Test
        fun testTuple_5Items_passesTypecheckingWithTypeAndTypeParamsSet() {
            val tupleExpr = KGTuple(listOf(
                    intLiteral(1), intLiteral(2), intLiteral(1), intLiteral(2), intLiteral(1)
            ))
            val result = TypeChecker.typeCheck(tupleExpr, randomTCNamespace())
            assertSucceedsAnd(result) {
                val tupleType = KGType.stdLibType(StdLibTypes.Tuple5)
                        .copy(typeParams = listOf(KGType.INT, KGType.INT, KGType.INT, KGType.INT, KGType.INT))
                Assertions.assertEquals(tupleType, tupleExpr.type)
            }
        }

        @Test
        fun testTuple_6Items_passesTypecheckingWithTypeAndTypeParamsSet() {
            val tupleExpr = KGTuple(listOf(
                    intLiteral(1), intLiteral(2), intLiteral(1), intLiteral(2), intLiteral(1), intLiteral(2)
            ))
            val result = TypeChecker.typeCheck(tupleExpr, randomTCNamespace())
            assertSucceedsAnd(result) {
                val tupleType = KGType.stdLibType(StdLibTypes.Tuple6)
                        .copy(typeParams = listOf(KGType.INT, KGType.INT, KGType.INT, KGType.INT, KGType.INT, KGType.INT))
                Assertions.assertEquals(tupleType, tupleExpr.type)
            }
        }
    }

    @Test
    fun testTuple_greaterThan6Items_failsTypechecking() {
        val tupleExpr = KGTuple(listOf(
                intLiteral(1), intLiteral(2), intLiteral(1), intLiteral(2), intLiteral(1), intLiteral(2), intLiteral(1)
        ))

        assertThrows<UnsupportedOperationException>(UnsupportedOperationException::class.java) {
            val result = TypeChecker.typeCheck(tupleExpr, randomTCNamespace())
            assertFails(result)
        }
    }
}