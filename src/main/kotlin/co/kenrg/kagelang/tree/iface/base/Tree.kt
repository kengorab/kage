package co.kenrg.kagelang.tree.iface.base

import co.kenrg.kagelang.model.Position
import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.iface.*
import co.kenrg.kagelang.tree.types.KGType

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
        object Concatenation : Kind<BinaryTree>(BinaryTree::class.java)
        object GreaterThan : Kind<BinaryTree>(BinaryTree::class.java)
        object LessThan : Kind<BinaryTree>(BinaryTree::class.java)
        object GreaterThanOrEqualTo : Kind<BinaryTree>(BinaryTree::class.java)
        object LessThanOrEqualTo : Kind<BinaryTree>(BinaryTree::class.java)
        object Equals : Kind<BinaryTree>(BinaryTree::class.java)
        object NotEquals : Kind<BinaryTree>(BinaryTree::class.java)

        // Binding Kinds
        object BindingReference : Kind<BindingReferenceTree>(BindingReferenceTree::class.java)
        object Invocation : Kind<InvocationTree>(InvocationTree::class.java)

        // Unary Operation Kinds
        object ArithmeticNegation : Kind<UnaryTree>(UnaryTree::class.java)
        object BooleanNegation : Kind<UnaryTree>(UnaryTree::class.java)

        // Other Expression Kinds
        object LetIn : Kind<LetInTree>(LetInTree::class.java)
        object IfElse : Kind<IfThenElseTree>(IfThenElseTree::class.java)
        object Dot : Kind<DotTree>(DotTree::class.java)
        object Index : Kind<IndexTree>(IndexTree::class.java)
        object Tuple : Kind<TupleTree>(TupleTree::class.java)
        object Array : Kind<ArrayTree>(ArrayTree::class.java)

        // Statement Kinds
        object Print : Kind<PrintTree>(PrintTree::class.java)
        object ValDeclaration : Kind<ValDeclarationTree>(ValDeclarationTree::class.java)
        object FnDeclaration : Kind<FnDeclarationTree>(FnDeclarationTree::class.java)
        object TypeDeclaration : Kind<TypeDeclarationTree>(TypeDeclarationTree::class.java)
    }

    fun kind(): Kind<*>

    var type: KGType?
    var position: Position
}