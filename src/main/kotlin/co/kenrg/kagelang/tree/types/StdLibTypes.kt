package co.kenrg.kagelang.tree.types

enum class StdLibTypes(val className: String) {
    Pair("kage/lang/tuple/Pair");

    fun getTypeClass() = Class.forName(this.className.replace('/', '.'))!!
}
