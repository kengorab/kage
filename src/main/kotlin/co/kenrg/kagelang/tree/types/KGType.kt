package co.kenrg.kagelang.tree.types

import jdk.internal.org.objectweb.asm.Opcodes
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl

data class KGType(
        val name: String,
        val className: String,
        val isPrimitive: Boolean = false,
        val isNumeric: Boolean = false,
        val isComparable: Boolean = false,
        val size: Int = 1,
        val props: Map<String, PropType> = hashMapOf(),
        val typeParams: List<KGType>? = null,
        val isGeneric: Boolean = false,
        val genericTypes: Map<String, KGType>? = null
) {
    data class PropType(val type: KGType, val isGeneric: Boolean)

    companion object {
        val INT = KGType(name = "Int", isPrimitive = true, className = "java/lang/Integer", isNumeric = true, isComparable = true)
        val DEC = KGType(name = "Dec", isPrimitive = true, className = "java/lang/Double", isNumeric = true, isComparable = true, size = 2)
        val BOOL = KGType(name = "Bool", isPrimitive = true, className = "java/lang/Boolean")
        val STRING = KGType(name = "String", className = "java/lang/String", isComparable = true)
        val UNIT = KGType(name = "Unit", className = "java/lang/Void")

        fun fromPrimitive(primitive: String): KGType {
            return when (primitive) {
                "int" -> INT
                else -> throw UnsupportedOperationException("Transformation of primitive $primitive is not handled")
            }
        }

        fun stdLibType(stdLibType: StdLibTypes, typeParams: List<KGType> = listOf()): KGType {
            val clazz = stdLibType.getTypeClass()
            val genericTypeRegex = Regex(".*<(\\w(?:,\\s*\\w)*)>.*")
            val classGenericStr = clazz.toGenericString()

            val genericTypes = if (typeParams.isNotEmpty()) {
                val genericTypeNames = genericTypeRegex.matchEntire(classGenericStr)?.groupValues?.get(1)?.split(',')
                        ?: throw IllegalStateException("Type params passed to non-generic type")

                if (typeParams.size != genericTypeNames.size)
                    throw IllegalStateException("Number of type params provided to generic type is incorrect")

                genericTypeNames.zip(typeParams).toMap()
            } else null

            val classFields = clazz.declaredFields
            val classMethods = clazz.declaredMethods

            val props = classFields
                    .filter { field ->
                        classMethods.filter { method -> method.name == "get${field.name.capitalize()}" }.isNotEmpty()
                    }
                    .map { field ->
                        val fieldAnnotatedType = field.annotatedType.type
                        val fieldType = when (fieldAnnotatedType) {
                            is Class<*> -> PropType(KGType.fromPrimitive(fieldAnnotatedType.name), false)
                            is TypeVariableImpl<*> -> {
                                if (genericTypes == null)
                                    throw IllegalStateException("Cannot evaluate generic type of prop '${field.name}'")

                                if (!genericTypes.containsKey(fieldAnnotatedType.name))
                                    throw IllegalStateException("Type has no type parameter '${fieldAnnotatedType.name}'")

                                PropType(genericTypes[fieldAnnotatedType.name]!!, true)
                            }
                            else -> throw UnsupportedOperationException("Unknown kind of type for field '${field.name}': '${fieldAnnotatedType.javaClass.canonicalName}'")
                        }
                        field.name to fieldType
                    }
                    .toMap()

            return KGType(
                    name = clazz.simpleName,
                    className = stdLibType.className,

                    props = props,
                    isGeneric = classGenericStr.contains(Regex("<.*>")),
                    genericTypes = genericTypes,
                    typeParams = typeParams
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
fun String.asKGType(typeParams: List<KGType> = listOf()): KGType? {
    val type = defaultTypes.find { it.name == this }
    if (type != null)
        return type

    try {
        return KGType.stdLibType(StdLibTypes.valueOf(this), typeParams)
    } catch (e: IllegalArgumentException) {
        throw UnsupportedOperationException("Cannot find stdlib class for type: $this")
    }
}
