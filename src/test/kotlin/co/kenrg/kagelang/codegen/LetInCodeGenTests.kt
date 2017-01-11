package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.util.*

class LetInCodeGenTests : BaseTest() {

    @TestFactory
    fun testLetInExpression(): List<DynamicTest> {
        data class Case(val repr: String, val bindings: List<KGStatement>, val body: KGTree, val expectedOutput: String)

        return listOf(
                Case(
                        "let\nval a = 1\nin\nprint(a)",
                        listOf(KGValDeclaration("a", intLiteral(1))),
                        KGPrint(KGBindingReference("a")),
                        "1"
                ),
                Case(
                        "let\nval a = 1\nval b = a + 1\nin\nprint(b + 1)",
                        listOf(
                                KGValDeclaration("a", intLiteral(1)),
                                KGValDeclaration("b", KGBinary(KGBindingReference("a"), "+", intLiteral(1)))
                        ),
                        KGPrint(KGBinary(KGBindingReference("b"), "+", intLiteral(1))),
                        "3"
                )
        ).map { testCase ->
            val (repr, bindings, body, expected) = testCase

            dynamicTest("Running `fn main() = $repr`, should output `$expected`") {
                val letInExpr = KGLetIn(bindings, body)
                val file = KGFile(
                        statements = listOf(
                                wrapInMainMethod(letInExpr)
                        ),
                        bindings = HashMap()
                )

                compileAndExecuteFileAnd(file) { output ->
                    assertEquals(expected, output)
                }
            }
        }
    }

    @Test fun testLetInExpression_bindingUsesNamespaceVal_outputsExpected() {
        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", KGBinary(intLiteral(1), "+", KGBindingReference("nsVal")))),
                KGPrint(KGBindingReference("a"))
        )
        val file = KGFile(
                statements = listOf(
                        KGValDeclaration("nsVal", intLiteral(1)),
                        wrapInMainMethod(letInExpr)
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("2", output)
        }
    }

    @Test fun testLetInExpression_bodyUsesNamespaceVal_outputsExpected() {
        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", intLiteral(1))),
                KGPrint(KGBinary(KGBindingReference("a"), "+", KGBindingReference("nsVal")))
        )
        val file = KGFile(
                statements = listOf(
                        KGValDeclaration("nsVal", intLiteral(1)),
                        wrapInMainMethod(letInExpr)
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("2", output)
        }
    }

    @Test fun testLetInExpression_bindingUsesNamespaceFn_outputsExpected() {
        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", KGInvocation(KGBindingReference("nsFn")))),
                KGPrint(KGBindingReference("a"))
        )
        val file = KGFile(
                statements = listOf(
                        KGFnDeclaration("nsFn", intLiteral(1)),
                        wrapInMainMethod(letInExpr)
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("1", output)
        }
    }

    @Test fun testLetInExpression_bodyUsesNamespaceFn_outputsExpected() {
        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", intLiteral(1))),
                KGPrint(KGBinary(KGBindingReference("a"), "+", KGInvocation(KGBindingReference("nsFn"))))
        )
        val file = KGFile(
                statements = listOf(
                        KGFnDeclaration("nsFn", intLiteral(1)),
                        wrapInMainMethod(letInExpr)
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("2", output)
        }
    }

    @Test fun testLetInExpression_valBindingReusesNameFromOuterScope_bodyUsesLocalBindingInsteadOfOuterScope() {
        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", intLiteral(1))),
                KGPrint(KGBinary(KGBindingReference("a"), "+", intLiteral(1)))
        )
        val file = KGFile(
                statements = listOf(
                        KGValDeclaration("a", intLiteral(200)),
                        wrapInMainMethod(letInExpr)
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            // If `a` were read from outer scope instead of let-scope, the result would be 201.
            assertEquals("2", output)
        }
    }

    // TODO - Once functions are backed by an Object, this should be possible
//    @Test fun testLetInExpression_fnBindingReusesNameFromOuterScope_bodyUsesLocalBindingInsteadOfOuterScope() {
//        val letInExpr = KGLetIn(
//                listOf(KGFnDeclaration("a", intLiteral(1))),
//                KGPrint(KGBinary(KGInvocation(KGBindingReference("a")), "+", intLiteral(1)))
//        )
//        val file = KGFile(
//                statements = listOf(
//                        KGFnDeclaration("a", intLiteral(200)),
//                        wrapInMainMethod(letInExpr)
//                ),
//                bindings = HashMap()
//        )
//
//        compileAndExecuteFileAnd(file) { output ->
//            // If `a` were read from outer scope instead of let-scope, the result would be 201.
//            assertEquals("2", output)
//        }
//    }
}

