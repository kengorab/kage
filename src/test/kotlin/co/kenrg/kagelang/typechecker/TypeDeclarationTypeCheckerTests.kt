package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.stringLiteral
import co.kenrg.kagelang.model.TypedName
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGType
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
        val typeDecl = KGTypeDeclaration(typeName, listOf(TypedName("prop1", "String"), TypedName("prop2", "Int")))
        val result = TypeChecker.typeCheck(typeDecl, ns)
        assertSucceedsAnd(result) {
            assertEquals(KGType.UNIT, typeDecl.type)

            val expectedType = KGType(
                    typeName,
                    "L${ns.name}\$$typeName;",
                    className = "${ns.name}\$$typeName",
                    props = mapOf(
                            "prop1" to KGType.STRING,
                            "prop2" to KGType.INT
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
                "L${ns.name}\$$otherTypeName;",
                className = "${ns.name}\$$otherTypeName",
                props = mapOf(
                        "someStr" to KGType.STRING
                )
        )
        ns.rootScope.types.put(otherTypeName, otherType)

        val typeName = RandomStringUtils.randomAlphabetic(16).capitalize()
        val typeDecl = KGTypeDeclaration(typeName, listOf(TypedName("prop1", otherTypeName)))
        val result = TypeChecker.typeCheck(typeDecl, ns)
        assertSucceedsAnd(result) {
            assertEquals(KGType.UNIT, typeDecl.type)

            val expectedType = KGType(
                    typeName,
                    "L${ns.name}\$$typeName;",
                    className = "${ns.name}\$$typeName",
                    props = mapOf(
                            "prop1" to otherType
                    )
            )
            assertEquals(expectedType, ns.rootScope.types[typeName])
        }
    }
}