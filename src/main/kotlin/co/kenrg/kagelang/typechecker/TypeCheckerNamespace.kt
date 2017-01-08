package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.model.Namespace
import co.kenrg.kagelang.model.Scope
import co.kenrg.kagelang.model.Signature
import co.kenrg.kagelang.tree.KGTree
import java.util.*

sealed class TCBinding(val name: String, val expression: KGTree) {
    class StaticValBinding(identifier: String, expression: KGTree) : TCBinding(identifier, expression)

    class FunctionBinding(
            name: String,
            expression: KGTree,
            val signature: Signature
    ) : TCBinding(name, expression)
}

class TCScope(
        override val vals: HashMap<String, TCBinding.StaticValBinding>,
        override val functions: HashMap<String, TCBinding.FunctionBinding>,
        override val parent: TCScope? = null
) : Scope<HashMap<String, TCBinding.StaticValBinding>, HashMap<String, TCBinding.FunctionBinding>> {
    companion object {
        fun empty() = TCScope(vals = HashMap(), functions = HashMap())
    }
}

class TCNamespace(name: String, rootScope: TCScope) : Namespace<TCScope>(name, rootScope) {
    companion object {
        fun empty(name: String) = TCNamespace(name, TCScope.empty())
    }
}