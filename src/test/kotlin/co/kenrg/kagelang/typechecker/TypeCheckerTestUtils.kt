package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.tree.KGTree.KGLiteral
import co.kenrg.kagelang.tree.types.KGTypeTag
import co.kenrg.kagelang.tree.types.KGTypeTag.*
import org.apache.commons.lang3.RandomUtils
import org.junit.jupiter.api.Assertions.fail

fun assertSucceedsAnd(result: TypeCheckingResult, fn: (TypeCheckingResult.Success) -> Unit) {
    when (result) {
        is TypeCheckingResult.Success -> fn(result)
        is TypeCheckingResult.Failure -> fail("Typechecking should have passed, failed with errors: ${result.errors}")
    }
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
        DEC -> KGLiteral(DEC, RandomUtils.nextFloat())
        BOOL -> KGLiteral(BOOL, RandomUtils.nextBoolean())
        else -> throw UnsupportedOperationException("Supplied type $type is not a literal type")
    }
}
