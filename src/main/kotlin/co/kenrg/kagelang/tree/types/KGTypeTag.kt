package co.kenrg.kagelang.tree.types

import co.kenrg.kagelang.tree.iface.LiteralTree
import co.kenrg.kagelang.tree.iface.base.Tree

enum class KGTypeTag {
    // Literals
    INT,
    DEC,
    BOOL,
    STRING,

    UNIT,
    UNSET;

    fun getLiteralKind(): Tree.Kind<LiteralTree> {
        return when (this) {
            KGTypeTag.INT -> Tree.Kind.IntLiteral
            KGTypeTag.DEC -> Tree.Kind.DecLiteral
            KGTypeTag.BOOL -> Tree.Kind.BoolLiteral
            KGTypeTag.STRING -> Tree.Kind.StringLiteral
            else -> throw UnsupportedOperationException("$this is not a literal, and has no literal kind")
        }
    }

    companion object {
        val numericTypes = listOf(INT, DEC)
    }
}