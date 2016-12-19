package co.kenrg.kagelang.tree.iface

import co.kenrg.kagelang.tree.iface.base.ExpressionTree

interface BinaryTree : ExpressionTree {
    fun getLeftExpression(): ExpressionTree
    fun getRightExpression(): ExpressionTree
}