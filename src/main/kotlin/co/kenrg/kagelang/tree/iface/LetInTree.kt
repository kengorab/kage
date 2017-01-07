package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree
import co.kenrg.kagelang.tree.iface.base.StatementTree
import co.kenrg.kagelang.tree.iface.base.Tree

interface LetInTree : ExpressionTree {
    fun statements(): List<StatementTree>
    fun body(): Tree
}