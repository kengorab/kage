package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree

interface ArrayTree : ExpressionTree {
    fun items(): List<ExpressionTree>
}