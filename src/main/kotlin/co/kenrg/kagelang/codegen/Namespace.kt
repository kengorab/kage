package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.model.Namespace
import co.kenrg.kagelang.model.Scope
import co.kenrg.kagelang.model.Signature
import co.kenrg.kagelang.tree.types.KGTypeTag
import jdk.internal.org.objectweb.asm.Label
import jdk.internal.org.objectweb.asm.MethodVisitor
import java.util.*

data class StaticValBinding(val name: String, val type: KGTypeTag)
data class FunctionBinding(val name: String, val signature: Signature)

data class FocusedMethod(val writer: MethodVisitor, val start: Label?, val end: Label?)
class CGScope(
        override val vals: LinkedHashMap<String, StaticValBinding> = LinkedHashMap(),
        override val functions: LinkedHashMap<String, FunctionBinding> = LinkedHashMap(),
        override val parent: CGScope? = null,
        var method: FocusedMethod? = null
) : Scope<StaticValBinding, FunctionBinding>

class CGNamespace(name: String, rootScope: CGScope) : Namespace<CGScope>(name, rootScope)
