package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.decLiteral
import co.kenrg.kagelang.codegen.falseLiteral
import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.trueLiteral
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.KGBinary
import co.kenrg.kagelang.tree.KGTree.KGBindingReference
import co.kenrg.kagelang.tree.types.KGTypeTag
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DynamicTest.dynamicTest
import java.util.*

class BindingReferenceTypeCheckerTests {
    data class Case(val repr: String, val expr: KGTree.KGExpression, val exprType: KGTypeTag)

    @TestFactory
    @DisplayName("A BindingReferenceExpression's type should be that of its inner expression")
    fun typecheckBindingReference_bindingPresentInContext_bindingExprTypeIsInnerExprType(): List<DynamicTest> {
        return listOf(
                Case("1", intLiteral(1), KGTypeTag.INT),
                Case("1.12", decLiteral(1.12), KGTypeTag.DEC),
                Case("true", trueLiteral, KGTypeTag.BOOL),

                Case("1 + 3", KGBinary(intLiteral(1), "+", intLiteral(3)), KGTypeTag.INT),
                Case("1.4 - 3.1", KGBinary(decLiteral(1.4), "-", decLiteral(3.1)), KGTypeTag.DEC),
                Case("true || false", KGBinary(trueLiteral, "||", falseLiteral), KGTypeTag.BOOL)

        ).map { testCase ->
            val (repr, innerExpr, exprType) = testCase

            dynamicTest("If `val a = $repr` is in context, then the binding reference `a` should have type $exprType") {
                val bindings = HashMap<String, Binding>()
                bindings.put("a", Binding("a", innerExpr.withType(exprType)))
                // Manually add the type using `withType`; normally this would be done by running the expression through
                // the TypeCheckerAttributorVisitor, but we want to simulate that here in tests.

                val bindingRef = KGBindingReference("a")
                val result = TypeChecker.typeCheck(bindingRef, bindings)
                assertSucceedsAnd(result) {
                    Assertions.assertEquals(exprType, it.type)
                }
            }
        }
    }

    @Test fun typecheckingBindingReference_inBinaryExpression_binaryExpressionTypeBasedOnBindingType() {
        val bindings = HashMap<String, Binding>()
        bindings.put("a", Binding("a", decLiteral(1.2).withType(KGTypeTag.DEC)))

        val binaryExprUsingBinding = KGBinary(intLiteral(2), "+", KGBindingReference("a"))
        val result = TypeChecker.typeCheck(binaryExprUsingBinding, bindings)
        assertSucceedsAnd(result) {
            assertEquals(KGTypeTag.DEC, it.bindings["a"]?.expression?.type)
            assertEquals(KGTypeTag.DEC, it.type)
        }
    }

    @Test fun typecheckBindingReference_bindingNotPresentInContext_fails() {
        val bindingRef = KGBindingReference("a")
        val result = TypeChecker.typeCheck(bindingRef, bindings = HashMap())

        assertFails(result)
    }

    @Test fun typecheckBindingReference_bindingPresentInContext_bindingExpressionTypeUnset_throwsException() {
        val bindings = HashMap<String, Binding>()
        bindings.put("a", Binding("a", KGBinary(intLiteral(1), "+", intLiteral(2))))
        // The KGBinary hasn't been run through the TypeCheckerAttributorVisitor, so its type is UNSET.

        val bindingRef = KGBindingReference("a")
        assertThrows<IllegalStateException>(IllegalStateException::class.java) {
            TypeChecker.typeCheck(bindingRef, bindings)
        }
    }
}