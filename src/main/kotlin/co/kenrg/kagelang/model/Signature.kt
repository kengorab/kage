package co.kenrg.kagelang.model

import co.kenrg.kagelang.tree.types.KGTypeTag

data class Signature(val params: List<FnParameter> = listOf(), val returnType: KGTypeTag) {
    companion object {
        val DEFAULT = Signature(params = listOf(), returnType = KGTypeTag.UNIT)
    }
}
