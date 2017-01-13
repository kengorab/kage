package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.model.Signature
import co.kenrg.kagelang.tree.iface.base.StatementTree
import co.kenrg.kagelang.tree.iface.base.Tree
import co.kenrg.kagelang.tree.types.KGTypeTag

interface FnDeclarationTree : StatementTree {
    fun params(): List<Param>
    fun body(): Tree
    fun name(): String

    var signature: Signature

    data class Param(val name: String, val type: KGTypeTag)
}
