package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.iface.base.ExpressionTree

interface BlockTree : ExpressionTree {
    fun lines(): List<KGTree>
}