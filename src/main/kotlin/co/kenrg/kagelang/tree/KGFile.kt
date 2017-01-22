package co.kenrg.kagelang.tree

import co.kenrg.kagelang.model.Position
import co.kenrg.kagelang.tree.iface.base.Tree
import co.kenrg.kagelang.tree.types.KGType

class KGFile(var statements: List<KGTree>, var bindings: Map<String, KGType>) : KGTree() {
    override fun <D> accept(visitor: Visitor<D>, data: D) = visitor.visitTopLevel(this, data)

    override fun kind(): Tree.Kind<*> = Tree.Kind.TopLevel

    override var type: KGType? = KGType.UNIT
    override var position: Position = Position.DEFAULT

    override fun withPosition(pos: Position): KGTree {
        throw UnsupportedOperationException("not implemented")
    }
}