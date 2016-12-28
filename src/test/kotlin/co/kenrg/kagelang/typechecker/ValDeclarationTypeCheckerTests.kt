package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.*
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGTypeTag
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.util.*

class ValDeclarationTypeCheckerTests {
    data class Case(val repr: String, val stmt: KGTree.KGValDeclaration, val exprType: KGTypeTag)

    val cases = listOf(
            Case("val a = 1", KGValDeclaration("a", intLiteral(1)), KGTypeTag.INT),
            Case("val a = 1.123", KGValDeclaration("a", decLiteral(1.123)), KGTypeTag.DEC),
            Case("val a = true", KGValDeclaration("a", trueLiteral()), KGTypeTag.BOOL),
            Case("val a = \"hello world\"", KGValDeclaration("a", stringLiteral("hello world")), KGTypeTag.STRING),
            Case("val a = 1 + 2", KGValDeclaration("a", KGBinary(intLiteral(1), "+", intLiteral(2))), KGTypeTag.INT),
            Case("val a = 1.2 - 4.2", KGValDeclaration("a", KGBinary(decLiteral(1.2), "-", decLiteral(4.2))), KGTypeTag.DEC),
            Case("val a = true || false", KGValDeclaration("a", KGBinary(trueLiteral(), "||", falseLiteral())), KGTypeTag.BOOL)
    )

    @TestFactory
    @DisplayName("ValDeclaration statements should have type of Unit, and their inner expressions should be typed")
    fun typecheckValDeclaration_returnsUnit_innerExpressionHasTypeSet(): List<DynamicTest> {
        return cases.map { testCase ->
            val (repr, statement, exprType) = testCase

            dynamicTest("$repr should have type UNIT, and inner expression should have type $exprType") {
                val result = TypeChecker.typeCheck(statement)
                assertSucceedsAnd(result) {
                    assertEquals(exprType, statement.expression.type)
                    assertEquals(KGTypeTag.UNIT, it.type)
                }
            }
        }
    }

    @TestFactory
    @DisplayName("ValDeclaration statements cause the val to be inserted into the bindings")
    fun typecheckValDeclaration_valPresentInBindings(): List<DynamicTest> {
        return cases.map { testCase ->
            val (repr, statement, exprType) = testCase

            dynamicTest("After typechecking $repr, there should be a val `a` with type $exprType in `bindings`") {
                val result = TypeChecker.typeCheck(statement)
                assertSucceedsAnd(result) {
                    assertEquals(it.bindings["a"]?.expression?.type, exprType)
                }
            }
        }
    }

    @Test fun typecheckValDeclaration_assignValToBinding_newValHasTypeOfBinding() {
        val bindings = HashMap<String, Binding>()
        bindings.put("a", Binding.ValBinding("a", intLiteral(1).withType(KGTypeTag.INT)))

        val valDeclOfBinding = KGValDeclaration("b", KGBindingReference("a"))
        val result = TypeChecker.typeCheck(valDeclOfBinding, bindings)
        assertSucceedsAnd(result) {
            assertEquals(KGTypeTag.INT, it.bindings["b"]?.expression?.type)
        }
    }

    @Test fun typecheckValDeclaration_duplicateValDeclaration_typecheckingFails() {
        val bindings = HashMap<String, Binding>()
        bindings.put("a", Binding.ValBinding("a", intLiteral(1).withType(KGTypeTag.INT)))

        val valDecl = KGValDeclaration("a", trueLiteral())
        val result = TypeChecker.typeCheck(valDecl, bindings)

        assertFails(result)
    }
}