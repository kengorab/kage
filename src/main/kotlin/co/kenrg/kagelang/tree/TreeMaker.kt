package co.kenrg.kagelang.tree

import co.kenrg.kagelang.KageParser
import co.kenrg.kagelang.model.Point
import co.kenrg.kagelang.model.Position
import co.kenrg.kagelang.tree.types.KGTypeTag
import org.antlr.v4.runtime.ParserRuleContext

class TreeMaker(val considerPosition: Boolean = true) {

    fun toKageFile(file: KageParser.KageFileContext): KGFile {
        val statements = file.line().map {
            toTree(it.statement())
        }

        return KGFile(statements, bindings = mapOf())
    }

    fun toTree(statement: KageParser.StatementContext): KGTree.KGStatement {
        val statementTree = when (statement) {
            is KageParser.PrintStatementContext ->
                KGTree.KGPrint(expr = toTree(statement.print().expression()))
            else -> throw UnsupportedOperationException("toTree(Statement) not yet implemented for ${statement.javaClass.canonicalName}...")
        }

        return if (considerPosition)
            statementTree.withPosition(getPositionFromContext(statement))
        else
            statementTree
    }

    fun toTree(expression: KageParser.ExpressionContext): KGTree.KGExpression {
        val exprTree = when (expression) {
            is KageParser.UnaryOperationContext ->
                KGTree.KGUnary(
                        operator = expression.operator.text,
                        expr = toTree(expression.expression())
                )
            is KageParser.BinaryOperationContext ->
                KGTree.KGBinary(
                        left = toTree(expression.left),
                        operator = expression.operator.text,
                        right = toTree(expression.right)
                )
            is KageParser.IntLiteralContext ->
                KGTree.KGLiteral(KGTypeTag.INT, expression.INTLIT().text.toInt())
            is KageParser.DecLiteralContext ->
                KGTree.KGLiteral(KGTypeTag.DEC, expression.DECLIT().text.toDouble())
            is KageParser.BoolLiteralContext ->
                KGTree.KGLiteral(KGTypeTag.BOOL, expression.BOOLLIT().text.toBoolean())
            is KageParser.ParenExpressionContext ->
                KGTree.KGParenthesized(expr = toTree(expression.expression()))
            else -> throw UnsupportedOperationException("toTree(Expression) not yet implemented for ${expression.javaClass.canonicalName}...")
        }

        return if (considerPosition)
            exprTree.withPosition(getPositionFromContext(expression))
        else
            exprTree
    }

    private fun getPositionFromContext(context: ParserRuleContext): Position {
        val start = context.getStart()
        val startPt = Point(start.line, start.charPositionInLine)
        val end = context.getStop()
        val endPt = Point(end.line, end.charPositionInLine + end.text.length)
        return Position(startPt, endPt)
    }
}