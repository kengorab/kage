package co.kenrg.kagelang.tree.types

enum class StdLibTypes(val className: String) {
    Pair("kage/lang/tuple/Pair"),
    Triple("kage/lang/tuple/Triple"),
    Tuple4("kage/lang/tuple/Tuple4"),
    Tuple5("kage/lang/tuple/Tuple5"),
    Tuple6("kage/lang/tuple/Tuple6"),

    Array("kage/lang/collection/Array");

    fun getTypeClass() = Class.forName(this.className.replace('/', '.'))!!
}
