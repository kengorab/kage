package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.model.Namespace
import co.kenrg.kagelang.model.Scope
import co.kenrg.kagelang.model.Signature
import co.kenrg.kagelang.tree.types.KGTypeTag
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import java.util.*

sealed class TCBinding(val name: String) {
    class StaticValBinding(identifier: String, val type: KGTypeTag) : TCBinding(identifier)

    class FunctionBinding(
            name: String,
            val signature: Signature
    ) : TCBinding(name)
}

class TCScope(
        override val vals: HashMap<String, TCBinding.StaticValBinding> = HashMap(),
        override val functions: ArrayListValuedHashMap<String, TCBinding.FunctionBinding> = ArrayListValuedHashMap(),
        override val parent: TCScope? = null
) : Scope<TCBinding.StaticValBinding, TCBinding.FunctionBinding>

class TCNamespace(name: String, rootScope: TCScope) : Namespace<TCScope>(name, rootScope) {
    companion object {
        fun empty(name: String) = TCNamespace(name, TCScope())
    }
}