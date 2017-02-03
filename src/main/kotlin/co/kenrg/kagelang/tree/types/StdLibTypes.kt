package co.kenrg.kagelang.tree.types

enum class StdLibTypes(val className: String) {
    Pair("kage/lang/tuple/Pair"),
    Triple("kage/lang/tuple/Triple");

    fun getTypeClass() = Class.forName(this.className.replace('/', '.'))!!
}
