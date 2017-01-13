package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.*
import co.kenrg.kagelang.model.Signature
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGTypeTag
import org.junit.jupiter.api.*
import org.junit.jupiter.api.DynamicTest.dynamicTest

class InvocationTypeCheckerTests {

    @Nested
    inner class InvocationOfFnWithParams {

        @Test fun typecheckInvocation_tooFewParams_typecheckingFails() {
            // fn abc :: Int -> Bool -> Int
            val abc = TCBinding.FunctionBinding(
                    "abc",
                    Signature(params = listOf(KGTypeTag.INT, KGTypeTag.BOOL), returnType = KGTypeTag.INT)
            )

            val ns = randomTCNamespace()
            ns.rootScope.functions.put("abc", abc)
            val invocation = KGInvocation(
                    KGBindingReference("abc"),
                    listOf(intLiteral(1).withType(KGTypeTag.INT))
            )
            val result = TypeChecker.typeCheck(invocation, ns)
            assertFails(result)
        }

        @Test fun typecheckInvocation_tooManyParams_typecheckingFails() {
            // fn abc :: Int -> Bool -> Int
            val abc = TCBinding.FunctionBinding(
                    "abc",
                    Signature(params = listOf(KGTypeTag.INT, KGTypeTag.BOOL), returnType = KGTypeTag.INT)
            )

            val ns = randomTCNamespace()
            ns.rootScope.functions.put("abc", abc)
            val invocation = KGInvocation(
                    KGBindingReference("abc"),
                    listOf(
                            intLiteral(1).withType(KGTypeTag.INT),
                            trueLiteral().withType(KGTypeTag.BOOL),
                            stringLiteral("too many params!").withType(KGTypeTag.STRING)
                    )
            )
            val result = TypeChecker.typeCheck(invocation, ns)
            assertFails(result)
        }

        @Test fun typecheckInvocation_incorrectlyTypedParams_typecheckingFails() {
            // fn abc :: Int -> Bool -> Int
            val abc = TCBinding.FunctionBinding(
                    "abc",
                    Signature(params = listOf(KGTypeTag.INT, KGTypeTag.BOOL), returnType = KGTypeTag.INT)
            )

            val ns = randomTCNamespace()
            ns.rootScope.functions.put("abc", abc)
            val invocation = KGInvocation(
                    KGBindingReference("abc"),
                    listOf(
                            trueLiteral().withType(KGTypeTag.BOOL),
                            stringLiteral("wrong type").withType(KGTypeTag.STRING)
                    )
            )
            val result = TypeChecker.typeCheck(invocation, ns)
            assertFails(result)
        }

        @Test fun typecheckInvocation_correctNumberAndTypeOfParams_typecheckingPasses() {
            // fn abc :: Int -> Bool -> Int
            val abc = TCBinding.FunctionBinding(
                    "abc",
                    Signature(params = listOf(KGTypeTag.INT, KGTypeTag.BOOL), returnType = KGTypeTag.INT)
            )

            val ns = randomTCNamespace()
            ns.rootScope.functions.put("abc", abc)
            val invocation = KGInvocation(
                    KGBindingReference("abc"),
                    listOf(
                            intLiteral(1).withType(KGTypeTag.INT),
                            falseLiteral().withType(KGTypeTag.BOOL)
                    )
            )
            val result = TypeChecker.typeCheck(invocation, ns)
            assertSucceeds(result)
        }

        // TODO - Add test which verifies correct polymorphic fn chosen based on params
        @Test fun typecheckInvocation_polymorphicFunctions() {
            // fn abc :: Int -> Bool -> Int
            val abc1 = TCBinding.FunctionBinding(
                    "abc",
                    Signature(params = listOf(KGTypeTag.INT, KGTypeTag.BOOL), returnType = KGTypeTag.INT)
            )

            // fn abc :: Int -> Bool -> Int -> Int
            val abc2 = TCBinding.FunctionBinding(
                    "abc",
                    Signature(params = listOf(KGTypeTag.INT, KGTypeTag.BOOL, KGTypeTag.INT), returnType = KGTypeTag.INT)
            )

            val ns = randomTCNamespace()
            ns.rootScope.functions.put("abc", abc1)
            ns.rootScope.functions.put("abc", abc2)

            // Invoking abc(1, false) should call abc1
            val invocation1 = KGInvocation(
                    KGBindingReference("abc"),
                    listOf(
                            intLiteral(1).withType(KGTypeTag.INT),
                            falseLiteral().withType(KGTypeTag.BOOL)
                    )
            )
            val result1 = TypeChecker.typeCheck(invocation1, ns)
            assertSucceeds(result1)

            // Invoking abc(1, false, 2) should call abc2
            val invocation2 = KGInvocation(
                    KGBindingReference("abc"),
                    listOf(
                            intLiteral(1).withType(KGTypeTag.INT),
                            falseLiteral().withType(KGTypeTag.BOOL),
                            intLiteral(2).withType(KGTypeTag.INT)
                    )
            )
            val result2 = TypeChecker.typeCheck(invocation2, ns)
            assertSucceeds(result2)

            // Invoking abc(1) should fail
            val invocation3 = KGInvocation(
                    KGBindingReference("abc"),
                    listOf(intLiteral(1).withType(KGTypeTag.INT))
            )
            val result3 = TypeChecker.typeCheck(invocation3, ns)
            assertFails(result3)
        }
    }

