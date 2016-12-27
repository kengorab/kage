package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree
import co.kenrg.kagelang.tree.iface.base.StatementTree

interface ValDeclarationTree : StatementTree {
    fun expression(): ExpressionTree
    fun identifier(): String
    fun typeAnnotation(): String?
}
