package co.kenrg.sandylang.ast

interface Node

data class SandyFile(val statements: List<Statement>) : Node

interface Statement : Node
interface Expression : Node
interface Type : Node

// Types

object IntType : Type
object DecimalType : Type

// Expressions

interface BinaryExpression : Expression {
    val left: Expression
    val right: Expression
}

data class SumExpression(override val left: Expression, override val right: Expression) : BinaryExpression
data class SubtractionExpression(override val left: Expression, override val right: Expression) : BinaryExpression
data class MultiplicationExpression(override val left: Expression, override val right: Expression) : BinaryExpression
data class DivisionExpression(override val left: Expression, override val right: Expression) : BinaryExpression
data class UnaryMinusExpression(val value: Expression) : Expression
data class VarReferenceExpression(val varName: String) : Expression
data class TypeConversionExpression(val value: Expression, val targetType: Type) : Expression
data class IntLiteralExpression(val value: String) : Expression
data class DecimalLiteral(val value: String) : Expression

// Statements

data class VarDeclarationStatement(val varName: String, val value: Expression) : Statement
data class AssignmentStatement(val varName: String, val value: Expression) : Statement
data class PrintStatement(val value: Expression) : Statement
