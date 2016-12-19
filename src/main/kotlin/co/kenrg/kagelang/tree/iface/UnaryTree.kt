package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree

interface UnaryTree : ExpressionTree {
    fun expression(): ExpressionTree
}