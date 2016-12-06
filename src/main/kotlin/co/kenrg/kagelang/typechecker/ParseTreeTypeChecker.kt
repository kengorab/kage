// TODO - Remove this file, it doesn't do anything
package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.ast.*
import co.kenrg.kagelang.model.Error
import java.util.*

data class Var(val type: KageType, val index: Int)

enum class KageType {
    Unit,
    Int,
    Decimal,
    Bool
}

data class VarsResult(val vars: Map<String, Var>? = null, val errors: List<Error>? = null)

data class TypeCheckResult(val type: KageType? = null, val errors: List<Error>? = null)

object ParseTreeTypeChecker {
    fun typeCheck(root: KageFile): VarsResult {
        val (vars, errs) = getVars(root)
        return if (errs != null) {
            VarsResult(errors = errs)
        } else if (vars != null) {
            VarsResult(vars = vars)
        } else {
            throw IllegalStateException()
        }
    }

    // @VisibleForTesting
    fun getVars(root: KageFile): VarsResult {
        val errors = LinkedList<Error>()
        val vars = HashMap<String, Var>()

        root.statements
                .filterIsInstance(VarDeclarationStatement::class.java)
                .forEachIndexed { index, stmt ->
                    val (type, errs) = typeForExpression(stmt.value, vars)
                    if (errs != null) {
                        errors.addAll(errors)
                    } else if (type != null) {
                        vars.putIfAbsent(stmt.varName, Var(type, index))
                    } else {
                        throw IllegalStateException("No available type, and no errors while type-checking ${stmt.varName}")
                    }
                }

        return VarsResult(vars = vars, errors = errors)
    }

    // @VisibleForTesting
    fun typeForExpression(expression: Expression, vars: Map<String, Var>): TypeCheckResult {
        return when (expression) {
            is IntLiteralExpression -> TypeCheckResult(type = KageType.Int)
            is DecimalLiteralExpression -> TypeCheckResult(type = KageType.Decimal)
            is BoolLiteralExpression -> TypeCheckResult(type = KageType.Bool)
            is VarReferenceExpression ->
                if (vars.containsKey(expression.varName)) {
                    TypeCheckResult(type = vars[expression.varName]!!.type)
                } else {
                    val error = Error("Cannot find variable named ${expression.varName}", expression.position?.start)
                    TypeCheckResult(errors = listOf(error))
                }
            is UnaryMinusExpression -> typeForExpression(expression, vars)
            is BinaryExpression -> {
                val (leftType, leftError) = typeForExpression(expression.left, vars)
                if (leftError != null) return TypeCheckResult(errors = leftError)

                val (rightType, rightError) = typeForExpression(expression.right, vars)
                if (rightError != null) return TypeCheckResult(errors = rightError)

                if (leftType == null || rightType == null)
                    throw IllegalStateException("Somehow there were no errors, yet left or right types were null...")

                when (expression) {
                    is SumExpression,
                    is SubtractionExpression,
                    is MultiplicationExpression,
                    is DivisionExpression -> {
                        if (leftType == KageType.Int && rightType == KageType.Int)
                            TypeCheckResult(type = KageType.Int)
                        else if (leftType == KageType.Decimal && rightType == KageType.Decimal)
                            TypeCheckResult(type = KageType.Decimal)
                        else if (leftType == KageType.Bool)
                            TypeCheckResult(errors = listOf(Error("Expected number, got boolean", expression.left.position?.start)))
                        else if (rightType == KageType.Bool)
                            TypeCheckResult(errors = listOf(Error("Expected number, got boolean", expression.right.position?.start)))
                        else
                            TypeCheckResult(errors = listOf(Error("Unexpected types $leftType and $rightType", expression.left.position?.start)))
                    }
                    is BooleanOrExpression,
                    is BooleanAndExpression -> {
                        if (leftType == KageType.Bool && rightType == KageType.Bool)
                            TypeCheckResult(type = KageType.Bool)
                        else if (leftType != KageType.Bool)
                            TypeCheckResult(errors = listOf(Error("Expected Bool, got $leftType", expression.left.position?.start)))
                        else if (rightType != KageType.Bool)
                            TypeCheckResult(errors = listOf(Error("Expected Bool, got $rightType", expression.right.position?.start)))
                        else
                            TypeCheckResult(errors = listOf(Error("Unexpected types $leftType and $rightType", expression.left.position?.start)))
                    }
                    else -> throw IllegalStateException("${expression.javaClass.canonicalName} is a subclass of BinaryExpression, but has no type checking implementation available")
                }
            }
            else -> throw UnsupportedOperationException("No typechecking available for ${expression.javaClass.canonicalName}")
        }
    }
}