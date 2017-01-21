package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.util.*

class IfThenElseCodeGenTests : BaseTest() {

    @Test fun testIfThenElseExpression_noElse_printInThen() {
        // if true then print("Hello World")
        val ifExpr = KGIfThenElse(
                trueLiteral(),
                KGPrint(stringLiteral("Hello World"))
        )
        val file = KGFile(
                statements = listOf(
                        wrapInMainMethod(ifExpr)
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("Hello World", output)
        }
    }

    @Test fun testIfThenElseExpression_printInThenAndElse_condIsTrue() {
        // if true then print("true statement") else print("false statement")
        val ifExpr = KGIfThenElse(
                trueLiteral(),
                KGPrint(stringLiteral("true statement")),
                KGPrint(stringLiteral("false statement"))
        )
        val file = KGFile(
                statements = listOf(
                        wrapInMainMethod(ifExpr)
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("true statement", output)
        }
    }

    @Test fun testIfThenElseExpression_printInThenAndElse_condIsFalse() {
        // if false then print("true statement") else print("false statement")
        val ifExpr = KGIfThenElse(
                falseLiteral(),
                KGPrint(stringLiteral("true statement")),
                KGPrint(stringLiteral("false statement"))
        )
        val file = KGFile(
                statements = listOf(
                        wrapInMainMethod(ifExpr)
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("false statement", output)
        }
    }

    @Test fun testIfThenElseExpression_bothBranchesReturnInt_condTrue_printResultOfExpression() {
        // val number = if true then 1 else -1
        // fn main(...) = print(number)
        val ifExpr = KGIfThenElse(
                trueLiteral(),
                intLiteral(1),
                intLiteral(-1)
        )
        val file = KGFile(
                statements = listOf(
                        KGValDeclaration("number", ifExpr),
                        wrapInMainMethod(KGPrint(KGBindingReference("number")))
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("1", output)
        }
    }

    @Test fun testIfThenElseExpression_nestedIfThenElseExpressions() {
        // val number = if true
        //   then (
        //     if false then 1 else -1
        //   )
        //   else 4
        // fn main(...) = print(number)
        val ifExpr = KGIfThenElse(
                trueLiteral(),
                KGIfThenElse(
                        falseLiteral(),
                        intLiteral(1),
                        intLiteral(-1)
                ),
                intLiteral(4)
        )
        val file = KGFile(
                statements = listOf(
                        KGValDeclaration("number", ifExpr),
                        wrapInMainMethod(KGPrint(KGBindingReference("number")))
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output ->
            assertEquals("-1", output)
        }
    }

    @TestFactory
    fun testIfThenElseExpression_conditionIsConditional(): List<DynamicTest> {
        return listOf(
                Triple("3 > 1", KGBinary(intLiteral(3), ">", intLiteral(1)), "true"),
                Triple("3 >= 1", KGBinary(intLiteral(3), ">=", intLiteral(1)), "true"),
                Triple("3 < 1", KGBinary(intLiteral(3), "<", intLiteral(1)), "false"),
                Triple("3 <= 1", KGBinary(intLiteral(3), "<=", intLiteral(1)), "false"),
                Triple("3 == 1", KGBinary(intLiteral(3), "==", intLiteral(1)), "false"),
                Triple("3 != 1", KGBinary(intLiteral(3), "!=", intLiteral(1)), "true")
        ).map {
            val (condRepr, condExpr, expectedResult) = it
            dynamicTest("The expression `if $condRepr then \"true\" else \"false\" should print $expectedResult") {
                val ifExpr = KGIfThenElse(condExpr, stringLiteral("true"), stringLiteral("false"))
                val file = KGFile(
                        statements = listOf(
                                wrapInMainMethod(KGPrint(ifExpr))
                        ),
                        bindings = HashMap()
                )

                compileAndExecuteFileAnd(file) { output -> assertEquals(expectedResult, output) }
            }
        }
    }

    @Test fun testIfThenElseExpression_thenAndElseHaveLetInExpressions() {
        /*
          fn thisDoesNotWork() =
            if 1 > 3
              then
                let
                  val a = 123
                in
                  print(a)
              else
                let
                  val b = 456
                in
                  print(b)
          */
        val fn = KGFnDeclaration("func", KGIfThenElse(
                KGBinary(intLiteral(1), ">", intLiteral(3)),
                KGLetIn(
                        listOf(KGValDeclaration("a", intLiteral(123))),
                        KGPrint(KGBindingReference("a"))
                ),
                KGLetIn(
                        listOf(KGValDeclaration("b", intLiteral(456))),
                        KGPrint(KGBindingReference("b"))
                )
        ))
        val file = KGFile(
                statements = listOf(
                        fn,
                        wrapInMainMethod(KGInvocation(KGBindingReference("func")))
                ),
                bindings = HashMap()
        )

        compileAndExecuteFileAnd(file) { output -> assertEquals("456", output) }
    }

    // TODO - When binding names are allowed to be duplicated between different branches, this test should pass.
//    @Test fun testIfThenElseExpression_thenAndElseHaveLetInExpressions_letBindingsUseSameVariables() {
//        /*
//          fn func() =
//            if 1 > 3
//            then
//              let
//                val a = 123
//              in
//                print(a)
//            else
//              let
//                val a = 456
//              in
//                print(a)
//          */
//        val fn = KGFnDeclaration("func", KGIfThenElse(
//                KGBinary(intLiteral(1), ">", intLiteral(3)),
//                KGLetIn(
//                        listOf(KGValDeclaration("a", intLiteral(123))),
//                        KGPrint(KGBindingReference("a"))
//                ),
//                KGLetIn(
//                        listOf(KGValDeclaration("a", intLiteral(456))),
//                        KGPrint(KGBindingReference("a"))
//                )
//        ))
//        val file = KGFile(
//                statements = listOf(
//                        fn,
//                        wrapInMainMethod(KGInvocation(KGBindingReference("func")))
//                ),
//                bindings = HashMap()
//        )
//
//        compileAndExecuteFileAnd(file) { output -> assertEquals("456", output) }
//    }
}
