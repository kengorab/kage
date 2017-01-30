package co.kenrg.kagelang.tree.types

import co.kenrg.kagelang.tree.iface.LiteralTree
import co.kenrg.kagelang.tree.iface.base.Tree
import jdk.internal.org.objectweb.asm.Opcodes

data class KGType(
        val name: String,
        val className: String? = null,
        val isNumeric: Boolean = false,
        val isComparable: Boolean = false,
        val size: Int = 1,
        val props: Map<String, KGType> = hashMapOf(),
        val typeParams: List<KGType>? = null
) {
    companion object {
        val INT = KGType(name = "Int", isNumeric = true, isComparable = true)
        val DEC = KGType(name = "Dec", isNumeric = true, isComparable = true, size = 2)
        val BOOL = KGType(name = "Bool")
        val STRING = KGType(name = "String", className = "java/lang/String", isComparable = true)
        val UNIT = KGType(name = "Unit")

        fun stdLibType(stdLibType: StdLibT): KGType {
            val clazz = stdLibType.getTypeClass()
            return KGType(
                    name = clazz.simpleName,
                    className = stdLibType.className,

                    // TODO - Replace with declaredFields that have public getters
                    props = mapOf()
            )
        }
    }

    fun jvmDescriptor() = when (this) {
        INT -> "I"
        DEC -> "D"
        BOOL -> "Z"
        UNIT -> "V"
        else ->
            if (className != null) "L$className;"
            else null
    }

    fun genericSignature(): String? {
        return if (typeParams == null) {
            null
        } else if (className == null) {
            null
        } else {
            "${jvmDescriptor()?.trimEnd(';')}<${typeParams.map(KGType::jvmDescriptor)}>;"
        }
    }

    fun getLiteralKind(): Tree.Kind<LiteralTree> {
        return when (this) {
            INT -> Tree.Kind.IntLiteral
            DEC -> Tree.Kind.DecLiteral
            BOOL -> Tree.Kind.BoolLiteral
            STRING -> Tree.Kind.StringLiteral
            else -> throw UnsupportedOperationException("$this is not a literal, and has no literal kind")
        }
    }

    fun getReturnInsn(): Int {
        return when (this) {
            KGType.INT -> Opcodes.IRETURN
            KGType.DEC -> Opcodes.DRETURN
            KGType.BOOL -> Opcodes.IRETURN
            KGType.STRING -> Opcodes.ARETURN
            KGType.UNIT -> Opcodes.RETURN
            else ->
                if (this.className != null)
                    Opcodes.ARETURN
                else
                    throw UnsupportedOperationException("Cannot perform return for type: $this")
        }
    }
}

private val defaultTypes = listOf(KGType.INT, KGType.DEC, KGType.BOOL, KGType.STRING, KGType.UNIT)

// Convenience method for nullable-chaining
fun String.asKGType(): KGType? = defaultTypes.find { it.name == this }
