package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree

interface LiteralTree : ExpressionTree {
    fun value(): Any
}