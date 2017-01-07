package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.TC
import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.trueLiteral
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGTypeTag
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class FnDeclarationTypeCheckerTests {

    @TestFactory
    @DisplayName("FnDeclaration statements should have type of Unit, and their inner expressions should be typed")
    fun typecheckFnDeclaration_returnsUnit_innerExpressionHasTypeSet(): List<DynamicTest> {
        return listOf(KGTypeTag.INT, KGTypeTag.DEC, KGTypeTag.BOOL, KGTypeTag.STRING).map { type ->
            val fnBody = randomKGLiteralOfType(type)
            val repr = "fn abc() = ${fnBody.value}"

            dynamicTest("$repr should have type UNIT, and inner expression should have type $type") {
                val statement = KGFnDeclaration("abc", fnBody)
                val result = TypeChecker.typeCheck(statement, randomTCNamespace())
                assertSucceedsAnd(result) {
                    assertEquals(type, statement.body.type)
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
                val result = TypeChecker.typeCheck(statement, randomTCNamespace())
                assertSucceedsAnd(result) {
                    val fn = it.namespace.rootScope.functions["abc"]
                    assertNotNull(fn)

                    assertEquals(Signature(params = listOf(), returnType = type), fn?.signature)
                }
            }
        }
    }

    @Test fun typecheckValDeclaration_assignValToBinding_newValHasTypeOfBinding() {
        val ns = randomTCNamespace()
        ns.rootScope.staticVals.put("a", TC.Binding.StaticValBinding("a", intLiteral(1).withType(KGTypeTag.INT)))

        val valDeclOfBinding = KGValDeclaration("b", KGBindingReference("a"))
        val result = TypeChecker.typeCheck(valDeclOfBinding, ns)
        assertSucceedsAnd(result) {
            assertEquals(KGTypeTag.INT, it.namespace.rootScope.staticVals["b"]?.expression?.type)
        }
    }

    @Test fun typecheckValDeclaration_duplicateValDeclaration_typecheckingFails() {
        val ns = randomTCNamespace()
        ns.rootScope.staticVals.put("a", TC.Binding.StaticValBinding("a", intLiteral(1).withType(KGTypeTag.INT)))

        val valDecl = KGValDeclaration("a", trueLiteral())
        val result = TypeChecker.typeCheck(valDecl, ns)

        assertFails(result)
    }

    @Test fun typecheckFnDeclaration_duplicateFnDeclaration_typecheckingFails() {
        val ns = randomTCNamespace()
        ns.rootScope.functions.put("a", TC.Binding.FunctionBinding("a", intLiteral(1).withType(KGTypeTag.INT), Signature(params = listOf(), returnType = KGTypeTag.INT)))

        val valDecl = KGFnDeclaration("a", trueLiteral())
        val result = TypeChecker.typeCheck(valDecl, ns)

        assertFails(result)
    }

    @Test fun typecheckFnDeclaration_duplicateValDeclaration_typecheckingFails() {
        val ns = randomTCNamespace()
        ns.rootScope.functions.put("a", TC.Binding.FunctionBinding("a", intLiteral(1).withType(KGTypeTag.INT), Signature(params = listOf(), returnType = KGTypeTag.INT)))

        val valDecl = KGFnDeclaration("a", trueLiteral())
        val result = TypeChecker.typeCheck(valDecl, ns)

        assertFails(result)
    }
}