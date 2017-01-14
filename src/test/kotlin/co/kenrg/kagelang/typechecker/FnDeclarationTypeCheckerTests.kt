package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.trueLiteral
import co.kenrg.kagelang.model.FnParameter
import co.kenrg.kagelang.model.Signature
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGTypeTag
import org.apache.commons.lang3.RandomUtils
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DynamicTest.dynamicTest

class FnDeclarationTypeCheckerTests {

    @Nested
    // Tests of function declarations that include return type annotations. Function declarations without return type
    // annotations are handled in the tests outside of this inner class.
    inner class FnDeclarationWithReturnTypeAnnotation {

        fun fnBodyAndReprForType(type: KGTypeTag, annotationType: KGTypeTag = type): Pair<KGTree, String> {
            return when (type) {
                KGTypeTag.INT,
                KGTypeTag.DEC,
                KGTypeTag.BOOL,
                KGTypeTag.STRING -> {
                    val fnBody = randomKGLiteralOfType(type)
                    val repr = "fn abc():${annotationType.typeName} = ${fnBody.value}"
                    Pair(fnBody, repr)
                }
                KGTypeTag.UNIT -> {
                    val fnBody = KGPrint(intLiteral(123))
                    val repr = "fn abc():${annotationType.typeName} = print(123)"
                    Pair(fnBody, repr)
                }
                else -> throw IllegalStateException("Invalid type: $type")
            }
        }

        @TestFactory
        @DisplayName("FnDeclarations with return type annotations that match their body's type should pass typechecking")
        fun typecheckFnDeclaration_hasReturnTypeAnnotation_annotationMatchesBodyType_passesTypechecking(): List<DynamicTest> {
            return listOf(KGTypeTag.INT, KGTypeTag.DEC, KGTypeTag.BOOL, KGTypeTag.STRING, KGTypeTag.UNIT).map { type ->
                val (fnBody, repr) = fnBodyAndReprForType(type)

                dynamicTest("$repr should pass typechecking, since its body's type is $type") {
                    val statement = KGFnDeclaration("abc", fnBody, listOf(), type)
                    val result = TypeChecker.typeCheck(statement, randomTCNamespace())
                    assertSucceedsAnd(result) { assertEquals(type, statement.body.type) }
                }
            }
        }

        @TestFactory
        @DisplayName("FnDeclarations with return type annotations that don't match their body's type should fail typechecking")
        fun typecheckFnDeclaration_hasReturnTypeAnnotation_annotationDoesNotMatchBodyType_failsTypechecking(): List<DynamicTest> {
            val types = listOf(KGTypeTag.INT, KGTypeTag.DEC, KGTypeTag.BOOL, KGTypeTag.STRING, KGTypeTag.UNIT)
            return types.map { type ->
                val returnAnnotationType = (types - type)[RandomUtils.nextInt(0, types.size - 1)]
                val (fnBody, repr) = fnBodyAndReprForType(type, returnAnnotationType)

                dynamicTest("$repr should fail typechecking, since its body's type is $type but the return annotation is $returnAnnotationType") {
                    val statement = KGFnDeclaration("abc", fnBody, listOf(), returnAnnotationType)
                    val result = TypeChecker.typeCheck(statement, randomTCNamespace())
                    assertFails(result)
                }
            }
        }
    }

