package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.tree.KGTree.KGLiteral
import co.kenrg.kagelang.tree.types.KGTypeTag
import co.kenrg.kagelang.tree.types.KGTypeTag.*
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import org.junit.jupiter.api.Assertions.fail

fun randomTCNamespace() =
        TCNamespace.empty(RandomStringUtils.randomAlphabetic(16))

fun assertSucceedsAnd(result: TypeCheckingResult, fn: (TypeCheckingResult.Success) -> Unit) {
    when (result) {
        is TypeCheckingResult.Success -> fn(result)
        is TypeCheckingResult.Failure -> fail("Typechecking should have passed, failed with errors: ${result.errors}")
    }
}

fun assertSucceeds(result: TypeCheckingResult) {
    assertSucceedsAnd(result) {}
}

fun assertFails(result: TypeCheckingResult) {
    when (result) {
        is TypeCheckingResult.Success -> fail("Typechecking should have failed, passed with type: ${result.type}")
        is TypeCheckingResult.Failure -> Unit
    }
}

fun randomKGLiteralOfType(type: KGTypeTag): KGLiteral {
    return when (type) {
        INT -> KGLiteral(INT, RandomUtils.nextInt())
        DEC -> KGLiteral(DEC, RandomUtils.nextDouble(0.0, 1000000.0))
        BOOL -> KGLiteral(BOOL, RandomUtils.nextBoolean())
        STRING -> KGLiteral(STRING, RandomStringUtils.random(64))
        else -> throw UnsupportedOperationException("Supplied type $type is not a literal type")
    }
}
