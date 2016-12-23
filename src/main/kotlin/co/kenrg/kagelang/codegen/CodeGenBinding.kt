package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.tree.types.KGTypeTag

// The size property is used for bindings whose type occupies more than 1 stack slot (i.e. Doubles/Longs).
// It's defaulted to 1, since that's the majority of the cases, but should be set to 2 when appropriate.
// TODO - Maybe instead of an Int, this field should be a Boolean, or an Enum value? It can can only be 1 or 2.
data class CodeGenBinding(val name: String, val type: KGTypeTag, val index: Int, val size: Int = 1)
