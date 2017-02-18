package co.kenrg.kagelang.typechecker.stdlib

import co.kenrg.kagelang.codegen.decLiteral
import co.kenrg.kagelang.codegen.intLiteral
import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.codegen.trueLiteral
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.tree.types.KGType.PropType
import co.kenrg.kagelang.tree.types.StdLibType
import co.kenrg.kagelang.typechecker.TypeChecker
import co.kenrg.kagelang.typechecker.assertFails
import co.kenrg.kagelang.typechecker.assertSucceedsAnd
import co.kenrg.kagelang.typechecker.randomTCNamespace
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MaybeTypeCheckerTests {

    @Nested
    inner class None {
        @Test fun testNone_hasNoPropValue() {
            val propAccess = KGDot(KGInvocation(KGBindingReference("None"), listOf()), "value")
            val result = TypeChecker.typeCheck(propAccess, randomTCNamespace())
            assertFails(result)
        }

        @Test fun testNone_typeParamIsAny() {
            val noneExpr = KGInvocation(KGBindingReference("None"), listOf())
            val result = TypeChecker.typeCheck(noneExpr, randomTCNamespace())
            assertSucceedsAnd(result) {
                assertEquals(KGType.stdLibType(StdLibType.None, listOf()), it.type)
            }
        }
    }

    @Nested
    inner class Some {
        @Test fun testSome_hasPropValue() {
            val propAccess = KGDot(KGInvocation(KGBindingReference("Some"), listOf(stringLiteral("asdf"))), "value")
            val result = TypeChecker.typeCheck(propAccess, randomTCNamespace())
            assertSucceedsAnd(result) {
                assertEquals(KGType.STRING, it.type)
            }
        }

        @Test fun testSome_paramIsIntLiteral_typeParamIsInt() {
            val someExpr = KGInvocation(KGBindingReference("Some"), listOf(intLiteral(2)))
            val result = TypeChecker.typeCheck(someExpr, randomTCNamespace())
            assertSucceedsAnd(result) {
                assertEquals(KGType.stdLibType(StdLibType.Some, listOf(KGType.INT)), it.type)
            }
        }

        @Test fun testSome_paramIsDecLiteral_typeParamIsDec() {
            val someExpr = KGInvocation(KGBindingReference("Some"), listOf(decLiteral(2.1)))
            val result = TypeChecker.typeCheck(someExpr, randomTCNamespace())
            assertSucceedsAnd(result) {
                assertEquals(KGType.stdLibType(StdLibType.Some, listOf(KGType.DEC)), it.type)
            }
        }

        @Test fun testSome_paramIsBoolLiteral_typeParamIsBool() {
            val someExpr = KGInvocation(KGBindingReference("Some"), listOf(trueLiteral()))
            val result = TypeChecker.typeCheck(someExpr, randomTCNamespace())
            assertSucceedsAnd(result) {
                assertEquals(KGType.stdLibType(StdLibType.Some, listOf(KGType.BOOL)), it.type)
            }
        }

        @Test fun testSome_paramIsStringLiteral_typeParamIsString() {
            val someExpr = KGInvocation(KGBindingReference("Some"), listOf(stringLiteral("asdf")))
            val result = TypeChecker.typeCheck(someExpr, randomTCNamespace())
            assertSucceedsAnd(result) {
                assertEquals(KGType.stdLibType(StdLibType.Some, listOf(KGType.STRING)), it.type)
            }
        }

        @Test fun testSome_paramIsSome_typeParamIsCorrect() {
            val someExpr = KGInvocation(KGBindingReference("Some"), listOf(KGInvocation(KGBindingReference("Some"), listOf(stringLiteral("asdf")))))
            val result = TypeChecker.typeCheck(someExpr, randomTCNamespace())
            assertSucceedsAnd(result) {
                assertEquals(KGType.stdLibType(StdLibType.Some, listOf(KGType.stdLibType(StdLibType.Some, listOf(KGType.STRING)))), it.type)
            }
        }

        @Test fun testSome_paramIsCustomTypeInstance_typeParamIsCustomType() {
            val ns = randomTCNamespace()

            val typeName = RandomStringUtils.randomAlphabetic(16).capitalize()
            val type = KGType(
                    typeName,
                    className = "${ns.name}\$$typeName",
                    props = mapOf(
                            "someStr" to PropType(KGType.STRING, false)
                    )
            )
            ns.rootScope.types.put(typeName, type)

            val someExpr = KGLetIn(
                    listOf(
                            KGValDeclaration("a", KGInvocation(KGBindingReference(typeName), listOf(stringLiteral("asdf"))))
                    ),
                    KGInvocation(KGBindingReference("Some"), listOf(KGBindingReference("a")))
            )
            val result = TypeChecker.typeCheck(someExpr, ns)
            assertSucceedsAnd(result) {
                assertEquals(KGType.stdLibType(StdLibType.Some, listOf(type)), it.type)
            }
        }
    }
}