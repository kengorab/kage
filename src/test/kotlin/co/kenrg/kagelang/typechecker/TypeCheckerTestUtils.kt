package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.KGLiteral
import co.kenrg.kagelang.tree.types.KGType
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

/**
 * Extension function, used to explicitly set the type of an expression during tests, for expressions that
 * will not be run through typechecking, i.e. vals/fns inserted directly into scope.
 */
fun KGTree.KGExpression.withType(type: KGType): KGTree.KGExpression {
    this.type = type
    return this
}

fun randomKGLiteralOfType(type: KGType): KGLiteral {
    return when (type) {
        KGType.INT -> KGLiteral(KGType.INT, RandomUtils.nextInt())
        KGType.DEC -> KGLiteral(KGType.DEC, RandomUtils.nextDouble(0.0, 1000000.0))
        KGType.BOOL -> KGLiteral(KGType.BOOL, RandomUtils.nextBoolean())
        KGType.STRING -> KGLiteral(KGType.STRING, RandomStringUtils.random(64))
        else -> throw UnsupportedOperationException("Supplied type $type is not a literal type")
    }
}
