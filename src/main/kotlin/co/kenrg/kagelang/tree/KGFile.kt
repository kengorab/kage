package co.kenrg.kagelang.tree

import co.kenrg.kagelang.model.Position
import co.kenrg.kagelang.tree.iface.base.Tree
import co.kenrg.kagelang.tree.types.KGTypeTag

class KGFile(var statements: List<KGTree>, var bindings: Map<String, KGTypeTag>) : KGTree() {
    override fun <D> accept(visitor: Visitor<D>, data: D) = visitor.visitTopLevel(this, data)

    override fun kind(): Tree.Kind<*> = Tree.Kind.TopLevel

    override var type: KGTypeTag = KGTypeTag.UNIT
    override var position: Position = Position.DEFAULT

    override fun withType(type: KGTypeTag): KGTree {
        throw UnsupportedOperationException("not implemented")
    }

    override fun withPosition(pos: Position): KGTree {
        throw UnsupportedOperationException("not implemented")
    }
}