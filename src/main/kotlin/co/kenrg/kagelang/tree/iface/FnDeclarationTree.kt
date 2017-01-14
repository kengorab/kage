package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.model.FnParameter
import co.kenrg.kagelang.model.Signature
import co.kenrg.kagelang.tree.iface.base.StatementTree
import co.kenrg.kagelang.tree.iface.base.Tree

interface FnDeclarationTree : StatementTree {
    fun params(): List<FnParameter>
    fun body(): Tree
    fun name(): String

    var signature: Signature
}
