package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree

interface VarReferenceTree : ExpressionTree {
    fun identifier(): ExpressionTree
}