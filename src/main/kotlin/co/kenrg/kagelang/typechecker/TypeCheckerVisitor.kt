//package co.kenrg.kagelang.typechecker
//
//import co.kenrg.kagelang.model.Error
//import co.kenrg.kagelang.tree.KGTree
//import co.kenrg.kagelang.tree.KGTree.Visitor
//import co.kenrg.kagelang.tree.KGTree.VisitorErrorHandler
//import co.kenrg.kagelang.tree.iface.base.Tree
//import co.kenrg.kagelang.tree.types.KGTypeTag
//import java.util.*
//
//class TypeCheckerVisitor(
//        val errorHandler: VisitorErrorHandler<Error>? = null
//) : Visitor<Map<String, KGTypeTag>, KGTypeTag?>, VisitorErrorHandler<Error> {
//
//    val typeErrors = LinkedList<Error>()
//    override fun handleError(error: Error) {
//        typeErrors.add(error)
//        errorHandler?.handleError(error)
//    }
//
//    // Expression visitors
//
//    override fun visitLiteral(literal: KGTree.KGLiteral, data: Map<String, KGTypeTag>) = literal.typeTag
//
//    override fun visitParenthesized(parenthesized: KGTree.KGParenthesized, data: Map<String, KGTypeTag>) =
//            parenthesized.innerExpression().accept(this, data)
//
//    override fun visitBinary(binary: KGTree.KGBinary, data: Map<String, KGTypeTag>): KGTypeTag? {
//        val leftType = binary.getLeftExpression().accept(this, data)
//        val rightType = binary.getRightExpression().accept(this, data)
//        when (binary.kind()) {
//            is Tree.Kind.ConditionalAnd,
//            is Tree.Kind.ConditionalOr ->
//                if (leftType == KGTypeTag.BOOL && rightType == KGTypeTag.BOOL) {
//                    return KGTypeTag.BOOL
//                } else {
//                    handleError(Error(error = "Booleans expected", position = null))
//                    return null
//                }
//            is Tree.Kind.Plus,
//            is Tree.Kind.Minus,
//            is Tree.Kind.Multiply ->
//                if (!KGTypeTag.numericTypes.contains(leftType)) {
//                    handleError(Error(error = "Numeric type expected for left expression", position = null))
//                    return null
//                } else if (!KGTypeTag.numericTypes.contains(rightType)) {
//                    handleError(Error(error = "Numeric type expected for right expression", position = null))
//                    return null
//                } else if (leftType == KGTypeTag.INT && rightType == KGTypeTag.INT) {
//                    return KGTypeTag.INT
//                } else if (leftType == KGTypeTag.DEC && rightType == KGTypeTag.DEC) {
//                    return KGTypeTag.DEC
//                } else {
//                    return KGTypeTag.DEC
//                }
//            is Tree.Kind.Divide ->
//                if (!KGTypeTag.numericTypes.contains(leftType)) {
//                    handleError(Error(error = "Numeric type expected for left expression", position = null))
//                    return null
//                } else if (!KGTypeTag.numericTypes.contains(rightType)) {
//                    handleError(Error(error = "Numeric type expected for right expression", position = null))
//                    return null
//                } else {
//                    return KGTypeTag.DEC
//                }
//            else ->
//                throw UnsupportedOperationException("${binary.kind().javaClass.canonicalName} is not a BinaryTree")
//        }
//    }
//
//    // Statement visitors
//
//    override fun visitPrint(print: KGTree.KGPrint, data: Map<String, KGTypeTag>): KGTypeTag? {
//        // Typecheck the internal expression; type of print statement will always be Unit
//        print.expression().accept(this, data)
//        return KGTypeTag.UNIT
//    }
//}