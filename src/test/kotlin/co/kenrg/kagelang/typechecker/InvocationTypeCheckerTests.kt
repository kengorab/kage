package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.*
import co.kenrg.kagelang.model.Signature
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.tree.types.KGType.Companion.BOOL
import co.kenrg.kagelang.tree.types.KGType.Companion.DEC
import co.kenrg.kagelang.tree.types.KGType.Companion.INT
import co.kenrg.kagelang.tree.types.KGType.Companion.STRING
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest

class InvocationTypeCheckerTests {

    @Nested
    inner class InvocationOfFnWithParams {

        @Test fun typecheckInvocation_tooFewParams_typecheckingFails() {
            // fn abc(a: Int, b: Bool): Int
            val abc = TCBinding.FunctionBinding(
                    "abc",
                    Signature(params = listOf(Pair("a", INT), Pair("a", BOOL)), returnType = INT)
            )

            val ns = randomTCNamespace()
            ns.rootScope.functions.put("abc", abc)
            val invocation = KGInvocation(
                    KGBindingReference("abc"),
                    listOf(intLiteral(1))
            )
            val result = TypeChecker.typeCheck(invocation, ns)
            assertFails(result)
        }

        @Test fun typecheckInvocation_tooManyParams_typecheckingFails() {
            // fn abc(a: Int, b: Bool): Int
            val abc = TCBinding.FunctionBinding(
                    "abc",
                    Signature(params = listOf(Pair("a", INT), Pair("b", BOOL)), returnType = INT)
            )

            val ns = randomTCNamespace()
            ns.rootScope.functions.put("abc", abc)
            val invocation = KGInvocation(
                    KGBindingReference("abc"),
                    listOf(
                            intLiteral(1),
                            trueLiteral(),
                            stringLiteral("too many params!")
                    )
            )
            val result = TypeChecker.typeCheck(invocation, ns)
            assertFails(result)
        }

        @Test fun typecheckInvocation_incorrectlyTypedParams_typecheckingFails() {
            // fn abc(a: Int, b: Bool): Int
            val abc = TCBinding.FunctionBinding(
                    "abc",
                    Signature(params = listOf(Pair("a", INT), Pair("b", BOOL)), returnType = INT)
            )

            val ns = randomTCNamespace()
            ns.rootScope.functions.put("abc", abc)
            val invocation = KGInvocation(
                    KGBindingReference("abc"),
                    listOf(
                            trueLiteral(),
                            stringLiteral("wrong type")
                    )
            )
            val result = TypeChecker.typeCheck(invocation, ns)
            assertFails(result)
        }

        @Test fun typecheckInvocation_correctNumberAndTypeOfParams_typecheckingPasses() {
            // fn abc(a: Int, b: Bool): Int
            val abc = TCBinding.FunctionBinding(
                    "abc",
                    Signature(params = listOf(Pair("a", INT), Pair("b", BOOL)), returnType = INT)
            )

            val ns = randomTCNamespace()
            ns.rootScope.functions.put("abc", abc)
            val invocation = KGInvocation(
                    KGBindingReference("abc"),
                    listOf(
                            intLiteral(1),
                            falseLiteral()
                    )
            )
            val result = TypeChecker.typeCheck(invocation, ns)
            assertSucceeds(result)
        }

        // TODO - Add test which verifies correct polymorphic fn chosen based on params
        @Test fun typecheckInvocation_polymorphicFunctions() {
            // fn abc(a: Int, b: Bool): Int
            val abc1 = TCBinding.FunctionBinding(
                    "abc",
                    Signature(params = listOf(Pair("a", INT), Pair("b", BOOL)), returnType = INT)
            )

            // fn abc(a: Int, b: Bool, c: Int): Int
            val abc2 = TCBinding.FunctionBinding(
                    "abc",
                    Signature(params = listOf(Pair("a", INT), Pair("b", BOOL), Pair("c", INT)), returnType = INT)
            )

            val ns = randomTCNamespace()
            ns.rootScope.functions.put("abc", abc1)
            ns.rootScope.functions.put("abc", abc2)

            // Invoking abc(1, false) should call abc1
            val invocation1 = KGInvocation(
                    KGBindingReference("abc"),
                    listOf(
                            intLiteral(1),
                            falseLiteral()
                    )
            )
            val result1 = TypeChecker.typeCheck(invocation1, ns)
            assertSucceeds(result1)

            // Invoking abc(1, false, 2) should call abc2
            val invocation2 = KGInvocation(
                    KGBindingReference("abc"),
                    listOf(
                            intLiteral(1),
                            falseLiteral(),
                            intLiteral(2)
                    )
            )
            val result2 = TypeChecker.typeCheck(invocation2, ns)
            assertSucceeds(result2)

            // Invoking abc(1) should fail
            val invocation3 = KGInvocation(
                    KGBindingReference("abc"),
                    listOf(intLiteral(1))
            )
            val result3 = TypeChecker.typeCheck(invocation3, ns)
            assertFails(result3)
        }
    }

