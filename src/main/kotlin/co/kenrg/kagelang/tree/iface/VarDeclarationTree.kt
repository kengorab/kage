package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree
import co.kenrg.kagelang.tree.iface.base.StatementTree

interface VarDeclarationTree : StatementTree {
    fun expression(): ExpressionTree
    fun variable(): ExpressionTree

//    override fun <R, D> accept(visitor: TreeVisitor<R, D>, data: D): R = visitor.visitVarDecl(this, data)
}