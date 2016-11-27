package co.kenrg.sandylang.ast.extensions

import co.kenrg.sandylang.SandyParser
import co.kenrg.sandylang.SandyParser.SandyFileContext
import co.kenrg.sandylang.ast.*
import org.antlr.v4.runtime.ParserRuleContext

fun ParserRuleContext.getPosition(calculatePos: Boolean): Position? {
    return if (calculatePos) {
        val start = this.getStart()
        val startPt = Point(start.line, start.charPositionInLine)
        val end = this.getStop()
        val endPt = Point(end.line, end.charPositionInLine + end.text.length)
        Position(startPt, endPt)
    } else {
        null
    }
}

fun SandyFileContext.toAst(considerPosition: Boolean = false): SandyFile =
        SandyFile(this.line().map { it.statement().toAst(considerPosition) }, this.getPosition(considerPosition))

fun SandyParser.StatementContext.toAst(considerPosition: Boolean = false): Statement {
    val position = this.getPosition(considerPosition)
    return when (this) {
        is SandyParser.VarDeclarationStatementContext -> {
            val assignment = this.varDeclaration().assignment()
            VarDeclarationStatement(assignment.ID().text, assignment.expression().toAst(considerPosition), position)
        }
        is SandyParser.AssignmentStatementContext ->
            AssignmentStatement(this.assignment().ID().text, this.assignment().expression().toAst(considerPosition), position)
        is SandyParser.PrintStatementContext ->
            PrintStatement(this.print().expression().toAst(considerPosition), position)
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }
}

fun SandyParser.BinaryOperationContext.toAst(considerPosition: Boolean = false): Expression {
    val position = this.getPosition(considerPosition)
    return when (this.operator.text) {
        "+" -> SumExpression(this.left.toAst(considerPosition), this.right.toAst(considerPosition), position)
        "-" -> SubtractionExpression(this.left.toAst(considerPosition), this.right.toAst(considerPosition), position)
        "*" -> MultiplicationExpression(this.left.toAst(considerPosition), this.right.toAst(considerPosition), position)
        "/" -> DivisionExpression(this.left.toAst(considerPosition), this.right.toAst(considerPosition), position)
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }
}

fun SandyParser.TypeContext.toAst(considerPosition: Boolean = false): Type {
    val position = this.getPosition(considerPosition)
    return when (this) {
        is SandyParser.IntegerContext -> IntType(position)
        is SandyParser.DecimalContext -> DecimalType(position)
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }
}

fun SandyParser.ExpressionContext.toAst(considerPosition: Boolean = false): Expression {
    val position = this.getPosition(considerPosition)
    return when (this) {
        is SandyParser.BinaryOperationContext -> this.toAst(considerPosition)
        is SandyParser.ParenExpressionContext -> this.expression().toAst(considerPosition)
        is SandyParser.MinusExpressionContext -> UnaryMinusExpression(this.expression().toAst(considerPosition), position)
        is SandyParser.VarReferenceContext -> VarReferenceExpression(this.ID().text, position)
        is SandyParser.TypeConversionContext -> TypeConversionExpression(this.value.toAst(considerPosition), this.targetType.toAst(considerPosition), position)
        is SandyParser.IntLiteralContext -> IntLiteralExpression(this.INTLIT().text, position)
        is SandyParser.DecLiteralContext -> DecimalLiteralExpression(this.DECLIT().text, position)
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }
}