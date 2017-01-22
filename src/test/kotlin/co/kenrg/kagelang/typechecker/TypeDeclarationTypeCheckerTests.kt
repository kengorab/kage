package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.stringLiteral
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

    @Test fun typecheckTypeDeclaration_emptyType_nameAlreadyExistsInNamespaceTypes_typecheckingFails() {
        val typeName = RandomStringUtils.randomAlphabetic(16).capitalize()

        val ns = randomTCNamespace()
        ns.rootScope.types.put(typeName, TCType(typeName))

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
}