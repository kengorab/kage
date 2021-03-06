package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.tree.KGTree.KGBinary
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.tree.types.KGType.Companion.BOOL
import co.kenrg.kagelang.tree.types.KGType.Companion.DEC
import co.kenrg.kagelang.tree.types.KGType.Companion.INT
import co.kenrg.kagelang.tree.types.KGType.Companion.STRING
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory

class BinaryTypeCheckerTests {

    @Nested
    @DisplayName("Arithmetic Binary Operations (+, -, *, /)")
    inner class ArithmeticBinaryOperationsTests {

        @TestFactory
        @DisplayName("(+, -, *) should determine the correct type for its inputs' types")
        fun testStandardBinaryOps_withNumericTypes(): List<DynamicTest> {
            data class Case(val left: KGType, val right: KGType, val expected: KGType)

            return listOf(
                    Case(left = INT, right = INT, expected = INT), Case(left = INT, right = DEC, expected = DEC),
                    Case(left = DEC, right = INT, expected = DEC), Case(left = DEC, right = DEC, expected = DEC)
            ).flatMap { testCase ->
                val (left, right, expected) = testCase
                listOf("+", "-", "*").flatMap { operator ->
                    listOf(
                            dynamicTest("$left $operator $right should yield $expected") {
                                val leftExpr = randomKGLiteralOfType(left)
                                val rightExpr = randomKGLiteralOfType(right)

                                val binary = KGBinary(leftExpr, operator, rightExpr)
                                val result = TypeChecker.typeCheck(binary, randomTCNamespace())
                                assertSucceedsAnd(result) { assertEquals(expected, it.type) }
                            },
                            dynamicTest("Binary should have type $expected, left $left and right $right") {
                                val leftExpr = randomKGLiteralOfType(left)
                                val rightExpr = randomKGLiteralOfType(right)

                                val binary = KGBinary(leftExpr, operator, rightExpr)
                                val result = TypeChecker.typeCheck(binary, randomTCNamespace())
                                assertSucceedsAnd(result) {
                                    assertEquals(left, binary.left.type)
                                    assertEquals(right, binary.right.type)
                                    assertEquals(expected, binary.type)
                                }
                            }
                    )
                }
            }
        }

        @TestFactory
        @DisplayName("(/) should always yield the DEC type, regardless of numeric input type")
        fun testDivisionBinaryOp_withNumericTypes(): List<DynamicTest> {
            data class Case(val left: KGType, val right: KGType)

            return listOf(
                    Case(left = INT, right = INT), Case(left = INT, right = DEC),
                    Case(left = DEC, right = INT), Case(left = DEC, right = DEC)
            ).flatMap { testCase ->
                val (left, right) = testCase
                listOf(
                        dynamicTest("$left / $right should yield DEC") {
                            val leftExpr = randomKGLiteralOfType(left)
                            val rightExpr = randomKGLiteralOfType(right)

                            val binary = KGBinary(leftExpr, "/", rightExpr)
                            val result = TypeChecker.typeCheck(binary, randomTCNamespace())
                            assertSucceedsAnd(result) { assertEquals(DEC, it.type) }
                        },
                        dynamicTest("Binary should have type DEC, left $left and right $right") {
                            val leftExpr = randomKGLiteralOfType(left)
                            val rightExpr = randomKGLiteralOfType(right)

                            val binary = KGBinary(leftExpr, "/", rightExpr)
                            val result = TypeChecker.typeCheck(binary, randomTCNamespace())
                            assertSucceedsAnd(result) {
                                assertEquals(left, binary.left.type)
                                assertEquals(right, binary.right.type)
                                assertEquals(DEC, binary.type)
                            }
                        }
                )
            }
        }

        @TestFactory
        @DisplayName("(+, -, *, /) should not typecheck if either input is non-numeric (BOOL, STRING)")
        fun testNumericBinaryOps_withBoolInputs_failsTypecheck(): List<DynamicTest> {
            data class Case(val left: KGType, val right: KGType)

            return listOf(
                    Case(left = INT, right = BOOL), Case(left = DEC, right = BOOL),
                    Case(left = BOOL, right = INT), Case(left = BOOL, right = DEC),
                    Case(left = BOOL, right = BOOL),

                    Case(left = INT, right = STRING), Case(left = DEC, right = STRING),
                    Case(left = STRING, right = INT), Case(left = STRING, right = DEC),
                    Case(left = STRING, right = STRING)
            ).flatMap { testCase ->
                val (left, right) = testCase
                listOf("+", "-", "*", "/").map { operation ->
                    dynamicTest("$left $operation $right should fail to typecheck") {
                        val leftExpr = randomKGLiteralOfType(left)
                        val rightExpr = randomKGLiteralOfType(right)

                        val binary = KGBinary(leftExpr, operation, rightExpr)
                        val result = TypeChecker.typeCheck(binary, randomTCNamespace())
                        assertFails(result)
                    }

                }
            }
        }
    }

