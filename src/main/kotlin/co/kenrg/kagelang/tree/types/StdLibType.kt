package co.kenrg.kagelang.tree.types

enum class StdLibType(val className: String) {
    Pair("kage/lang/tuple/Pair"),
    Triple("kage/lang/tuple/Triple"),
    Tuple4("kage/lang/tuple/Tuple4"),
    Tuple5("kage/lang/tuple/Tuple5"),
    Tuple6("kage/lang/tuple/Tuple6"),
    Maybe("kage/lang/util/Maybe"),
    Some("kage/lang/util/Maybe\$Some"),
    None("kage/lang/util/Maybe\$None");

    fun getTypeClass() = Class.forName(this.className.replace('/', '.'))!!

    companion object {
        fun forClassName(className: String): StdLibType? {
            val fixedClassName = className.replace('.', '/')
            return StdLibType.values().find { it.className == fixedClassName }
        }
    }
}
