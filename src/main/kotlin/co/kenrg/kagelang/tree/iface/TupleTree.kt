package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree

interface TupleTree : ExpressionTree {
    fun items(): List<ExpressionTree>
}