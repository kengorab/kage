package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree

interface DotTree : ExpressionTree {
    fun target(): ExpressionTree
    fun prop(): String
}