    @Nested
    inner class FnDeclarationWithParams {

        @Test fun typecheckFnDeclaration_duplicateParamsSameType_typecheckingFails() {
            // fn abc(a: Int, a: Int) = 1
            val statement = KGFnDeclaration("abc", intLiteral(1), listOf(FnParameter("a", KGTypeTag.INT), FnParameter("a", KGTypeTag.INT)))
            val result = TypeChecker.typeCheck(statement, randomTCNamespace())
            assertFails(result)
        }

        @Test fun typecheckFnDeclaration_duplicateParams_typecheckingFails() {
            // fn abc(a: Int, b: String, a: Bool) = 1
            val statement = KGFnDeclaration(
                    "abc",
                    intLiteral(1),
                    listOf(
                            FnParameter("a", KGTypeTag.INT),
                            FnParameter("b", KGTypeTag.BOOL),
                            FnParameter("a", KGTypeTag.INT)
                    )
            )
            val result = TypeChecker.typeCheck(statement, randomTCNamespace())
            assertFails(result)
        }

        @Test fun typecheckFnDeclaration_oneParamWithTypeAnnotation_passesTypecheckingWithSignatureSet() {
            // fn abc(a: Int) = 1
            val statement = KGFnDeclaration("abc", intLiteral(1), listOf(FnParameter("a", KGTypeTag.INT)))
            val result = TypeChecker.typeCheck(statement, randomTCNamespace())
            assertSucceedsAnd(result) {
                val abc = it.namespace.rootScope.functions["abc"]
                assertNotNull(abc)
                assertEquals(
                        Signature(params = listOf(FnParameter("a", KGTypeTag.INT)), returnType = KGTypeTag.INT),
                        abc[0].signature
                )
            }
        }

        @Test fun typecheckFnDeclaration_manyParamsWithTypeAnnotations_passesTypecheckingWithSignatureSet() {
            // fn abc(a: Int, a1: Int, b: Bool, c: String) = 1
            val statement = KGFnDeclaration(
                    "abc",
                    intLiteral(1),
                    listOf(
                            FnParameter("a", KGTypeTag.INT), FnParameter("a1", KGTypeTag.INT),
                            FnParameter("b", KGTypeTag.BOOL), FnParameter("c", KGTypeTag.STRING)
                    )
            )
            val result = TypeChecker.typeCheck(statement, randomTCNamespace())
            assertSucceedsAnd(result) {
                val abc = it.namespace.rootScope.functions["abc"]
                assertNotNull(abc)
                assertEquals(
                        Signature(
                                params = listOf(
                                        FnParameter("a", KGTypeTag.INT), FnParameter("a1", KGTypeTag.INT),
                                        FnParameter("b", KGTypeTag.BOOL), FnParameter("c", KGTypeTag.STRING)
                                ),
                                returnType = KGTypeTag.INT
                        ),
                        abc[0].signature
                )
            }
        }

        @Test fun typecheckFnDeclaration_oneParamWithTypeAnnotation_useParamInBody_passesTypechecking() {
            // fn abc(a: Int) = a + 1
            val statement = KGFnDeclaration(
                    "abc",
                    KGBinary(KGBindingReference("a"), "+", intLiteral(1)),
                    listOf(FnParameter("a", KGTypeTag.INT))
            )
            val result = TypeChecker.typeCheck(statement, randomTCNamespace())
            assertSucceedsAnd(result) {
                assertEquals(KGTypeTag.INT, statement.body.type)
                val abc = it.namespace.rootScope.functions["abc"]
                assertNotNull(abc)
                assertEquals(
                        Signature(params = listOf(FnParameter("a", KGTypeTag.INT)), returnType = KGTypeTag.INT),
                        abc[0].signature
                )
            }
        }

        @Test fun typecheckFnDeclaration_twoParamsUsedInBody_bodyFailsTypechecking_failsTypechecking() {
            // fn abc(a: Int, b: String) = a + b
            val statement = KGFnDeclaration(
                    "abc",
                    KGBinary(KGBindingReference("a"), "+", KGBindingReference("b")),
                    listOf(FnParameter("a", KGTypeTag.INT), FnParameter("b", KGTypeTag.STRING))
            )
            val result = TypeChecker.typeCheck(statement, randomTCNamespace())
            assertFails(result)
        }
    }

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

    @Test fun typecheckFnDeclaration_usesValDefinedOutsideFn_succeeds() {
        val fnDecl = KGFnDeclaration("abc", KGBinary(intLiteral(3), "+", KGBindingReference("a")))

        val ns = randomTCNamespace()
        ns.rootScope.vals.put("a", TCBinding.StaticValBinding("a", KGTypeTag.INT))
        val result = TypeChecker.typeCheck(fnDecl, ns)
        assertSucceedsAnd(result) {
            assertEquals(KGTypeTag.INT, fnDecl.body.type)
            assertEquals(KGTypeTag.UNIT, it.type)
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

                    assertEquals(Signature(params = listOf(), returnType = type), fn[0].signature)
                }
            }
        }
    }

    @Test fun typecheckFnDeclaration_duplicateFnNameDeclaration_differentParamSignature_sameReturnType_typecheckingPasses() {
        val abcFn = TCBinding.FunctionBinding("abc", Signature(params = listOf(), returnType = KGTypeTag.INT))

        val ns = randomTCNamespace()
        ns.rootScope.functions.put("abc", abcFn)

        val fnDecl = KGFnDeclaration(
                "abc",
                intLiteral(3),
                listOf(FnParameter("b", KGTypeTag.BOOL)),
                KGTypeTag.INT
        )
        val result = TypeChecker.typeCheck(fnDecl, ns)
        assertSucceeds(result)
    }

    @Test fun typecheckFnDeclaration_duplicateFnNameDeclaration_sameParamSignature_differentReturnType_typecheckingFails() {
        val abcFn = TCBinding.FunctionBinding("abc", Signature(params = listOf(), returnType = KGTypeTag.INT))

        val ns = randomTCNamespace()
        ns.rootScope.functions.put("abc", abcFn)

        val fnDecl = KGFnDeclaration("abc", trueLiteral(), listOf(), KGTypeTag.BOOL)
        val result = TypeChecker.typeCheck(fnDecl, ns)
        assertFails(result)
    }

    @Test fun typecheckFnDeclaration_duplicateFnNameDeclaration_differentParamSignature_differentReturnType_typecheckingFails() {
        val abcFn = TCBinding.FunctionBinding("abc", Signature(params = listOf(), returnType = KGTypeTag.INT))

        val ns = randomTCNamespace()
        ns.rootScope.functions.put("abc", abcFn)

        val fnDecl = KGFnDeclaration(
                "abc",
                trueLiteral(),
                listOf(FnParameter("b", KGTypeTag.BOOL)),
                KGTypeTag.BOOL
        )
        val result = TypeChecker.typeCheck(fnDecl, ns)
        assertFails(result)
    }
}