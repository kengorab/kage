package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.model.TypedName
import co.kenrg.kagelang.tree.iface.base.StatementTree

interface TypeDeclarationTree : StatementTree {
    fun name(): String
    fun props(): List<TypedName>
}
