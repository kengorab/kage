package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.trueLiteral
import co.kenrg.kagelang.model.FnParameter
import co.kenrg.kagelang.model.Signature
import co.kenrg.kagelang.model.TypeIdentifier
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.tree.types.StdLibType
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

        fun fnBodyAndReprForType(type: KGType, annotationType: KGType = type): Pair<KGTree, String> {
            return when (type) {
                KGType.INT,
                KGType.DEC,
                KGType.BOOL,
                KGType.STRING -> {
                    val fnBody = randomKGLiteralOfType(type)
                    val repr = "fn abc():${annotationType.name} = ${fnBody.value}"
                    Pair(fnBody, repr)
                }
                KGType.UNIT -> {
                    val fnBody = KGPrint(intLiteral(123))
                    val repr = "fn abc():${annotationType.name} = print(123)"
                    Pair(fnBody, repr)
                }
                else -> throw IllegalStateException("Invalid type: $type")
            }
        }

        @TestFactory
        @DisplayName("FnDeclarations with return type annotations that match their body's type should pass typechecking")
        fun typecheckFnDeclaration_hasReturnTypeAnnotation_annotationMatchesBodyType_passesTypechecking(): List<DynamicTest> {
            return listOf(KGType.INT, KGType.DEC, KGType.BOOL, KGType.STRING, KGType.UNIT).map { type ->
                val (fnBody, repr) = fnBodyAndReprForType(type)

                dynamicTest("$repr should pass typechecking, since its body's type is $type") {
                    val statement = KGFnDeclaration("abc", fnBody, listOf(), TypeIdentifier(type.name))
                    val result = TypeChecker.typeCheck(statement, randomTCNamespace())
                    assertSucceedsAnd(result) { assertEquals(type, statement.body.type) }
                }
            }
        }

        @Test fun typecheckFnDeclaration_retTypeIsPair_bodyIsPairOfSameTypes_passesTypechecking() {
            val statement = KGFnDeclaration(
                    "abc",
                    KGTuple(listOf(KGBindingReference("a"), KGBindingReference("a"))),
                    listOf(FnParameter("a", TypeIdentifier("String"))),
                    TypeIdentifier("Pair", listOf(TypeIdentifier("String"), TypeIdentifier("String")))
            )
            val result = TypeChecker.typeCheck(statement, randomTCNamespace())
            assertSucceedsAnd(result) {
                assertEquals(KGType.stdLibType(StdLibType.Pair, typeParams = listOf(KGType.STRING, KGType.STRING)), statement.body.type)
            }
        }

        @TestFactory
        @DisplayName("FnDeclarations with return type annotations that don't match their body's type should fail typechecking")
        fun typecheckFnDeclaration_hasReturnTypeAnnotation_annotationDoesNotMatchBodyType_failsTypechecking(): List<DynamicTest> {
            val types = listOf(KGType.INT, KGType.DEC, KGType.BOOL, KGType.STRING, KGType.UNIT)
            return types.map { type ->
                val returnAnnotationType = (types - type)[RandomUtils.nextInt(0, types.size - 1)]
                val (fnBody, repr) = fnBodyAndReprForType(type, returnAnnotationType)

                dynamicTest("$repr should fail typechecking, since its body's type is $type but the return annotation is $returnAnnotationType") {
                    val statement = KGFnDeclaration("abc", fnBody, listOf(), TypeIdentifier(returnAnnotationType.name))
                    val result = TypeChecker.typeCheck(statement, randomTCNamespace())
                    assertFails(result)
                }
            }
        }

        @Test fun testFunctionWithSupertypeReturnType_exprReturnsSubtype_passesTypechecking() {
            val fnDecl = KGFnDeclaration(
                    "optify",
                    KGInvocation(KGBindingReference("Some"), listOf(KGBindingReference("i"))),
                    listOf(FnParameter("i", TypeIdentifier("Int"))),
                    TypeIdentifier("Maybe", listOf(TypeIdentifier("Int")))
            )
            val result = TypeChecker.typeCheck(fnDecl, randomTCNamespace())
            assertSucceeds(result)
        }

        @Test fun testFunctionWithSupertypeParamTypes_passedSubtypes_passesTypechecking() {
            val ns = randomTCNamespace()
            val maybeInt = KGType.stdLibType(StdLibType.Maybe, typeParams = listOf(KGType.INT))
            ns.rootScope.functions.put(
                    "pairify",
                    TCBinding.FunctionBinding(
                            "pairify",
                            Signature(
                                    listOf(Pair("a", maybeInt), Pair("b", maybeInt)),
                                    KGType.stdLibType(StdLibType.Pair, listOf(maybeInt, maybeInt))
                            )
                    )
            )
            val invocation = KGInvocation(KGBindingReference("pairify"), listOf(KGInvocation(KGBindingReference("Some"), listOf(intLiteral(3))), KGInvocation(KGBindingReference("None"), listOf())))
            val result = TypeChecker.typeCheck(invocation, ns)
            assertSucceeds(result)
        }
    }

    @Nested
    inner class FnDeclarationWithParams {

        @Test fun typecheckFnDeclaration_duplicateParamsSameType_typecheckingFails() {
            // fn abc(a: Int, a: Int) = 1
            val statement = KGFnDeclaration("abc", intLiteral(1), listOf(FnParameter("a", TypeIdentifier("Int")), FnParameter("a", TypeIdentifier("Int"))))
            val result = TypeChecker.typeCheck(statement, randomTCNamespace())
            assertFails(result)
        }

        @Test fun typecheckFnDeclaration_duplicateParams_typecheckingFails() {
            // fn abc(a: Int, b: String, a: Bool) = 1
            val statement = KGFnDeclaration(
                    "abc",
                    intLiteral(1),
                    listOf(
                            FnParameter("a", TypeIdentifier("Int")),
                            FnParameter("b", TypeIdentifier("Bool")),
                            FnParameter("a", TypeIdentifier("Int"))
                    )
            )
            val result = TypeChecker.typeCheck(statement, randomTCNamespace())
            assertFails(result)
        }

        @Test fun typecheckFnDeclaration_oneParamWithTypeAnnotation_passesTypecheckingWithSignatureSet() {
            // fn abc(a: Int) = 1
            val statement = KGFnDeclaration("abc", intLiteral(1), listOf(FnParameter("a", TypeIdentifier("Int"))))
            val result = TypeChecker.typeCheck(statement, randomTCNamespace())
            assertSucceedsAnd(result) {
                val abc = it.namespace.rootScope.functions["abc"]
                assertNotNull(abc)
                assertEquals(
                        Signature(params = listOf(Pair("a", KGType.INT)), returnType = KGType.INT),
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
                            FnParameter("a", TypeIdentifier("Int")), FnParameter("a1", TypeIdentifier("Int")),
                            FnParameter("b", TypeIdentifier("Bool")), FnParameter("c", TypeIdentifier("String"))
                    )
            )
            val result = TypeChecker.typeCheck(statement, randomTCNamespace())
            assertSucceedsAnd(result) {
                val abc = it.namespace.rootScope.functions["abc"]
                assertNotNull(abc)
                assertEquals(
                        Signature(
                                params = listOf(
                                        Pair("a", KGType.INT), Pair("a1", KGType.INT),
                                        Pair("b", KGType.BOOL), Pair("c", KGType.STRING)
                                ),
                                returnType = KGType.INT
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
                    listOf(FnParameter("a", TypeIdentifier("Int")))
            )
            val result = TypeChecker.typeCheck(statement, randomTCNamespace())
            assertSucceedsAnd(result) {
                assertEquals(KGType.INT, statement.body.type)
                val abc = it.namespace.rootScope.functions["abc"]
                assertNotNull(abc)
                assertEquals(
                        Signature(params = listOf(Pair("a", KGType.INT)), returnType = KGType.INT),
                        abc[0].signature
                )
            }
        }

        @Test fun typecheckFnDeclaration_twoParamsUsedInBody_bodyFailsTypechecking_failsTypechecking() {
            // fn abc(a: Int, b: String) = a + b
            val statement = KGFnDeclaration(
                    "abc",
                    KGBinary(KGBindingReference("a"), "+", KGBindingReference("b")),
                    listOf(FnParameter("a", TypeIdentifier("Int")), FnParameter("b", TypeIdentifier("String")))
            )
            val result = TypeChecker.typeCheck(statement, randomTCNamespace())
            assertFails(result)
        }
    }

    @TestFactory
    @DisplayName("FnDeclaration statements should have type of Unit, and their inner expressions should be typed")
    fun typecheckFnDeclaration_returnsUnit_innerExpressionHasTypeSet(): List<DynamicTest> {
        return listOf(KGType.INT, KGType.DEC, KGType.BOOL, KGType.STRING).map { type ->
            val fnBody = randomKGLiteralOfType(type)
            val repr = "fn abc() = ${fnBody.value}"

            dynamicTest("$repr should have type UNIT, and inner expression should have type $type") {
                val statement = KGFnDeclaration("abc", fnBody)
                val result = TypeChecker.typeCheck(statement, randomTCNamespace())
                assertSucceedsAnd(result) {
                    assertEquals(type, statement.body.type)
                    assertEquals(KGType.UNIT, it.type)
                }
            }
        }
    }

    @Test fun typecheckFnDeclaration_usesValDefinedOutsideFn_succeeds() {
        val fnDecl = KGFnDeclaration("abc", KGBinary(intLiteral(3), "+", KGBindingReference("a")))

        val ns = randomTCNamespace()
        ns.rootScope.vals.put("a", TCBinding.StaticValBinding("a", KGType.INT))
        val result = TypeChecker.typeCheck(fnDecl, ns)
        assertSucceedsAnd(result) {
            assertEquals(KGType.INT, fnDecl.body.type)
            assertEquals(KGType.UNIT, it.type)
        }
    }

    @TestFactory
    @DisplayName("FnDeclaration statements cause the function to be inserted into the bindings, with proper signature")
    fun typecheckFnDeclaration_noParams_fnPresentInBindingsWithProperSignature(): List<DynamicTest> {
        return listOf(KGType.INT, KGType.DEC, KGType.BOOL, KGType.STRING).map { type ->
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
        val abcFn = TCBinding.FunctionBinding("abc", Signature(params = listOf(), returnType = KGType.INT))

        val ns = randomTCNamespace()
        ns.rootScope.functions.put("abc", abcFn)

        val fnDecl = KGFnDeclaration(
                "abc",
                intLiteral(3),
                listOf(FnParameter("b", TypeIdentifier("Bool"))),
                TypeIdentifier("Int")
        )
        val result = TypeChecker.typeCheck(fnDecl, ns)
        assertSucceeds(result)
    }

    @Test fun typecheckFnDeclaration_duplicateFnNameDeclaration_sameParamSignature_differentReturnType_typecheckingFails() {
        val abcFn = TCBinding.FunctionBinding("abc", Signature(params = listOf(), returnType = KGType.INT))

        val ns = randomTCNamespace()
        ns.rootScope.functions.put("abc", abcFn)

        val fnDecl = KGFnDeclaration("abc", trueLiteral(), listOf(), TypeIdentifier("Bool"))
        val result = TypeChecker.typeCheck(fnDecl, ns)
        assertFails(result)
    }

    @Test fun typecheckFnDeclaration_duplicateFnNameDeclaration_differentParamSignature_differentReturnType_typecheckingFails() {
        val abcFn = TCBinding.FunctionBinding("abc", Signature(params = listOf(), returnType = KGType.INT))

        val ns = randomTCNamespace()
        ns.rootScope.functions.put("abc", abcFn)

        val fnDecl = KGFnDeclaration(
                "abc",
                trueLiteral(),
                listOf(FnParameter("b", TypeIdentifier("Bool"))),
                TypeIdentifier("Bool")
        )
        val result = TypeChecker.typeCheck(fnDecl, ns)
        assertFails(result)
    }

    @Test fun typecheckFnDeclaration_fnNameIsSameAsTypeInScope_typecheckingFails() {
        val ns = randomTCNamespace()
        ns.rootScope.types.put("TypeName", KGType("TypeName", ""))

        val fnDecl = KGFnDeclaration(
                "TypeName",
                trueLiteral(),
                listOf(FnParameter("b", TypeIdentifier("Bool"))),
                TypeIdentifier("Bool")
        )
        val result = TypeChecker.typeCheck(fnDecl, ns)
        assertFails(result)
    }


}