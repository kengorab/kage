package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree
import co.kenrg.kagelang.tree.iface.base.Tree

interface IfThenElseTree : ExpressionTree {
    fun condition(): ExpressionTree
    fun thenBody(): Tree
    fun elseBody(): Tree?
}