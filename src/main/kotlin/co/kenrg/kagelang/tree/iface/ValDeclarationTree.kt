package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree
import co.kenrg.kagelang.tree.iface.base.StatementTree
import co.kenrg.kagelang.tree.types.KGTypeTag

interface ValDeclarationTree : StatementTree {
    fun expression(): ExpressionTree
    fun identifier(): String
    fun typeAnnotation(): KGTypeTag?
}
