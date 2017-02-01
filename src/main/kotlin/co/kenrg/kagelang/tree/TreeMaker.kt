package co.kenrg.kagelang.tree

import co.kenrg.kagelang.KageParser
import co.kenrg.kagelang.model.*
import co.kenrg.kagelang.tree.types.KGType
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

    private fun nullableStatementOrExpressionToTree(statementOrExpression: KageParser.StatementOrExpressionContext?) =
            if (statementOrExpression != null) statementOrExpressionToTree(statementOrExpression)
            else null

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
                        typeAnnotation = statement.valDeclaration().typeAnnotation?.type?.toTypeIdentifier()
                )
            is KageParser.FnDeclarationStatementContext ->
                KGTree.KGFnDeclaration(
                        name = statement.fnDeclaration().fnName.text,
                        body = statementOrExpressionToTree(statement.fnDeclaration().statementOrExpression()),
                        params = statement.fnDeclaration().params?.fnParam()?.map {
                            FnParameter(it.Identifier().text, it.typeAnnot().type.toTypeIdentifier())
                        } ?: listOf(),
                        retTypeAnnotation = statement.fnDeclaration().typeAnnotation?.type?.toTypeIdentifier()
                )
            is KageParser.TypeDeclarationStatementContext ->
                KGTree.KGTypeDeclaration(
                        name = statement.name.text,
                        props = statement.props?.props?.typeProp()
                                ?.map {
                                    TypedName(it.prop.text, it.typeAnnotation.type.toTypeIdentifier())
                                }
                                ?: listOf()
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
                KGTree.KGLiteral(KGType.INT, expression.IntLiteral().text.toInt())
            is KageParser.DecLiteralContext ->
                KGTree.KGLiteral(KGType.DEC, expression.DecimalLiteral().text.toDouble())
            is KageParser.BoolLiteralContext ->
                KGTree.KGLiteral(KGType.BOOL, expression.BooleanLiteral().text.toBoolean())
            is KageParser.StringLiteralContext ->
                KGTree.KGLiteral(
                        KGType.STRING,
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
                KGTree.KGInvocation(
                        invokee = toTree(expression.invokee),
                        params = expression.params?.expression()?.map { toTree(it) } ?: listOf()
                )
            is KageParser.LetInExpressionContext ->
                KGTree.KGLetIn(
                        statements = expression.statements().statement().map { toTree(it) },
                        body = statementOrExpressionToTree(expression.statementOrExpression())
                )
            is KageParser.IfThenElseExpressionContext ->
                KGTree.KGIfThenElse(
                        condition = toTree(expression.cond),
                        thenBody = statementOrExpressionToTree(expression.thenBody),
                        elseBody = nullableStatementOrExpressionToTree(expression.elseBody)
                )
            is KageParser.DotExpressionContext ->
                KGTree.KGDot(
                        target = toTree(expression.target),
                        prop = expression.prop.text
                )
            is KageParser.TupleLiteralContext ->
                KGTree.KGTuple(
                        items = expression.items.expression().map { toTree(it) }
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
