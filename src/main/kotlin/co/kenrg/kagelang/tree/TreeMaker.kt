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
            is KageParser.ValDeclarationStatementContext ->
                KGTree.KGValDeclaration(
                        identifier = statement.valDeclaration().Identifier().text,
                        expression = toTree(statement.valDeclaration().expression()),
                        typeAnnotation =
                        if (statement.valDeclaration().typeAnnotation?.text != null)
                            KGTypeTag.fromString(statement.valDeclaration().typeAnnotation.text)
                        else
                            null
                )
            is KageParser.FnDeclarationStatementContext ->
                KGTree.KGFnDeclaration(
                        name = statement.fnDeclaration().fnName.text,
                        expression = toTree(statement.fnDeclaration().body)
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
            is KageParser.BlockExpressionContext -> {
//                fun blockLineToTree(line: KageParser.StatementOrExpressionContext): Tree {
//                }
//
//                fun blockLinesToTreeList(lines: KageParser.StatementsOrExpressionsContext): List<Tree> {
//                    if (lines.statementOrExpression() != null && lines.)
//                }
//                KGTree.KGBlock(getBlockContents(expression.block().lines))
                val contents = expression.block().lines?.statementOrExpression()?.map { line ->
                    if (line.expression() != null && line.statement() != null)
                        throw IllegalStateException("Line is both a statement and an expression")
                    else if (line.expression() != null)
                        toTree(line.expression())
                    else if (line.statement() != null)
                        toTree(line.statement())
                    else
                        throw IllegalStateException("Line is neither statement nor expression")
                }
                KGTree.KGBlock(contents ?: listOf())
            }


//                KGTree.KGBlock(expression.block().lines.jj .statementOrExpressionLine().map {
//                    val statementOrExpr = it.statementOrExpression()
//                    if (statementOrExpr.expression() != null && statementOrExpr.statement() != null)
//                        throw UnsupportedOperationException("Block contains line which is both expression and statement")
//
//                    if (statementOrExpr.expression() != null)
//                        toTree(statementOrExpr.expression())
//                    else if (statementOrExpr.statement() != null)
//                        toTree(statementOrExpr.statement())
//                    else
//                        throw UnsupportedOperationException("Block contains line which is neither expression nor statement")
//                })
            else -> throw UnsupportedOperationException("toTree(Expression) not yet implemented for ${expression.javaClass.canonicalName}...")
        }

        return if (considerPosition)
            exprTree.withPosition(getPositionFromContext(expression))
        else
            exprTree
    }

//    private fun getBlockContents(blockContents: KageParser.StatementsOrExpressionsContext, items: List<Tree> = listOf()): List<Tree> {
//        if (blockContents.statementOrExpression() != null
//                && blockContents.statementsOrExpressions() != null)
//            throw IllegalStateException("Line is both single statement/expr AND recursively-multiple statements/exprs")
//
//        return if (blockContents.statementOrExpression() != null) {
//            if (blockContents.statementOrExpression().expression() != null
//                    && blockContents.statementOrExpression().statement() != null)
//                throw IllegalStateException("Line is both statement and expression")
//
//            if (blockContents.statementOrExpression().expression() != null)
//                listOf(toTree(blockContents.statementOrExpression().expression()))
//            else if (blockContents.statementOrExpression().statement() != null)
//                listOf(toTree(blockContents.statementOrExpression().statement()))
//            else
//                throw IllegalStateException("Line is neither statement nor expression")
//        } else if (blockContents.statementsOrExpressions() != null) {
//            items + getBlockContents(blockContents.statementsOrExpressions())
//        } else {
//            throw IllegalStateException("Line is neither single statement/expr nor recursively-multiple statements/exprs")
//        }
//
//    }

    private fun getPositionFromContext(context: ParserRuleContext): Position {
        val start = context.getStart()
        val startPt = Point(start.line, start.charPositionInLine)
        val end = context.getStop()
        val endPt = Point(end.line, end.charPositionInLine + end.text.length)
        return Position(startPt, endPt)
    }
}
