package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree

interface IndexTree : ExpressionTree {
    fun target(): ExpressionTree
    fun index(): ExpressionTree
}