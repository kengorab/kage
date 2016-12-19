package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.model.Error2
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.Visitor
import co.kenrg.kagelang.tree.KGTree.VisitorErrorHandler
import co.kenrg.kagelang.tree.iface.base.Tree
import co.kenrg.kagelang.tree.types.KGTypeTag
import java.util.*

/**
 * An implementor of the Visitor interface, this visits each node in the tree
 * and determines the type of that node. If the node is correctly typed, it
 * will be "attributed" with that type (meaning, the `type` property of that
 * node will be set. Note, that the `type` property for any subtree defaults
 * to UNSET, and will remain that way unless processed by this class).
 *
 * After being visited by this Visitor, the tree will have its `type` property
 * set, as will all of its subtrees. If the typechecking was unsuccessful, this
 * class's `typeErrors` list will be nonempty.
 *
 * @see Visitor
 */
class TypeCheckerAttributorVisitor(
        val errorHandler: VisitorErrorHandler<Error2>? = null
) : Visitor<Map<String, KGTypeTag>>, VisitorErrorHandler<Error2> {

    val typeErrors = LinkedList<Error2>()
    override fun handleError(error: Error2) {
        typeErrors.add(error)
        errorHandler?.handleError(error)
    }

    fun isValid() = typeErrors.isEmpty()

    var result: KGTypeTag = KGTypeTag.UNSET

    fun attribExpr(tree: KGTree, data: Map<String, KGTypeTag>): KGTypeTag {
        tree.accept(this, data)
        return result
    }

    // Expression visitors

    override fun visitLiteral(literal: KGTree.KGLiteral, data: Map<String, KGTypeTag>) {
        literal.type = literal.typeTag
        result = literal.type
    }

    override fun visitParenthesized(parenthesized: KGTree.KGParenthesized, data: Map<String, KGTypeTag>) {
        val ownType = attribExpr(parenthesized.expr, data)
        parenthesized.type = ownType
        result = ownType
    }

    override fun visitUnary(unary: KGTree.KGUnary, data: Map<String, KGTypeTag>) {
        val exprType = attribExpr(unary.expr, data)

        var ownType = unary.type
        when (unary.kind()) {
            is Tree.Kind.ArithmeticNegation ->
                if (exprType == KGTypeTag.INT || exprType == KGTypeTag.DEC) {
                    ownType = exprType
                } else {
                    handleError(Error2(error = "Numeric type expected for arithmetic negation", position = unary.position.start))
                }
            is Tree.Kind.BooleanNegation ->
                if (exprType == KGTypeTag.BOOL) {
                    ownType = exprType
                } else {
                    handleError(Error2(error = "Boolean type expected for boolean negation", position = unary.position.start))
                }
        }
        unary.type = ownType
        result = ownType
    }

    override fun visitBinary(binary: KGTree.KGBinary, data: Map<String, KGTypeTag>) {
        val leftType = attribExpr(binary.left, data)
        val rightType = attribExpr(binary.right, data)

        var ownType = binary.type
        when (binary.kind()) {
            is Tree.Kind.ConditionalAnd,
            is Tree.Kind.ConditionalOr ->
                if (leftType == KGTypeTag.BOOL && rightType == KGTypeTag.BOOL) {
                    ownType = KGTypeTag.BOOL
                } else {
                    handleError(Error2(error = "Booleans expected", position = binary.position.start))
                }
            is Tree.Kind.Plus,
            is Tree.Kind.Minus,
            is Tree.Kind.Multiply ->
                if (!KGTypeTag.numericTypes.contains(leftType)) {
                    handleError(Error2(error = "Numeric type expected for left expression", position = binary.position.start))
                } else if (!KGTypeTag.numericTypes.contains(rightType)) {
                    handleError(Error2(error = "Numeric type expected for right expression", position = binary.position.start))
                } else if (leftType == KGTypeTag.INT && rightType == KGTypeTag.INT) {
                    ownType = KGTypeTag.INT
                } else if (leftType == KGTypeTag.DEC && rightType == KGTypeTag.DEC) {
                    ownType = KGTypeTag.DEC
                } else {
                    ownType = KGTypeTag.DEC
                }
            is Tree.Kind.Divide ->
                if (!KGTypeTag.numericTypes.contains(leftType)) {
                    handleError(Error2(error = "Numeric type expected for left expression", position = binary.position.start))
                } else if (!KGTypeTag.numericTypes.contains(rightType)) {
                    handleError(Error2(error = "Numeric type expected for right expression", position = binary.position.start))
                } else {
                    ownType = KGTypeTag.DEC
                }
            else ->
                throw UnsupportedOperationException("${binary.kind().javaClass.canonicalName} is not a BinaryTree")
        }

        binary.type = ownType
        result = ownType
    }

    // Statement visitors

    override fun visitPrint(print: KGTree.KGPrint, data: Map<String, KGTypeTag>) {
        // Typecheck the internal expression; type of print statement will always be Unit
        attribExpr(print.expr, data)
        result = KGTypeTag.UNIT
    }
}