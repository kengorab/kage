package co.kenrg.kagelang.model

import co.kenrg.kagelang.tree.types.KGType

data class Signature(val params: List<Pair<String, KGType>> = listOf(), val returnType: KGType) {
    companion object {
        val DEFAULT = Signature(params = listOf(), returnType = KGType.UNIT)
    }

    fun jvmTypeDescriptor(): String {
        return "(${params.map { it.second.jvmDescriptor }.joinToString("")})${returnType.jvmDescriptor}"
    }
}
