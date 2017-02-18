package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.model.TypeIdentifier
import co.kenrg.kagelang.model.TypedName
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.tree.types.KGType.PropType
import co.kenrg.kagelang.tree.types.StdLibType
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TypeDeclarationTypeCheckerTests {
    @Test fun typecheckTypeDeclaration_emptyType_notAtRootScope_typecheckingFails() {
        val ns = randomTCNamespace()

        val typeName = RandomStringUtils.randomAlphabetic(16).capitalize()
        val typeDecl = KGTypeDeclaration(typeName)

        val letInExpr = KGLetIn(listOf(typeDecl), KGPrint(stringLiteral("Should fail")))
        val result = TypeChecker.typeCheck(letInExpr, ns)
        assertFails(result)
    }

    @Test fun typecheckTypeDeclaration_typeNameIsNotCapitalized_typecheckingFails() {
        val ns = randomTCNamespace()

        val typeName = RandomStringUtils.randomAlphabetic(16).toLowerCase()
        val typeDecl = KGTypeDeclaration(typeName)

        val letInExpr = KGLetIn(listOf(typeDecl), KGPrint(stringLiteral("Should fail")))
        val result = TypeChecker.typeCheck(letInExpr, ns)
        assertFails(result)
    }

    @Test fun typecheckTypeDeclaration_typeWithNameExistsInNamespace_typecheckingFails() {
        val typeName = RandomStringUtils.randomAlphabetic(16).capitalize()

        val ns = randomTCNamespace()
        ns.rootScope.types.put(typeName, KGType(typeName, ""))

        val typeDecl = KGTypeDeclaration(typeName)
        val result = TypeChecker.typeCheck(typeDecl, ns)
        assertFails(result)
    }

    @Test fun typecheckTypeDeclaration_emptyType_passesTypecheckingWithTypeUnit() {
        val ns = randomTCNamespace()

        val typeName = RandomStringUtils.randomAlphabetic(16).capitalize()
        val typeDecl = KGTypeDeclaration(typeName)
        val result = TypeChecker.typeCheck(typeDecl, ns)
        assertSucceedsAnd(result) { assertEquals(KGType.UNIT, typeDecl.type) }
    }

    @Test fun typecheckTypeDeclaration_typeWithProps_passesTypecheckingWithTypeInNamespace() {
        val ns = randomTCNamespace()

        val typeName = RandomStringUtils.randomAlphabetic(16).capitalize()
        val typeDecl = KGTypeDeclaration(typeName, listOf(TypedName("prop1", TypeIdentifier("String")), TypedName("prop2", TypeIdentifier("Int"))))
        val result = TypeChecker.typeCheck(typeDecl, ns)
        assertSucceedsAnd(result) {
            assertEquals(KGType.UNIT, typeDecl.type)

            val expectedType = KGType(
                    typeName,
                    className = "${ns.name}\$$typeName",
                    props = mapOf(
                            "prop1" to PropType(KGType.STRING, false),
                            "prop2" to PropType(KGType.INT, false)
                    )
            )
            assertEquals(expectedType, ns.rootScope.types[typeName])
        }
    }

    @Test fun typecheckTypeDeclaration_typeWithProp_propIsCustomType_passesTypecheckingWithTypeInNamespace() {
        val ns = randomTCNamespace()

        val otherTypeName = RandomStringUtils.randomAlphabetic(16).capitalize()
        val otherType = KGType(
                otherTypeName,
                className = "${ns.name}\$$otherTypeName",
                props = mapOf(
                        "someStr" to PropType(KGType.STRING, false)
                )
        )
        ns.rootScope.types.put(otherTypeName, otherType)

        val typeName = RandomStringUtils.randomAlphabetic(16).capitalize()
        val typeDecl = KGTypeDeclaration(typeName, listOf(TypedName("prop1", TypeIdentifier(otherTypeName))))
        val result = TypeChecker.typeCheck(typeDecl, ns)
        assertSucceedsAnd(result) {
            assertEquals(KGType.UNIT, typeDecl.type)

            val expectedType = KGType(
                    typeName,
                    className = "${ns.name}\$$typeName",
                    props = mapOf(
                            "prop1" to PropType(otherType, false)
                    )
            )
            assertEquals(expectedType, ns.rootScope.types[typeName])
        }
    }

    @Test fun typecheckTypeDeclaration_typeHasPropOfParametrizedType_passesTypechecking() {
        val ns = randomTCNamespace()
        val typeName = RandomStringUtils.randomAlphabetic(16).capitalize()
        val typeDecl = KGTypeDeclaration(typeName, listOf(TypedName("intStringPair", TypeIdentifier("Pair", listOf(TypeIdentifier("Int"), TypeIdentifier("String"))))))
        val result = TypeChecker.typeCheck(typeDecl, ns)
        assertSucceedsAnd(result) {
            assertEquals(KGType.UNIT, typeDecl.type)

            val expectedType = KGType(
                    typeName,
                    className = "${ns.name}\$$typeName",
                    props = mapOf(
                            "intStringPair" to PropType(KGType.stdLibType(StdLibType.Pair, typeParams = listOf(KGType.INT, KGType.STRING)), false)
                    )
            )
            assertEquals(expectedType, ns.rootScope.types[typeName])
        }
    }
}