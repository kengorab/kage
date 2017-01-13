package co.kenrg.kagelang.tree

import co.kenrg.kagelang.KageParser
import co.kenrg.kagelang.model.Point
import co.kenrg.kagelang.model.Position
import co.kenrg.kagelang.tree.iface.FnDeclarationTree
import co.kenrg.kagelang.tree.types.KGTypeTag
import co.kenrg.kagelang.tree.types.asKGTypeTag
import org.antlr.v4.runtime.ParserRuleContext

class TreeMaker(val considerPosition: Boolean = true) {

    private fun statementOrExpressionToTree(statementOrExpression: KageParser.StatementOrExpressionContext): KGTree {
        return if (statementOrExpression.expression() != null && statementOrExpression.statement() != null)
            throw IllegalStateException("Line is both statement and expression")
        else if (statementOrExpression.expression() != null)
            toTree(statementOrExpression.expression())
        else if (statementOrExpression.statement() != null)
            toTree(statementOrExpression.statement())
        else
            throw IllegalStateException("Line is neither statement nor expression")
    }

    fun toKageFile(file: KageParser.KageFileContext): KGFile {
        val statements = file.line().map { statementOrExpressionToTree(it.statementOrExpression()) }
        return KGFile(statements, bindings = mapOf())
    }

    fun toTree(statement: KageParser.StatementContext): KGTree.KGStatement {
        val statementTree = when (statement) {
            is KageParser.PrintStatementContext ->
                KGTree.KGPrint(expr = toTree(statement.print().expression()))
            is KageParser.ValDeclarationStatementContext ->
                KGTree.KGValDeclaration(
                        identifier = statement.valDeclaration().Identifier().text,
                        expression = toTree(statement.valDeclaration().expression()),
                        typeAnnotation = statement.valDeclaration().typeAnnotation?.text?.asKGTypeTag()
                )
            is KageParser.FnDeclarationStatementContext ->
                KGTree.KGFnDeclaration(
                        name = statement.fnDeclaration().fnName.text,
                        body = statementOrExpressionToTree(statement.fnDeclaration().statementOrExpression()),
                        params = statement.fnDeclaration().params?.fnParam()?.map {
                            FnDeclarationTree.Param(it.Identifier().text, it.TypeAnnotation().text.asKGTypeTag())
                        } ?: listOf(),
                        retTypeAnnotation = statement.fnDeclaration().typeAnnotation?.text?.asKGTypeTag()
                )
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
                KGTree.KGLiteral(KGTypeTag.INT, expression.IntLiteral().text.toInt())
            is KageParser.DecLiteralContext ->
                KGTree.KGLiteral(KGTypeTag.DEC, expression.DecimalLiteral().text.toDouble())
            is KageParser.BoolLiteralContext ->
                KGTree.KGLiteral(KGTypeTag.BOOL, expression.BooleanLiteral().text.toBoolean())
            is KageParser.StringLiteralContext ->
                KGTree.KGLiteral(
                        KGTypeTag.STRING,
                        expression.StringLiteral().text
                                .trimStart('\"')
                                .trimEnd('\"')
                                .replace("\\b", "\b").replace("\\t", "\t")
                                .replace("\\r", "\r").replace("\\n", "\n")
                )
            is KageParser.ParenExpressionContext ->
                KGTree.KGParenthesized(expr = toTree(expression.expression()))
            is KageParser.BindingReferenceContext ->
                KGTree.KGBindingReference(expression.Identifier().text)
            is KageParser.InvocationContext ->
                KGTree.KGInvocation(toTree(expression.invokee))
            is KageParser.LetInExpressionContext ->
                KGTree.KGLetIn(
                        statements = expression.statements().statement().map { toTree(it) },
                        body = statementOrExpressionToTree(expression.statementOrExpression())
                )
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
