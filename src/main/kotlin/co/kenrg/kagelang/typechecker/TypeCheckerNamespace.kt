package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.typechecker.Signature
import java.util.*

// Since data classes must be universally unique, and there are similarly named data classes for codegen, use TC (for
// TypeChecker) as a namespace.
class TC {
    sealed class Binding(val name: String, val expression: KGTree) {
        class StaticValBinding(identifier: String, expression: KGTree) : Binding(identifier, expression)

        class FunctionBinding(
                name: String,
                expression: KGTree,
                val signature: Signature,
                val scope: Scope = Scope.empty()
        ) : Binding(name, expression)
    }

    data class Scope(
            val staticVals: HashMap<String, Binding.StaticValBinding>,
            val functions: HashMap<String, Binding.FunctionBinding>,
            val parent: Scope? = null
    ) {
        companion object {
            fun empty() = Scope(staticVals = HashMap(), functions = HashMap())
        }

        fun isRoot() = parent == null
    }

    data class Namespace(val name: String, val rootScope: Scope) {
        companion object {
            fun empty(name: String) =
                    Namespace(name = name, rootScope = Scope.empty())

        }
    }
}
