package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.trueLiteral
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGTypeTag
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.util.*

class FnDeclarationTypeCheckerTests {

    @TestFactory
    @DisplayName("FnDeclaration statements should have type of Unit, and their inner expressions should be typed")
    fun typecheckFnDeclaration_returnsUnit_innerExpressionHasTypeSet(): List<DynamicTest> {
        return listOf(KGTypeTag.INT, KGTypeTag.DEC, KGTypeTag.BOOL, KGTypeTag.STRING).map { type ->
            val fnBody = randomKGLiteralOfType(type)
            val repr = "fn abc() = ${fnBody.value}"

            dynamicTest("$repr should have type UNIT, and inner expression should have type $type") {
                val statement = KGFnDeclaration("abc", fnBody)
                val result = TypeChecker.typeCheck(statement)
                assertSucceedsAnd(result) {
                    assertEquals(type, statement.expression.type)
                    assertEquals(KGTypeTag.UNIT, it.type)
                }
            }
        }
    }

    @TestFactory
    @DisplayName("FnDeclaration statements cause the function to be inserted into the bindings, with proper signature")
    fun typecheckFnDeclaration_noParams_fnPresentInBindingsWithProperSignature(): List<DynamicTest> {
        return listOf(KGTypeTag.INT, KGTypeTag.DEC, KGTypeTag.BOOL, KGTypeTag.STRING).map { type ->
            val fnBody = randomKGLiteralOfType(type)
            val repr = "fn abc() = ${fnBody.value}"

            dynamicTest("After typechecking `$repr`, bindings should include a function `abc` of type `() -> $type`") {
                val statement = KGFnDeclaration("abc", fnBody)
                val result = TypeChecker.typeCheck(statement)
                assertSucceedsAnd(result) {
                    val fn = it.bindings["abc"]
                    assertNotNull(fn)

                    if (fn !is Binding.FnBinding) fail("Fn binding should be of type Binding.FnBinding")

                    assertEquals(Signature(params = listOf(), returnType = type), (fn as Binding.FnBinding).signature)
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

    @Test fun typecheckFnDeclaration_duplicateFnDeclaration_typecheckingFails() {
        val bindings = HashMap<String, Binding>()
        bindings.put("a", Binding.FnBinding("a", intLiteral(1).withType(KGTypeTag.INT), Signature(params = listOf(), returnType = KGTypeTag.INT)))

        val valDecl = KGFnDeclaration("a", trueLiteral())
        val result = TypeChecker.typeCheck(valDecl, bindings)

        assertFails(result)
    }

    @Test fun typecheckFnDeclaration_duplicateValDeclaration_typecheckingFails() {
        val bindings = HashMap<String, Binding>()
        bindings.put("a", Binding.ValBinding("a", intLiteral(1).withType(KGTypeTag.INT)))

        val valDecl = KGFnDeclaration("a", trueLiteral())
        val result = TypeChecker.typeCheck(valDecl, bindings)

        assertFails(result)
    }
}