package co.kenrg.kagelang.tree.types

import jdk.internal.org.objectweb.asm.Opcodes

data class KGType(
        val name: String,
        val className: String,
        val isPrimitive: Boolean = false,
        val isNumeric: Boolean = false,
        val isComparable: Boolean = false,
        val size: Int = 1,
        val props: Map<String, KGType> = hashMapOf(),
        val typeParams: List<KGType>? = null,
        val isGeneric: Boolean = false
) {
    companion object {
        val INT = KGType(name = "Int", isPrimitive = true, className = "java/lang/Integer", isNumeric = true, isComparable = true)
        val DEC = KGType(name = "Dec", isPrimitive = true, className = "java/lang/Double", isNumeric = true, isComparable = true, size = 2)
        val BOOL = KGType(name = "Bool", isPrimitive = true, className = "java/lang/Boolean")
        val STRING = KGType(name = "String", className = "java/lang/String", isComparable = true)
        val UNIT = KGType(name = "Unit", className = "java/lang/Void")

        fun stdLibType(stdLibType: StdLibTypes): KGType {
            val clazz = stdLibType.getTypeClass()
            return KGType(
                    name = clazz.simpleName,
                    className = stdLibType.className,

                    // TODO - Replace with declaredFields that have public getters
                    props = mapOf(),
                    isGeneric = clazz.toGenericString().contains(Regex("<.*>"))
            )
        }
    }

    fun jvmDescriptor() = when (this) {
        INT -> "I"
        DEC -> "D"
        BOOL -> "Z"
        UNIT -> "V"
        else -> "L$className;"
    }

    fun genericSignature() =
            if (typeParams == null) null
            else "${jvmDescriptor().trimEnd(';')}<${typeParams.map { "L${it.className};" }.joinToString("")}>;"


    fun getReturnInsn(): Int {
        return when (this) {
            KGType.INT -> Opcodes.IRETURN
            KGType.DEC -> Opcodes.DRETURN
            KGType.BOOL -> Opcodes.IRETURN
            KGType.UNIT -> Opcodes.RETURN
            else -> Opcodes.ARETURN // Includes KGType.STRING
        }
    }
}

private val defaultTypes = listOf(KGType.INT, KGType.DEC, KGType.BOOL, KGType.STRING, KGType.UNIT)

// Convenience method for nullable-chaining
fun String.asKGType(): KGType? {
    val type = defaultTypes.find { it.name == this }
    if (type != null)
        return type

    try {
        return KGType.stdLibType(StdLibTypes.valueOf(this))
    } catch (e: IllegalArgumentException) {
        throw UnsupportedOperationException("Cannot find stdlib class for type: $this")
    }
}
