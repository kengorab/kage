package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree

interface IdentifierTree : ExpressionTree {
    fun name(): String
}