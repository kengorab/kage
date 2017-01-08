package co.kenrg.kagelang.model

interface Scope<out V, out F> {
    val vals: V
    val functions: F
    val parent: Scope<V, F>?

    fun isRoot() = parent == null
}

open class Namespace<out T : Scope<*, *>>(val name: String, val rootScope: T)
