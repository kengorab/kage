package co.kenrg.kagelang.tree.types

import co.kenrg.kagelang.tree.iface.LiteralTree
import co.kenrg.kagelang.tree.iface.base.Tree

enum class KGTypeTag(val typeName: String) {
    // Literals
    INT("Int"),
    DEC("Dec"),
    BOOL("Bool"),
    STRING("String"),

    UNIT("Unit"),
    UNSET("???");

    fun getLiteralKind(): Tree.Kind<LiteralTree> {
        return when (this) {
            KGTypeTag.INT -> Tree.Kind.IntLiteral
            KGTypeTag.DEC -> Tree.Kind.DecLiteral
            KGTypeTag.BOOL -> Tree.Kind.BoolLiteral
            KGTypeTag.STRING -> Tree.Kind.StringLiteral
            else -> throw UnsupportedOperationException("$this is not a literal, and has no literal kind")
        }
    }

    fun getSize() = when (this) {
        KGTypeTag.BOOL,
        KGTypeTag.STRING,
        KGTypeTag.INT -> 1
        KGTypeTag.DEC -> 2
        else -> throw IllegalStateException("Cannot calculate size of type $this")
    }

    companion object {
        val numericTypes = listOf(INT, DEC)

        fun fromString(str: String): KGTypeTag =
                when (str.trimStart(':', ' ').trimEnd(' ')) {
                    "Int" -> KGTypeTag.INT
                    "Dec" -> KGTypeTag.DEC
                    "Bool" -> KGTypeTag.BOOL
                    "String" -> KGTypeTag.STRING
                    else -> throw IllegalStateException("No acceptable type found for: $str")
                }
    }
}

// Convenience method for nullable-chaining
fun String.asKGTypeTag() = KGTypeTag.fromString(this)