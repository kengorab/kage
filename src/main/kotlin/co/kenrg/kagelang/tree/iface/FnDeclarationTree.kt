package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.StatementTree
import co.kenrg.kagelang.tree.iface.base.Tree
import co.kenrg.kagelang.typechecker.Signature

interface FnDeclarationTree : StatementTree {
    fun body(): Tree
    fun name(): String

    var signature: Signature
}
