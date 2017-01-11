package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.model.Namespace
import co.kenrg.kagelang.model.Scope
import co.kenrg.kagelang.model.Signature
import co.kenrg.kagelang.tree.types.KGTypeTag
import jdk.internal.org.objectweb.asm.Label
import jdk.internal.org.objectweb.asm.MethodVisitor
import org.apache.commons.collections4.map.LinkedMap

data class FunctionBinding(val name: String, val signature: Signature)
sealed class ValBinding(val name: String, val type: KGTypeTag) {
    class Local(name: String, type: KGTypeTag, val size: Int = 1, val index: Int) : ValBinding(name, type)
    class Static(name: String, type: KGTypeTag) : ValBinding(name, type)
}

data class FocusedMethod(val writer: MethodVisitor, val start: Label?, val end: Label?)
class CGScope(
        override val vals: LinkedMap<String, ValBinding> = LinkedMap(),
        override val functions: LinkedMap<String, FunctionBinding> = LinkedMap(),
        override val parent: CGScope? = null,
        var method: FocusedMethod? = null
) : Scope<ValBinding, FunctionBinding>

class CGNamespace(name: String, rootScope: CGScope) : Namespace<CGScope>(name, rootScope)
