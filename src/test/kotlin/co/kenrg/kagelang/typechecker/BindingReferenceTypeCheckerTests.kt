package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.*
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.KGBinary
import co.kenrg.kagelang.tree.KGTree.KGBindingReference
import co.kenrg.kagelang.tree.types.KGTypeTag
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DynamicTest.dynamicTest

class BindingReferenceTypeCheckerTests {
    data class Case(val repr: String, val expr: KGTree.KGExpression, val exprType: KGTypeTag)

    @TestFactory
    @DisplayName("A BindingReferenceExpression's type should be that of its inner expression")
    fun typecheckBindingReference_bindingPresentInContext_bindingExprTypeIsInnerExprType(): List<DynamicTest> {
        return listOf(
                Case("1", intLiteral(1), KGTypeTag.INT),
                Case("1.12", decLiteral(1.12), KGTypeTag.DEC),
                Case("true", trueLiteral(), KGTypeTag.BOOL),
                Case("\"hello world\"", stringLiteral("hello world"), KGTypeTag.STRING),

                Case("1 + 3", KGBinary(intLiteral(1), "+", intLiteral(3)), KGTypeTag.INT),
                Case("1.4 - 3.1", KGBinary(decLiteral(1.4), "-", decLiteral(3.1)), KGTypeTag.DEC),
                Case("true || false", KGBinary(trueLiteral(), "||", falseLiteral()), KGTypeTag.BOOL)

        ).map { testCase ->
            val (repr, innerExpr, exprType) = testCase

            dynamicTest("If `val a = $repr` is in context, then the binding reference `a` should have type $exprType") {
                val ns = randomTCNamespace()
                ns.rootScope.vals.put("a", TCBinding.StaticValBinding("a", exprType))

                val bindingRef = KGBindingReference("a")
                val result = TypeChecker.typeCheck(bindingRef, ns)
                assertSucceedsAnd(result) {
                    Assertions.assertEquals(exprType, it.type)
                }
            }
        }
    }

    @Test fun typecheckingBindingReference_inBinaryExpression_binaryExpressionTypeBasedOnBindingType() {
        val ns = randomTCNamespace()
        ns.rootScope.vals.put("a", TCBinding.StaticValBinding("a", KGTypeTag.DEC))

        val binaryExprUsingBinding = KGBinary(intLiteral(2), "+", KGBindingReference("a"))
        val result = TypeChecker.typeCheck(binaryExprUsingBinding, ns)
        assertSucceedsAnd(result) {
            assertEquals(KGTypeTag.DEC, it.namespace.rootScope.vals["a"]?.type)
            assertEquals(KGTypeTag.DEC, it.type)
        }
    }

    @Test fun typecheckBindingReference_bindingNotPresentInContext_fails() {
        val bindingRef = KGBindingReference("a")
        val result = TypeChecker.typeCheck(bindingRef, namespace = randomTCNamespace())

        assertFails(result)
    }

    @Test fun typecheckBindingReference_bindingPresentInContext_bindingExpressionTypeUnset_throwsException() {
        val ns = randomTCNamespace()
        ns.rootScope.vals.put("a", TCBinding.StaticValBinding("a", KGTypeTag.UNSET))

        val bindingRef = KGBindingReference("a")
        assertThrows<IllegalStateException>(IllegalStateException::class.java) {
            TypeChecker.typeCheck(bindingRef, ns)
        }
    }
}