package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.tree.types.KGType.PropType
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DotExpressionTypeCheckerTests {

    @Test fun testDotExpression_noPropsAvailableForTarget_typecheckingFails() {
        // type TYPE_NAME
        // let val a = TYPE_NAME() in a.someProp
        val ns = randomTCNamespace()

        val typeName = RandomStringUtils.randomAlphabetic(16).capitalize()
        val otherType = KGType(
                typeName,
                className = "${ns.name}\$$typeName",
                props = mapOf()
        )
        ns.rootScope.types.put(typeName, otherType)

        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", KGInvocation(KGBindingReference(typeName)))),
                KGDot(KGBindingReference("a"), "someProp")
        )
        val result = TypeChecker.typeCheck(letInExpr, ns)
        assertFails(result)
    }

    @Test fun testDotExpression_propsWithNameNotAvailableForTarget_typecheckingFails() {
        // type TYPE_NAME { someProp: String }
        // let val a = TYPE_NAME() in a.someOtherProp
        val ns = randomTCNamespace()

        val typeName = RandomStringUtils.randomAlphabetic(16).capitalize()
        val otherType = KGType(
                typeName,
                className = "${ns.name}\$$typeName",
                props = mapOf(
                        "someProp" to PropType(KGType.STRING, false)
                )
        )
        ns.rootScope.types.put(typeName, otherType)

        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a", KGInvocation(KGBindingReference(typeName)))),
                KGDot(KGBindingReference("a"), "someOtherProp")
        )
        val result = TypeChecker.typeCheck(letInExpr, ns)
        assertFails(result)
    }

    @Test fun testDotExpression_propsWithNameAvailableForTarget_passesTypecheckingWithTypeOfProp() {
        // type TYPE_NAME { someProp: String }
        // let val a = TYPE_NAME("some string") in a.someProp
        val ns = randomTCNamespace()

        val typeName = RandomStringUtils.randomAlphabetic(16).capitalize()
        val otherType = KGType(
                typeName,
                className = "${ns.name}\$$typeName",
                props = mapOf(
                        "someProp" to PropType(KGType.STRING, false)
                )
        )
        ns.rootScope.types.put(typeName, otherType)

        val letInExpr = KGLetIn(
                listOf(KGValDeclaration("a",
                        KGInvocation(KGBindingReference(typeName), listOf(stringLiteral("some string")))
                )),
                KGDot(KGBindingReference("a"), "someProp")
        )
        val result = TypeChecker.typeCheck(letInExpr, ns)
        assertSucceedsAnd(result) {
            assertEquals(KGType.STRING, it.type)
            assertEquals(KGType.STRING, letInExpr.type)
        }
    }
}