    @TestFactory
    @DisplayName("Invoking invokable binding should pass typechecking, and have type of signature's returnType")
    fun typecheckInvocation_targetIsInvokableBinding_typecheckingPassesAndInvocationExprHasProperType(): List<DynamicTest> {
        data class Case(val repr: String, val expr: KGTree.KGExpression, val type: KGType)

        return listOf(
                Case("1", intLiteral(1), INT),
                Case("3.14", decLiteral(3.14), DEC),
                Case("true", trueLiteral(), BOOL),
                Case("\"some string\"", stringLiteral("some string"), STRING),

                Case("(1 + 1)", KGParenthesized(KGBinary(intLiteral(1), "+", intLiteral(1))), INT),
                Case("(1.0 - -2.14)", KGParenthesized(KGBinary(decLiteral(1.0), "-", KGUnary("-", decLiteral(2.14)))), DEC),
                Case("(true || false)", KGParenthesized(KGBinary(trueLiteral(), "||", falseLiteral())), BOOL),
                Case("(\"hello \" ++ \"world\")", KGParenthesized(KGBinary(stringLiteral("hello "), "++", stringLiteral("world"))), STRING)
        ).map { testCase ->
            val (repr, expr, type) = testCase

            dynamicTest("With `fn abc() = $repr` defined, the invocation `abc()` should have type $type") {
                val ns = randomTCNamespace()
                ns.rootScope.functions.put("abc", TCBinding.FunctionBinding("a", Signature(params = listOf(), returnType = type)))
                val result = TypeChecker.typeCheck(KGInvocation(KGBindingReference("abc")), ns)
                assertSucceedsAnd(result) {
                    assertEquals(type, it.type)
                }
            }
        }
    }

    @TestFactory
    @DisplayName("Invoking non-invokable expressions should fail typechecking")
    fun typecheckInvocation_targetIsNonInvokableExpression_typecheckingFails(): List<DynamicTest> {
        data class Case(val repr: String, val expr: KGTree.KGExpression)

        return listOf(
                Case("1", intLiteral(1)),
                Case("3.14", decLiteral(3.14)),
                Case("true", trueLiteral()),
                Case("\"some string\"", stringLiteral("some string")),

                Case("(1 + 1)", KGParenthesized(KGBinary(intLiteral(1), "+", intLiteral(1)))),
                Case("(1.0 - -2.14)", KGParenthesized(KGBinary(decLiteral(1.0), "-", KGUnary("-", decLiteral(2.14))))),
                Case("(true || false)", KGParenthesized(KGBinary(trueLiteral(), "||", falseLiteral()))),
                Case("(\"hello \" ++ \"world\")", KGParenthesized(KGBinary(stringLiteral("hello "), "++", stringLiteral("world"))))
        ).map { testCase ->
            val (repr, expr) = testCase

            dynamicTest("The expression `$repr()` should not typecheck, since $repr is not invokable") {
                val result = TypeChecker.typeCheck(KGInvocation(expr), randomTCNamespace())
                assertFails(result)
            }
        }
    }

