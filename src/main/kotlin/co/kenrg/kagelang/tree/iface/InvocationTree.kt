package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree

interface InvocationTree : ExpressionTree {
    fun invokee(): ExpressionTree
    fun params(): List<ExpressionTree>
}