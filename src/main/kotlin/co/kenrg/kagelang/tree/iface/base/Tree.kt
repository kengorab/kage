package co.kenrg.kagelang.tree.iface.base

import co.kenrg.kagelang.model.Position
import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.iface.*
import co.kenrg.kagelang.tree.types.KGTypeTag

interface Tree {

    sealed class Kind<T : Tree>(val associatedTreeIface: Class<T>) {
        // Top-level
        object TopLevel : Kind<KGFile>(KGFile::class.java)

        // Literal Kinds
        object IntLiteral : Kind<LiteralTree>(LiteralTree::class.java)
        object DecLiteral : Kind<LiteralTree>(LiteralTree::class.java)
        object BoolLiteral : Kind<LiteralTree>(LiteralTree::class.java)
        object StringLiteral : Kind<LiteralTree>(LiteralTree::class.java)

        // Binary Operation Kinds
        object Plus : Kind<BinaryTree>(BinaryTree::class.java)
        object Minus : Kind<BinaryTree>(BinaryTree::class.java)
        object Multiply : Kind<BinaryTree>(BinaryTree::class.java)
        object Divide : Kind<BinaryTree>(BinaryTree::class.java)
        object ConditionalAnd : Kind<BinaryTree>(BinaryTree::class.java)
        object ConditionalOr : Kind<BinaryTree>(BinaryTree::class.java)

        // Binding Kinds
        object BindingReference : Kind<BindingReferenceTree>(BindingReferenceTree::class.java)

        // Unary Operation Kinds
        object ArithmeticNegation : Kind<UnaryTree>(UnaryTree::class.java)
        object BooleanNegation : Kind<UnaryTree>(UnaryTree::class.java)

        // Statement Kinds
        object Print : Kind<PrintTree>(PrintTree::class.java)
        object ValDeclaration : Kind<ValDeclarationTree>(ValDeclarationTree::class.java)
    }

    fun kind(): Kind<*>

    var type: KGTypeTag
    var position: Position
}