    @Nested
    @DisplayName("Boolean Binary Operations (&&, ||)")
    inner class BooleanBinaryOperationsTests {

        @TestFactory
        @DisplayName("Conditional Binary operations (&&, ||) should return BOOL if given BOOLs")
        fun testBinaryConditionalOps_withBoolTypes(): List<DynamicTest> {
            return listOf("&&", "||").flatMap { operation ->
                listOf(
                        dynamicTest("BOOL $operation BOOL should yield BOOL") {
                            val leftExpr = randomKGLiteralOfType(BOOL)
                            val rightExpr = randomKGLiteralOfType(BOOL)

                            val binary = KGBinary(leftExpr, operation, rightExpr)
                            val result = TypeChecker.typeCheck(binary, randomTCNamespace())
                            assertSucceedsAnd(result) { assertEquals(BOOL, it.type) }
                        },
                        dynamicTest("Binary should have type BOOL, left BOOL and right BOOL") {
                            val leftExpr = randomKGLiteralOfType(BOOL)
                            val rightExpr = randomKGLiteralOfType(BOOL)

                            val binary = KGBinary(leftExpr, operation, rightExpr)
                            val result = TypeChecker.typeCheck(binary, randomTCNamespace())
                            assertSucceedsAnd(result) {
                                assertEquals(BOOL, binary.left.type)
                                assertEquals(BOOL, binary.right.type)
                                assertEquals(BOOL, binary.type)
                            }
                        }
                )
            }
        }

        @TestFactory
        @DisplayName("Conditional Binary operations (&&, ||) should fail to typecheck if given non-BOOL input")
        fun testBinaryConditionalOps_nonBoolInputs_failsTypecheck(): List<DynamicTest> {
            data class Case(val left: KGType, val right: KGType)

            return listOf(
                    Case(left = INT, right = INT), Case(left = INT, right = DEC), Case(left = INT, right = BOOL), Case(left = INT, right = STRING),
                    Case(left = DEC, right = INT), Case(left = DEC, right = DEC), Case(left = DEC, right = BOOL), Case(left = DEC, right = STRING),
                    Case(left = STRING, right = INT), Case(left = STRING, right = DEC), Case(left = STRING, right = BOOL), Case(left = STRING, right = STRING),
                    Case(left = BOOL, right = INT), Case(left = BOOL, right = DEC), Case(left = BOOL, right = STRING)
            ).flatMap { testCase ->
                val (left, right) = testCase
                listOf("&&", "||").map { operation ->
                    dynamicTest("$left $operation $right should fail to typecheck") {
                        val leftExpr = randomKGLiteralOfType(left)
                        val rightExpr = randomKGLiteralOfType(right)

                        val binary = KGBinary(leftExpr, operation, rightExpr)
                        val result = TypeChecker.typeCheck(binary, randomTCNamespace())
                        assertFails(result)
                    }
                }
            }
        }

        @TestFactory
        @DisplayName("Boolean comparisons (>, >=, <, <=, ==, !=) should pass typechecking, with type BOOL, if given comparable inputs")
        fun testBooleanComparison_comparableInputs_passesTypecheckWithTypeBool(): List<DynamicTest> {
            data class Case(val left: KGType, val right: KGType)

            return listOf(
                    Case(left = INT, right = INT), Case(left = INT, right = DEC),
                    Case(left = DEC, right = INT), Case(left = DEC, right = DEC),
                    Case(left = STRING, right = STRING)
            ).flatMap { testCase ->
                val (left, right) = testCase
                listOf(">", ">=", "<", "<=", "==", "!=").map { operation ->
                    dynamicTest("$left $operation $right typecheck to Bool") {
                        val leftExpr = randomKGLiteralOfType(left)
                        val rightExpr = randomKGLiteralOfType(right)

                        val binary = KGBinary(leftExpr, operation, rightExpr)
                        val result = TypeChecker.typeCheck(binary, randomTCNamespace())
                        assertSucceedsAnd(result) {
                            assertEquals(BOOL, binary.type)
                        }
                    }
                }
            }
        }

        @TestFactory
        @DisplayName("Boolean comparisons (>, >=, <, <=, ==, !=) should fail typechecking, if given non-comparable inputs")
        fun testBooleanComparison_nonComparableInputs_failsTypechecking(): List<DynamicTest> {
            data class Case(val left: KGType, val right: KGType)

            return listOf(
                    Case(left = INT, right = BOOL), Case(left = INT, right = STRING),
                    Case(left = DEC, right = BOOL), Case(left = DEC, right = STRING),
                    Case(left = STRING, right = INT), Case(left = STRING, right = DEC), Case(left = STRING, right = BOOL),
                    Case(left = BOOL, right = INT), Case(left = BOOL, right = DEC), Case(left = BOOL, right = STRING)
            ).flatMap { testCase ->
                val (left, right) = testCase
                listOf(">", ">=", "<", "<=", "==", "!=").map { operation ->
                    dynamicTest("$left $operation $right should fail to typecheck") {
                        val leftExpr = randomKGLiteralOfType(left)
                        val rightExpr = randomKGLiteralOfType(right)

                        val binary = KGBinary(leftExpr, operation, rightExpr)
                        val result = TypeChecker.typeCheck(binary, randomTCNamespace())
                        assertFails(result)
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("List-like Binary Operations (++)")
    inner class ListlikeBinaryOperationsTests {

        @TestFactory
        @DisplayName("(++) with Strings should be String")
        fun testConcatenationOfStrings(): List<DynamicTest> {
            data class Case(val left: KGType, val right: KGType, val expected: KGType)

            return listOf(
                    Case(left = STRING, right = STRING, expected = STRING),
                    Case(left = INT, right = STRING, expected = STRING), Case(left = STRING, right = INT, expected = STRING),
                    Case(left = DEC, right = STRING, expected = STRING), Case(left = STRING, right = DEC, expected = STRING),
                    Case(left = BOOL, right = STRING, expected = STRING), Case(left = STRING, right = BOOL, expected = STRING)
            ).flatMap { testCase ->
                val (left, right, expected) = testCase
                listOf("++").flatMap { operator ->
                    listOf(
                            dynamicTest("$left $operator $right should yield $expected") {
                                val leftExpr = randomKGLiteralOfType(left)
                                val rightExpr = randomKGLiteralOfType(right)

                                val binary = KGBinary(leftExpr, operator, rightExpr)
                                val result = TypeChecker.typeCheck(binary, randomTCNamespace())
                                assertSucceedsAnd(result) { assertEquals(expected, it.type) }
                            },
                            dynamicTest("Binary should have type $expected, left $left and right $right") {
                                val leftExpr = randomKGLiteralOfType(left)
                                val rightExpr = randomKGLiteralOfType(right)

                                val binary = KGBinary(leftExpr, operator, rightExpr)
                                val result = TypeChecker.typeCheck(binary, randomTCNamespace())
                                assertSucceedsAnd(result) {
                                    assertEquals(left, binary.left.type)
                                    assertEquals(right, binary.right.type)
                                    assertEquals(expected, binary.type)
                                }
                            }
                    )
                }
            }
        }
    }
}