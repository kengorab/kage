package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.tree.types.KGTypeTag

data class Signature(val params: List<KGTypeTag> = listOf(), val returnType: KGTypeTag) {
    companion object {
        val DEFAULT = Signature(params = listOf(), returnType = KGTypeTag.UNIT)
    }
}
