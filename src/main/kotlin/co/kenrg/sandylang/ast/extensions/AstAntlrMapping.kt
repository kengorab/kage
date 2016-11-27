package co.kenrg.sandylang.ast.extensions

import co.kenrg.sandylang.SandyParser
import co.kenrg.sandylang.SandyParser.SandyFileContext
import co.kenrg.sandylang.ast.*

fun SandyFileContext.toAst(): SandyFile = SandyFile(this.line().map { it.statement().toAst() })

fun SandyParser.StatementContext.toAst(): Statement = when (this) {
    is SandyParser.VarDeclarationStatementContext -> {
        val assignment = this.varDeclaration().assignment()
        VarDeclarationStatement(assignment.ID().text, assignment.expression().toAst())
    }
    is SandyParser.AssignmentStatementContext ->
        AssignmentStatement(this.assignment().ID().text, this.assignment().expression().toAst())
    is SandyParser.PrintStatementContext ->
        PrintStatement(this.print().expression().toAst())
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun SandyParser.BinaryOperationContext.toAst(): Expression = when (this.operator.text) {
    "+" -> SumExpression(this.left.toAst(), this.right.toAst())
    "-" -> SubtractionExpression(this.left.toAst(), this.right.toAst())
    "*" -> MultiplicationExpression(this.left.toAst(), this.right.toAst())
    "/" -> DivisionExpression(this.left.toAst(), this.right.toAst())
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun SandyParser.TypeContext.toAst(): Type = when (this) {
    is SandyParser.IntegerContext -> IntType
    is SandyParser.DecimalContext -> DecimalType
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}

fun SandyParser.ExpressionContext.toAst(): Expression = when (this) {
    is SandyParser.BinaryOperationContext -> this.toAst()
    is SandyParser.ParenExpressionContext -> this.expression().toAst()
    is SandyParser.MinusExpressionContext -> UnaryMinusExpression(this.expression().toAst())
    is SandyParser.VarReferenceContext -> VarReferenceExpression(this.ID().text)
    is SandyParser.TypeConversionContext -> TypeConversionExpression(this.value.toAst(), this.targetType.toAst())
    is SandyParser.IntLiteralContext -> IntLiteralExpression(this.INTLIT().text)
    is SandyParser.DecLiteralContext -> IntLiteralExpression(this.DECLIT().text)
    else -> throw UnsupportedOperationException(this.javaClass.canonicalName)
}