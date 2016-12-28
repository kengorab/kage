package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree
import co.kenrg.kagelang.tree.iface.base.StatementTree

interface FnDeclarationTree : StatementTree {
    fun body(): ExpressionTree
    fun name(): String
}
