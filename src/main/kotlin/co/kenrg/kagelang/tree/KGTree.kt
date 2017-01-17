package co.kenrg.kagelang.tree

import co.kenrg.kagelang.model.FnParameter
import co.kenrg.kagelang.model.Position
import co.kenrg.kagelang.model.Signature
import co.kenrg.kagelang.tree.iface.*
import co.kenrg.kagelang.tree.iface.base.ExpressionTree
import co.kenrg.kagelang.tree.iface.base.StatementTree
import co.kenrg.kagelang.tree.iface.base.Tree
import co.kenrg.kagelang.tree.types.KGTypeTag
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder

abstract class KGTree : Tree {
    interface Visitor<in D> {
        // Top-level visitor
        fun visitTopLevel(file: KGFile, data: D)

        // Expression visitors
        fun visitLiteral(literal: KGLiteral, data: D)

        fun visitUnary(unary: KGUnary, data: D)
        fun visitBinary(binary: KGBinary, data: D)
        fun visitParenthesized(parenthesized: KGParenthesized, data: D)
        fun visitBindingReference(bindingReference: KGBindingReference, data: D)
        fun visitInvocation(invocation: KGInvocation, data: D)
        fun visitLetIn(letIn: KGLetIn, data: D)

        // Statement visitors
        fun visitPrint(print: KGPrint, data: D)

        fun visitValDeclaration(valDecl: KGValDeclaration, data: D)
        fun visitFnDeclaration(fnDecl: KGFnDeclaration, data: D)
    }

    interface VisitorErrorHandler<in E> {
        fun handleError(error: E)
    }

    abstract fun <D> accept(visitor: Visitor<D>, data: D)
    abstract fun withType(type: KGTypeTag): KGTree
    abstract fun withPosition(pos: Position): KGTree

    override fun equals(other: Any?) = EqualsBuilder.reflectionEquals(this, other)
    override fun hashCode() = HashCodeBuilder.reflectionHashCode(this)

    /*
        Expressions
     */
    abstract class KGExpression : KGTree(), ExpressionTree {
        override var type: KGTypeTag = KGTypeTag.UNSET
        override var position: Position = Position.DEFAULT

        override fun withType(type: KGTypeTag): KGExpression {
            this.type = type
            return this
        }

        override fun withPosition(pos: Position): KGExpression {
            this.position = pos
            return this
        }
    }

    class KGLiteral(val typeTag: KGTypeTag, val value: Any) : KGExpression(), LiteralTree {
        override fun value() = value

        override fun kind(): Tree.Kind<LiteralTree> = typeTag.getLiteralKind()

        override fun <D> accept(visitor: Visitor<D>, data: D) = visitor.visitLiteral(this, data)
    }

    class KGUnary(val operator: String /*TODO - Not String*/, val expr: KGExpression) : KGExpression(), UnaryTree {
        override fun expression() = expr

        override fun kind(): Tree.Kind<UnaryTree> = when (operator) {
            "-" -> Tree.Kind.ArithmeticNegation
            "!" -> Tree.Kind.BooleanNegation
            else -> throw UnsupportedOperationException("Operator $operator not yet implemented")
        }

        override fun <D> accept(visitor: Visitor<D>, data: D) = visitor.visitUnary(this, data)
    }

    class KGBinary(val left: KGExpression, val operator: String /*TODO - Not String*/, val right: KGExpression) : KGExpression(), BinaryTree {
        override fun getLeftExpression() = left
        override fun getRightExpression() = right

        override fun kind(): Tree.Kind<BinaryTree> = when (operator) {
            "+" -> Tree.Kind.Plus
            "-" -> Tree.Kind.Minus
            "*" -> Tree.Kind.Multiply
            "/" -> Tree.Kind.Divide
            "&&" -> Tree.Kind.ConditionalAnd
            "||" -> Tree.Kind.ConditionalOr
            "++" -> Tree.Kind.Concatenation
            ">" -> Tree.Kind.GreaterThan
            "<" -> Tree.Kind.LessThan
            else -> throw UnsupportedOperationException("Operator $operator not yet implemented")
        }

        override fun <D> accept(visitor: Visitor<D>, data: D) = visitor.visitBinary(this, data)
    }

    class KGParenthesized(val expr: KGExpression) : KGExpression(), ParenthesizedTree {
        override fun innerExpression() = expr

        override fun kind() = expr.kind()

        override fun <D> accept(visitor: Visitor<D>, data: D) = visitor.visitParenthesized(this, data)
    }

    class KGBindingReference(val binding: String) : KGExpression(), BindingReferenceTree {
        override fun binding() = binding

        override fun kind() = Tree.Kind.BindingReference

        override fun <D> accept(visitor: Visitor<D>, data: D) = visitor.visitBindingReference(this, data)
    }

    class KGInvocation(val invokee: KGExpression, val params: List<KGExpression> = listOf()) : KGExpression(), InvocationTree {
        override fun invokee() = invokee
        override fun params() = params

        override fun kind() = Tree.Kind.Invocation

        override fun <D> accept(visitor: Visitor<D>, data: D) = visitor.visitInvocation(this, data)
    }

    class KGLetIn(val statements: List<KGStatement>, val body: KGTree) : KGExpression(), LetInTree {
        override fun statements() = statements
        override fun body() = body

        override fun kind() = Tree.Kind.LetIn

        override fun <D> accept(visitor: Visitor<D>, data: D) = visitor.visitLetIn(this, data)
    }

    /*
        Statements
     */
    abstract class KGStatement : KGTree(), StatementTree {
        override var type: KGTypeTag = KGTypeTag.UNIT
        override var position: Position = Position.DEFAULT

        override fun withType(type: KGTypeTag): KGStatement {
            // Cannot set type on statement; statement type is always UNIT
            return this
        }

        override fun withPosition(pos: Position): KGStatement {
            this.position = pos
            return this
        }
    }

    class KGPrint(val expr: KGExpression) : KGStatement(), PrintTree {
        override fun expression() = expr

        override fun kind() = Tree.Kind.Print

        override fun <D> accept(visitor: Visitor<D>, data: D) = visitor.visitPrint(this, data)
    }

    class KGValDeclaration(val identifier: String, val expression: KGExpression, val typeAnnotation: KGTypeTag? = null) : KGStatement(), ValDeclarationTree {
        override fun expression(): ExpressionTree = expression
        override fun identifier() = identifier
        override fun typeAnnotation() = typeAnnotation

        override fun kind() = Tree.Kind.ValDeclaration

        override fun <D> accept(visitor: Visitor<D>, data: D) = visitor.visitValDeclaration(this, data)
    }

    class KGFnDeclaration(
            val name: String,
            val body: KGTree,
            val params: List<FnParameter> = listOf(),
            val retTypeAnnotation: KGTypeTag? = null
    ) : KGStatement(), FnDeclarationTree {
        override fun params() = params
        override fun body() = body
        override fun name() = name

        // This will be set during Typechecking/Attribution
        override var signature = Signature.DEFAULT

        override fun kind() = Tree.Kind.FnDeclaration

        override fun <D> accept(visitor: Visitor<D>, data: D) = visitor.visitFnDeclaration(this, data)
    }
}