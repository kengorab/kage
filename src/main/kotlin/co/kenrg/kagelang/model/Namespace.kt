package co.kenrg.kagelang.model

interface Scope<out V, out F> {
    val vals: Map<String, V>
    val functions: Map<String, F>
    val parent: Scope<V, F>?

    fun isRoot() = parent == null

    fun getVal(name: String): V? =
            if (vals.containsKey(name)) vals[name]
            else parent?.getVal(name)

    fun getFn(name: String): F? =
            if (functions.containsKey(name)) functions[name]
            else parent?.getFn(name)
}

open class Namespace<out T : Scope<*, *>>(val name: String, val rootScope: T)
