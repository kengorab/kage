package co.kenrg.kagelang.typechecker.stdlib

import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.tree.KGTree.KGDot
import co.kenrg.kagelang.tree.KGTree.KGTuple
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.typechecker.TypeChecker
import co.kenrg.kagelang.typechecker.assertFails
import co.kenrg.kagelang.typechecker.assertSucceedsAnd
import co.kenrg.kagelang.typechecker.randomTCNamespace
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

class TuplePropsTypeCheckerTests {

    private fun testCases(cases: List<Pair<String, KGType?>>, tuple: KGTuple, tupleSig: String): List<DynamicTest> {
        return cases.map { testCase ->
            val (propName, expectedType) = testCase

            if (expectedType == null) {
                dynamicTest("Accessing prop `$propName` of type $tupleSig should fail typechecking") {
                    val propAccess = KGDot(tuple, propName)
                    val result = TypeChecker.typeCheck(propAccess, randomTCNamespace())
                    assertFails(result)
                }
            } else {
                dynamicTest("Accessing prop `$propName` of type $tupleSig should pass typechecking with type ${expectedType.name}") {
                    val propAccess = KGDot(tuple, propName)
                    val result = TypeChecker.typeCheck(propAccess, randomTCNamespace())
                    assertSucceedsAnd(result) {
                        assertEquals(expectedType, it.type)
                        assertEquals(expectedType, propAccess.type)
                    }
                }
            }
        }
    }

    @TestFactory
    fun testPairPropAccess(): List<DynamicTest> {
        val pair = KGTuple(listOf(stringLiteral("Hello"), stringLiteral("World")))
        val pairSig = "Pair[String, String]"

        val cases = listOf(
                Pair("first", KGType.STRING),
                Pair("second", KGType.STRING),
                Pair("fst", null)
        )
        return testCases(cases, pair, pairSig)
    }

    @TestFactory
    fun testTriplePropAccess(): List<DynamicTest> {
        val triple = KGTuple(listOf(stringLiteral("Hello"), intLiteral(24), stringLiteral("World")))
        val tripleSig = "Triple[String, Int, String]"

        val cases = listOf(
                Pair("first", KGType.STRING),
                Pair("second", KGType.INT),
                Pair("third", KGType.STRING),
                Pair("thd", null)
        )
        return testCases(cases, triple, tripleSig)
    }

    @TestFactory
    fun testTuple4PropAccess(): List<DynamicTest> {
        val triple = KGTuple(listOf(stringLiteral("Hello"), intLiteral(24), stringLiteral("World"), intLiteral(48)))
        val tripleSig = "Tuple4[String, Int, String, Int]"

        val cases = listOf(
                Pair("_1", KGType.STRING),
                Pair("_2", KGType.INT),
                Pair("_3", KGType.STRING),
                Pair("_4", KGType.INT),
                Pair("first", null))
        return testCases(cases, triple, tripleSig)
    }

    @TestFactory
    fun testTuple5PropAccess(): List<DynamicTest> {
        val triple = KGTuple(listOf(
                stringLiteral("Hello"), intLiteral(24), stringLiteral("World"), intLiteral(48), stringLiteral("!")
        ))
        val tripleSig = "Tuple5[String, Int, String, Int, String]"

        val cases = listOf(
                Pair("_1", KGType.STRING),
                Pair("_2", KGType.INT),
                Pair("_3", KGType.STRING),
                Pair("_4", KGType.INT),
                Pair("_5", KGType.STRING),
                Pair("first", null))
        return testCases(cases, triple, tripleSig)
    }

    @TestFactory
    fun testTuple6PropAccess(): List<DynamicTest> {
        val triple = KGTuple(listOf(
                stringLiteral("Hello"), intLiteral(24), stringLiteral("World"), intLiteral(48), stringLiteral("!"), intLiteral(1)
        ))
        val tripleSig = "Tuple6[String, Int, String, Int, String, Int]"

        val cases = listOf(
                Pair("_1", KGType.STRING),
                Pair("_2", KGType.INT),
                Pair("_3", KGType.STRING),
                Pair("_4", KGType.INT),
                Pair("_5", KGType.STRING),
                Pair("_6", KGType.INT),
                Pair("first", null))
        return testCases(cases, triple, tripleSig)
    }
}