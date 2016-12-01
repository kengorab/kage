package co.kenrg.kagelang.ast.extensions

import co.kenrg.kagelang.KageParser
import co.kenrg.kagelang.KageParser.KageFileContext
import co.kenrg.kagelang.ast.*
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

fun KageFileContext.toAst(considerPosition: Boolean = false): KageFile =
        KageFile(this.line().map { it.statement().toAst(considerPosition) }, this.getPosition(considerPosition))

fun KageParser.StatementContext.toAst(considerPosition: Boolean = false): Statement {
    val position = this.getPosition(considerPosition)
    return when (this) {
        is KageParser.VarDeclarationStatementContext -> {
            val assignment = this.varDeclaration().assignment()
            VarDeclarationStatement(assignment.ID().text, assignment.expression().toAst(considerPosition), position)
        }
        is KageParser.AssignmentStatementContext ->
            AssignmentStatement(this.assignment().ID().text, this.assignment().expression().toAst(considerPosition), position)
        is KageParser.PrintStatementContext ->
            PrintStatement(this.print().expression().toAst(considerPosition), position)
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }
}

fun KageParser.BinaryOperationContext.toAst(considerPosition: Boolean = false): Expression {
    val position = this.getPosition(considerPosition)
    return when (this.operator.text) {
        "+" -> SumExpression(this.left.toAst(considerPosition), this.right.toAst(considerPosition), position)
        "-" -> SubtractionExpression(this.left.toAst(considerPosition), this.right.toAst(considerPosition), position)
        "*" -> MultiplicationExpression(this.left.toAst(considerPosition), this.right.toAst(considerPosition), position)
        "/" -> DivisionExpression(this.left.toAst(considerPosition), this.right.toAst(considerPosition), position)
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }
}

fun KageParser.ExpressionContext.toAst(considerPosition: Boolean = false): Expression {
    val position = this.getPosition(considerPosition)
    return when (this) {
        is KageParser.BinaryOperationContext -> this.toAst(considerPosition)
        is KageParser.ParenExpressionContext -> this.expression().toAst(considerPosition)
        is KageParser.MinusExpressionContext -> UnaryMinusExpression(this.expression().toAst(considerPosition), position)
        is KageParser.VarReferenceContext -> VarReferenceExpression(this.ID().text, position)
        is KageParser.IntLiteralContext -> IntLiteralExpression(this.INTLIT().text, position)
        is KageParser.DecLiteralContext -> DecimalLiteralExpression(this.DECLIT().text, position)
        is KageParser.BoolLiteralContext -> BoolLiteralExpression(this.BOOLLIT().text, position)
        else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
    }
}