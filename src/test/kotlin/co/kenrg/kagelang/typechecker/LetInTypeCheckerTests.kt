package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.*
import co.kenrg.kagelang.model.FnParameter
import co.kenrg.kagelang.model.Signature
import co.kenrg.kagelang.model.TypeIdentifier
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.tree.types.StdLibType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class LetInTypeCheckerTests {

    @Test fun testLetInExpression_bodyIsPrintStatement_typechecksToUnit() {
        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", intLiteral(1))),
                KGPrint(KGBindingReference("a"))
        )
        val result = TypeChecker.typeCheck(letInExpr, randomTCNamespace())
        assertSucceedsAnd(result) { assertEquals(KGType.UNIT, it.type) }
    }

    @Test fun testLetInExpression_bindingUsesNamespaceVal_passesTypechecking() {
        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", KGBinary(intLiteral(1), "+", KGBindingReference("outerVal")))),
                KGPrint(KGBindingReference("a"))
        )
        val ns = randomTCNamespace()
        ns.rootScope.vals.put("outerVal", TCBinding.StaticValBinding("outerVal", KGType.INT))
        val result = TypeChecker.typeCheck(letInExpr, ns)
        assertSucceedsAnd(result) { assertEquals(KGType.UNIT, it.type) }
    }

    @Test fun testLetInExpression_bodyUsesNamespaceVal_passesTypechecking() {
        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", intLiteral(1))),
                KGPrint(KGBinary(KGBindingReference("a"), "+", KGBindingReference("outerVal")))
        )
        val ns = randomTCNamespace()
        ns.rootScope.vals.put("outerVal", TCBinding.StaticValBinding("outerVal", KGType.INT))
        val result = TypeChecker.typeCheck(letInExpr, ns)
        assertSucceedsAnd(result) { assertEquals(KGType.UNIT, it.type) }
    }

    @Test fun testLetInExpression_bindingUsesNamespaceFn_passesTypechecking() {
        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", KGInvocation(KGBindingReference("outerFn")))),
                KGPrint(KGBindingReference("a"))
        )
        val ns = randomTCNamespace()
        ns.rootScope.functions.put("outerFn", TCBinding.FunctionBinding("outerFn", Signature(returnType = KGType.INT)))
        val result = TypeChecker.typeCheck(letInExpr, ns)
        assertSucceedsAnd(result) { assertEquals(KGType.UNIT, it.type) }
    }

    @Test fun testLetInExpression_bodyUsesNamespaceFn_passesTypechecking() {
        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", intLiteral(1))),
                KGPrint(KGBinary(KGBindingReference("a"), "+", KGInvocation(KGBindingReference("outerFn"))))
        )
        val ns = randomTCNamespace()
        ns.rootScope.functions.put("outerFn", TCBinding.FunctionBinding("outerFn", Signature(returnType = KGType.INT)))
        val result = TypeChecker.typeCheck(letInExpr, ns)
        assertSucceedsAnd(result) { assertEquals(KGType.UNIT, it.type) }
    }

    @Test fun testLetInExpression_valBindingReusesNameFromOuterScope_bodyUsesLocalBindingInsteadOfOuterScope() {
        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", intLiteral(1))),
                KGBinary(KGBindingReference("a"), "+", intLiteral(1))
        )
        val ns = randomTCNamespace()
        ns.rootScope.vals.put("a", TCBinding.StaticValBinding("a", KGType.DEC))
        val result = TypeChecker.typeCheck(letInExpr, ns)

        // Assert type is INT, because if it used the outer `a` instead of the local `a`, it'd be DEC.
        assertSucceedsAnd(result) { assertEquals(KGType.INT, it.type) }
    }

    @Test fun testLetInExpression_fnBindingReusesNameFromOuterScope_bodyUsesLocalBindingInsteadOfOuterScope() {
        val letInExpr = KGLetIn(
                listOf(KGFnDeclaration("a", intLiteral(1))),
                KGBinary(KGInvocation(KGBindingReference("a")), "+", intLiteral(1))
        )
        val ns = randomTCNamespace()
        ns.rootScope.functions.put("a", TCBinding.FunctionBinding("a", Signature(returnType = KGType.DEC)))
        val result = TypeChecker.typeCheck(letInExpr, ns)

        // Assert type is INT, because if it used the outer `a` instead of the local `a`, it'd be DEC.
        assertSucceedsAnd(result) { assertEquals(KGType.INT, it.type) }
    }

    @TestFactory
    fun testLetInExpression_testBindings_bodyIsBinaryExpression_exprHasCorrectType(): List<DynamicTest> {
        data class Case(val repr: String, val expr: KGTree.KGExpression, val exprType: KGType)

        return listOf(
                Case(
                        "let\nval a = 1\nin\na + 1",
                        KGLetIn(listOf(KGValDeclaration("a", intLiteral(1))), KGBinary(KGBindingReference("a"), "+", intLiteral(1))),
                        KGType.INT
                ),
                Case(
                        "let\nval a = 1.1\nin\na + 1",
                        KGLetIn(listOf(KGValDeclaration("a", decLiteral(1.0))), KGBinary(KGBindingReference("a"), "+", intLiteral(1))),
                        KGType.DEC
                ),
                Case(
                        "let\nval t = true\nin\na || false",
                        KGLetIn(listOf(KGValDeclaration("a", trueLiteral())), KGBinary(KGBindingReference("a"), "||", falseLiteral())),
                        KGType.BOOL
                ),
                Case(
                        "let\nval a = \"hello\"\nin\na ++ \" world\"",
                        KGLetIn(listOf(KGValDeclaration("a", stringLiteral("hello"))), KGBinary(KGBindingReference("a"), "++", stringLiteral(" world"))),
                        KGType.STRING
                )
        ).map { testCase ->
            val (repr, innerExpr, exprType) = testCase

            dynamicTest("The expression `$repr`, should have type $exprType") {
                val result = TypeChecker.typeCheck(innerExpr, randomTCNamespace())
                assertSucceedsAnd(result) { assertEquals(exprType, it.type) }
            }
        }
    }

    @Test fun testLetInExpression_bindingUsesOtherBinding_succeedsTypecheck() {
        val letInExpr = KGLetIn(
                listOf(
                        KGValDeclaration("a", intLiteral(1)),
                        KGValDeclaration("b", KGBinary(intLiteral(1), "+", KGBindingReference("a")))
                ),
                KGBindingReference("b")
        )
        val result = TypeChecker.typeCheck(letInExpr, randomTCNamespace())
        assertSucceedsAnd(result) { assertEquals(KGType.INT, it.type) }
    }

    @Test fun testLetInExpression_bodyUsesUndefinedBinding_failsTypecheck() {
        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", intLiteral(1))),
                KGPrint(KGBindingReference("b"))
        )
        val result = TypeChecker.typeCheck(letInExpr, randomTCNamespace())
        assertFails(result)
    }

    @Test fun testLetInExpression_duplicateBindings_failsTypecheck() {
        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", intLiteral(1)), KGValDeclaration("a", intLiteral(2))),
                KGPrint(KGBindingReference("a"))
        )
        val result = TypeChecker.typeCheck(letInExpr, randomTCNamespace())
        assertFails(result)
    }

    @Test fun testLetInExpression_fnDeclWithParams_bindingDuplicatesParamName_failsTypecheck() {
        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", intLiteral(1)), KGValDeclaration("a", intLiteral(2))),
                KGPrint(KGBindingReference("a"))
        )
        val fnDecl = KGFnDeclaration("abc", letInExpr, listOf(FnParameter("a", TypeIdentifier("String"))))
        val result = TypeChecker.typeCheck(fnDecl, randomTCNamespace())
        assertFails(result)
    }

    @Test fun testLetInExpression_returnsPair_passesTypecheckWithPairType() {
        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", intLiteral(1)), KGValDeclaration("b", stringLiteral("asdf"))),
                KGTuple(listOf(KGBindingReference("a"), KGBindingReference("b")))
        )
        val result = TypeChecker.typeCheck(letInExpr, randomTCNamespace())
        assertSucceedsAnd(result) {
            assertEquals(KGType.stdLibType(StdLibType.Pair, typeParams = listOf(KGType.INT, KGType.STRING)), it.type)
        }
    }
}