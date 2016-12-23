package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree

interface BindingReferenceTree : ExpressionTree {
    fun binding(): String
}