    @TestFactory
    @DisplayName("Invoking invokable binding should pass typechecking, and have type of signature's returnType")
    fun typecheckInvocation_targetIsInvokableBinding_typecheckingPassesAndInvocationExprHasProperType(): List<DynamicTest> {
        data class Case(val repr: String, val expr: KGTree.KGExpression, val type: KGTypeTag)

        return listOf(
                Case("1", intLiteral(1), KGTypeTag.INT),
                Case("3.14", decLiteral(3.14), KGTypeTag.DEC),
                Case("true", trueLiteral(), KGTypeTag.BOOL),
                Case("\"some string\"", stringLiteral("some string"), KGTypeTag.STRING),

                Case("(1 + 1)", KGParenthesized(KGBinary(intLiteral(1), "+", intLiteral(1))), KGTypeTag.INT),
                Case("(1.0 - -2.14)", KGParenthesized(KGBinary(decLiteral(1.0), "-", KGUnary("-", decLiteral(2.14)))), KGTypeTag.DEC),
                Case("(true || false)", KGParenthesized(KGBinary(trueLiteral(), "||", falseLiteral())), KGTypeTag.BOOL),
                Case("(\"hello \" ++ \"world\")", KGParenthesized(KGBinary(stringLiteral("hello "), "++", stringLiteral("world"))), KGTypeTag.STRING)
        ).map { testCase ->
            val (repr, expr, type) = testCase

            dynamicTest("With `fn abc() = $repr` defined, the invocation `abc()` should have type $type") {
                val ns = randomTCNamespace()
                ns.rootScope.functions.put("abc", TCBinding.FunctionBinding("a", Signature(params = listOf(), returnType = type)))
                val result = TypeChecker.typeCheck(KGInvocation(KGBindingReference("abc")), ns)
                assertSucceedsAnd(result) {
                    Assertions.assertEquals(type, it.type)
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
                Case("1", intLiteral(1).withType(KGTypeTag.INT)),
                Case("3.14", decLiteral(3.14).withType(KGTypeTag.DEC)),
                Case("true", trueLiteral().withType(KGTypeTag.BOOL)),
                Case("\"some string\"", stringLiteral("some string").withType(KGTypeTag.STRING)),

                Case("(1 + 1)", KGParenthesized(KGBinary(intLiteral(1), "+", intLiteral(1))).withType(KGTypeTag.INT)),
                Case("(1.0 - -2.14)", KGParenthesized(KGBinary(decLiteral(1.0), "-", KGUnary("-", decLiteral(2.14)))).withType(KGTypeTag.DEC)),
                Case("(true || false)", KGParenthesized(KGBinary(trueLiteral(), "||", falseLiteral())).withType(KGTypeTag.BOOL)),
                Case("(\"hello \" ++ \"world\")", KGParenthesized(KGBinary(stringLiteral("hello "), "++", stringLiteral("world"))).withType(KGTypeTag.STRING))
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
}