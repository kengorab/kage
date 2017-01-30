package co.kenrg.kagelang.tree.types

enum class StdLibT(val className: String) {
    Pair("kage/lang/tuple/Pair");

    fun getTypeClass() = Class.forName(this.className.replace('/', '.'))!!
}
