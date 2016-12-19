package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree

interface ParenthesizedTree : ExpressionTree {
    fun innerExpression(): ExpressionTree
}