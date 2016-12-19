package co.kenrg.kagelang.tree.iface.base

import co.kenrg.kagelang.tree.iface.BinaryTree
import co.kenrg.kagelang.tree.iface.LiteralTree

interface TreeVisitor<out R, in D> {
    fun visitLiteral(tree: LiteralTree, data: D): R
    fun visitBinary(tree: BinaryTree, data: D): R
}