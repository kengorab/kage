package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.types.KGTypeTag

//data class Binding(val identifier: String, val expression: KGTree.KGExpression)

data class Signature(val params: List<KGTypeTag> = listOf(), val returnType: KGTypeTag)

sealed class Binding(val identifier: String, val expression: KGTree.KGExpression) {
    class ValBinding(identifier: String, expression: KGTree.KGExpression) : Binding(identifier, expression)
    class FnBinding(name: String, expression: KGTree.KGExpression, val signature: Signature) : Binding(name, expression)
}