    @TestFactory
    @DisplayName("Invoking non-invokable bindings should fail typechecking")
    fun typecheckInvocation_targetIsNonInvokableBinding_typecheckingFails(): List<DynamicTest> {
        data class Case(val repr: String, val expr: KGTree.KGExpression)

        return listOf(
                Case("1", intLiteral(1).withType(INT)),
                Case("3.14", decLiteral(3.14).withType(DEC)),
                Case("true", trueLiteral().withType(BOOL)),
                Case("\"some string\"", stringLiteral("some string").withType(STRING)),

                Case("(1 + 1)", KGParenthesized(KGBinary(intLiteral(1), "+", intLiteral(1))).withType(INT)),
                Case("(1.0 - -2.14)", KGParenthesized(KGBinary(decLiteral(1.0), "-", KGUnary("-", decLiteral(2.14)))).withType(DEC)),
                Case("(true || false)", KGParenthesized(KGBinary(trueLiteral(), "||", falseLiteral())).withType(BOOL)),
                Case("(\"hello \" ++ \"world\")", KGParenthesized(KGBinary(stringLiteral("hello "), "++", stringLiteral("world"))).withType(STRING))
        ).map { testCase ->
            val (repr, expr) = testCase

            dynamicTest("The expression `$repr()` should not typecheck, since $repr is not invokable") {
                val ns = randomTCNamespace()
                ns.rootScope.vals.put("a", TCBinding.StaticValBinding("a", expr.type))
                val result = TypeChecker.typeCheck(KGInvocation(KGBindingReference("a")), ns)
                assertFails(result)
            }
        }
    }

    @Test fun typecheckInvocation_targetIsUndefinedBinding_typecheckingFails() {
        val result = TypeChecker.typeCheck(KGInvocation(KGBindingReference("a")), randomTCNamespace())
        assertFails(result)
    }

    @Test fun typecheckInvocation_targetIsTypeConstructor_passesTypecheckingWithType() {
        val ns = randomTCNamespace()
        ns.rootScope.types.put("SomeType", KGType("SomeType", ""))
        val invocation = KGInvocation(KGBindingReference("SomeType"))
        val result = TypeChecker.typeCheck(invocation, ns)
        assertSucceedsAnd(result) { assertEquals(KGType("SomeType", ""), invocation.type) }
    }

    @Test fun typecheckInvocation_targetIsConstructorForTypeWithProps_notAllPropsPassed_typecheckingFails() {
        val ns = randomTCNamespace()
        val typeName = RandomStringUtils.randomAlphabetic(16).capitalize()
        val type = KGType(
                typeName,
                "L${ns.name}\$$typeName;",
                className = "${ns.name}\$$typeName",
                props = mapOf(
                        "someStr" to KGType.STRING
                )
        )
        ns.rootScope.types.put(typeName, type)

        val invocation = KGInvocation(KGBindingReference(typeName), listOf())
        val result = TypeChecker.typeCheck(invocation, ns)
        assertFails(result)
    }

    @Test fun typecheckInvocation_targetIsConstructorForTypeWithProps_incorrectlyTypedPropsPassed_typecheckingFails() {
        val ns = randomTCNamespace()
        val typeName = RandomStringUtils.randomAlphabetic(16).capitalize()
        val type = KGType(
                typeName,
                "L${ns.name}\$$typeName;",
                className = "${ns.name}\$$typeName",
                props = mapOf(
                        "someStr" to KGType.STRING
                )
        )
        ns.rootScope.types.put(typeName, type)

        val invocation = KGInvocation(KGBindingReference(typeName), listOf(intLiteral(2)))
        val result = TypeChecker.typeCheck(invocation, ns)
        assertFails(result)
    }

    @Test fun typecheckInvocation_targetIsConstructorForTypeWithProps_correctlyTypedPropsPassed_passesTypechecking() {
        val ns = randomTCNamespace()
        val typeName = RandomStringUtils.randomAlphabetic(16).capitalize()
        val type = KGType(
                typeName,
                "L${ns.name}\$$typeName;",
                className = "${ns.name}\$$typeName",
                props = mapOf(
                        "someStr" to KGType.STRING
                )
        )
        ns.rootScope.types.put(typeName, type)

        val invocation = KGInvocation(KGBindingReference(typeName), listOf(stringLiteral("Hello")))
        val result = TypeChecker.typeCheck(invocation, ns)
        assertSucceedsAnd(result) { assertEquals(type, invocation.type) }
    }
}