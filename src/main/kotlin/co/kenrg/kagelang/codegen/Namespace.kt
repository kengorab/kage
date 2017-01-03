package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.tree.types.KGTypeTag
import co.kenrg.kagelang.typechecker.Signature
import java.util.*

data class StaticValBinding(val name: String, val type: KGTypeTag)

data class FunctionBinding(val name: String, val signature: Signature)

data class Namespace(
        val name: String,
        val staticVals: LinkedHashMap<String, StaticValBinding>,
        val functions: LinkedHashMap<String, FunctionBinding>
)