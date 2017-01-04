package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.types.KGTypeTag

//data class Binding(val identifier: String, val expression: KGTree.KGExpression)

data class Signature(val params: List<KGTypeTag> = listOf(), val returnType: KGTypeTag) {
    companion object {
        val DEFAULT = Signature(params = listOf(), returnType = KGTypeTag.UNIT)
    }
}

sealed class Binding(val identifier: String, val expression: KGTree) {
    class ValBinding(identifier: String, expression: KGTree.KGExpression) : Binding(identifier, expression)
    class FnBinding(name: String, expression: KGTree, val signature: Signature) : Binding(name, expression)
}
