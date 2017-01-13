package co.kenrg.kagelang.model

import org.apache.commons.collections4.MultiValuedMap

interface Scope<out V, F> {
    val vals: Map<String, V>
    val functions: MultiValuedMap<String, F>
    val parent: Scope<V, F>?

    fun isRoot() = parent == null

    fun getVal(name: String): V? =
            if (vals.containsKey(name)) vals[name]
            else parent?.getVal(name)

    fun getFnsForName(name: String): List<F>? {
        val fns = functions[name].toList()
        return if (fns.isEmpty())
            parent?.getFnsForName(name)
        else
            fns
    }
}

open class Namespace<out T : Scope<*, *>>(val name: String, val rootScope: T)
