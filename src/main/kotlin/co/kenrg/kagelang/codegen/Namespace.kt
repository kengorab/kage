package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.model.Namespace
import co.kenrg.kagelang.model.Scope
import co.kenrg.kagelang.model.Signature
import co.kenrg.kagelang.tree.types.KGType
import jdk.internal.org.objectweb.asm.Label
import jdk.internal.org.objectweb.asm.MethodVisitor
import org.apache.commons.collections4.map.LinkedMap
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import java.util.*

data class FunctionBinding(val name: String, val signature: Signature)
sealed class ValBinding(val name: String, val type: KGType) {
    class Local(name: String, type: KGType, val size: Int = 1, val index: Int) : ValBinding(name, type)
    class Static(name: String, type: KGType) : ValBinding(name, type)
}

class CGType(val name: String)

data class FocusedMethod(val writer: MethodVisitor, val start: Label?, val end: Label?)
class CGScope(
        override val vals: LinkedMap<String, ValBinding> = LinkedMap(),
        override val functions: ArrayListValuedHashMap<String, FunctionBinding> = ArrayListValuedHashMap(),
        override val parent: CGScope? = null,
        override val types: HashMap<String, CGType> = HashMap(),
        var method: FocusedMethod? = null
) : Scope<ValBinding, FunctionBinding, CGType> {
    fun createChildScope(vals: LinkedMap<String, ValBinding> = LinkedMap(), method: FocusedMethod? = null) =
            CGScope(vals = vals, functions = ArrayListValuedHashMap(), parent = this, method = method)
}

class CGNamespace(name: String, rootScope: CGScope) : Namespace<CGScope>(name, rootScope)
