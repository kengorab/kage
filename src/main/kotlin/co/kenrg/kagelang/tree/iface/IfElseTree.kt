package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree
import co.kenrg.kagelang.tree.iface.base.Tree

interface IfElseTree : ExpressionTree {
    fun condition(): ExpressionTree
    fun trueBody(): Tree
    fun falseBody(): Tree?
}