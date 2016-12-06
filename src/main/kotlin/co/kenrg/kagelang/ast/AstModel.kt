package co.kenrg.kagelang.ast

data class Point(val line: Int, val column: Int)
data class Position(val start: Point, val end: Point)

fun position(startLine: Int, startCol: Int, endLine: Int, endCol: Int) =
        Position(Point(startLine, startCol), Point(endLine, endCol))

interface Node {
    val position: Position?
}

data class KageFile(val statements: List<Statement>, override val position: Position? = null) : Node

interface Statement : Node
interface Expression : Node

// Expressions

interface BinaryExpression : Expression {
    val left: Expression
    val right: Expression
}

data class SumExpression(override val left: Expression, override val right: Expression, override val position: Position? = null) : BinaryExpression
data class SubtractionExpression(override val left: Expression, override val right: Expression, override val position: Position? = null) : BinaryExpression
data class MultiplicationExpression(override val left: Expression, override val right: Expression, override val position: Position? = null) : BinaryExpression
data class DivisionExpression(override val left: Expression, override val right: Expression, override val position: Position? = null) : BinaryExpression

data class BooleanOrExpression(override val left: Expression, override val right: Expression, override val position: Position? = null) : BinaryExpression
data class BooleanAndExpression(override val left: Expression, override val right: Expression, override val position: Position? = null) : BinaryExpression

data class UnaryMinusExpression(val value: Expression, override val position: Position? = null) : Expression
data class VarReferenceExpression(val varName: String, override val position: Position? = null) : Expression

// Expressions: Literals
data class IntLiteralExpression(val value: String, override val position: Position? = null) : Expression

data class DecimalLiteralExpression(val value: String, override val position: Position? = null) : Expression
data class BoolLiteralExpression(val value: String, override val position: Position? = null) : Expression

// Statements

data class VarDeclarationStatement(val varName: String, val value: Expression, override val position: Position? = null) : Statement
data class AssignmentStatement(val varName: String, val value: Expression, override val position: Position? = null) : Statement
data class PrintStatement(val value: Expression, override val position: Position? = null) : Statement
