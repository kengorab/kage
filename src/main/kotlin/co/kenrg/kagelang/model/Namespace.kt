package co.kenrg.kagelang.model

import co.kenrg.kagelang.tree.types.KGType
import org.apache.commons.collections4.MultiValuedMap

interface Scope<out V, F> {
    val vals: Map<String, V>
    val functions: MultiValuedMap<String, F>
    val types: Map<String, KGType>
    val parent: Scope<V, F>?

    fun isRoot() = parent == null

    fun getVal(name: String): V? =
            if (vals.containsKey(name)) vals[name]
            else parent?.getVal(name)

    fun getType(name: String): KGType? =
            // As of now, types can only be defined at the root scope.
            if (!isRoot()) parent!!.getType(name)
            else types[name]

    fun getFnsForName(name: String): List<F>? {
        val fns = functions[name].toList()
        return if (fns.isEmpty())
            parent?.getFnsForName(name)
        else
            fns
    }
}

open class Namespace<out T : Scope<*, *>>(val name: String, val rootScope: T)
