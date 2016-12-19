package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree
import co.kenrg.kagelang.tree.iface.base.StatementTree

interface VarAssignmentTree : StatementTree {
    fun getVariable(): ExpressionTree
    fun getExpression(): ExpressionTree
}