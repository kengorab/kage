package co.kenrg.kagelang.tree.types

import jdk.internal.org.objectweb.asm.Opcodes
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
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
        val genericTypes: Map<String, KGType>? = null,
        val superType: KGType? = KGType.ANY
) {
    data class PropType(val type: KGType, val isGeneric: Boolean)

    companion object {
        val INT = KGType(name = "Int", isPrimitive = true, className = "java/lang/Integer", isNumeric = true, isComparable = true)
        val DEC = KGType(name = "Dec", isPrimitive = true, className = "java/lang/Double", isNumeric = true, isComparable = true, size = 2)
        val BOOL = KGType(name = "Bool", isPrimitive = true, className = "java/lang/Boolean")
        val STRING = KGType(name = "String", className = "java/lang/String", isComparable = true)
        val UNIT = KGType(name = "Unit", className = "java/lang/Void")
        val ANY = KGType(name = "Any", className = "java/lang/Object")
        val NOTHING = KGType(name = "Nothing", className = "java/lang/Object")

        fun fromPrimitive(primitive: String): KGType {
            return when (primitive) {
                "int" -> INT
                else -> throw UnsupportedOperationException("Transformation of primitive $primitive is not handled")
            }
        }

        private fun Class<*>.genericTypeNames(): List<String>? {
            val classGenericStr = this.toGenericString()
            val genericTypeRegex = Regex(".*<(\\w(?:,\\s*\\w)*)>.*")
            return genericTypeRegex.matchEntire(classGenericStr)?.groupValues?.get(1)?.split(',')
        }

        private fun ParameterizedTypeImpl.typeInfo(): Pair<String, List<String>?> {
            val genericTypeRegex = Regex("(.*)<(\\w(?:,\\s*\\w)*)>.*")
            val groupValues = genericTypeRegex.matchEntire(this.typeName)?.groupValues
            val typeName = groupValues?.get(1)!!
            val typeParameters = groupValues?.get(2)?.split(',')?.map(String::trim)
            return Pair(typeName, typeParameters)
        }

        fun stdLibType(stdLibType: StdLibType, typeParams: List<KGType> = listOf()): KGType {
            val clazz = stdLibType.getTypeClass()

            val genericTypes = if (typeParams.isNotEmpty()) {
                val genericTypeNames = clazz.genericTypeNames()
                        ?: throw IllegalStateException("Type params passed to non-generic type")

                if (typeParams.size != genericTypeNames.size)
                    throw IllegalStateException("Number of type params provided to generic type is incorrect")

                genericTypeNames.zip(typeParams).toMap()
            } else mapOf()

            return stdLibType(stdLibType, genericTypes)
        }

        fun stdLibType(stdLibType: StdLibType, genericTypes: Map<String, KGType> = mapOf()): KGType {
            val clazz = stdLibType.getTypeClass()
            val classGenericStr = clazz.toGenericString()

            val classFields = clazz.declaredFields
            val classMethods = clazz.declaredMethods

            val props = classFields
                    .filter { field ->
                        classMethods.filter { method -> method.name == "get${field.name.capitalize()}" }.isNotEmpty()
                    }
                    .map { field ->
                        val fieldAnnotatedType = field.annotatedType.type
                        val fieldType = when (fieldAnnotatedType) {
                            is Class<*> -> {
                                // TODO - Handle Type props whose type is non-generic (i.e. int)
                                throw UnsupportedOperationException("Accessing non-generic prop '${field.name}' of a stdlib type")
                            }
                            is TypeVariableImpl<*> -> {
                                if (genericTypes.isEmpty())
                                    PropType(KGType.ANY, true)
                                else if (!genericTypes.containsKey(fieldAnnotatedType.name))
                                    throw IllegalStateException("Type has no type parameter '${fieldAnnotatedType.name}'")
                                else
                                    PropType(genericTypes[fieldAnnotatedType.name]!!, true)
                            }
                            else -> throw UnsupportedOperationException("Unknown kind of type for field '${field.name}': '${fieldAnnotatedType.javaClass.canonicalName}'")
                        }
                        field.name to fieldType
                    }
                    .toMap()

            val genericSuperclass = clazz.genericSuperclass
            val superType = when (genericSuperclass) {
                is Class<*> -> KGType.ANY // TODO - Handle proper sub/superTypes
                is ParameterizedTypeImpl -> {
                    val (typeClassName, typeParams) = genericSuperclass.typeInfo()
                    val stdLibTypeForName = StdLibType.forClassName(typeClassName)
                            ?: throw UnsupportedOperationException("Superclasses that aren't stdlib are not yet supported")

                    val typeParamTypes = typeParams?.map {
                        genericTypes[it] ?: KGType.NOTHING
                    } ?: listOf()

                    KGType.stdLibType(stdLibTypeForName, typeParamTypes)
                }
                else -> throw UnsupportedOperationException("")
            }

            return KGType(
                    name = clazz.simpleName,
                    className = stdLibType.className,

                    props = props,
                    isGeneric = classGenericStr.contains(Regex("<.*>")),
                    genericTypes = genericTypes,
                    typeParams = clazz.genericTypeNames()?.map { genericTypes[it] ?: KGType.ANY } ?: listOf(),
                    superType = superType
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

    fun isAssignableToType(other: KGType): Boolean {
        return this == other || other == KGType.ANY || this == KGType.NOTHING
                || (
                if (this.typeParams == null || other.typeParams == null) false
                else this.typeParams.zip(other.typeParams).fold(true) { acc, pair -> acc && pair.first.isAssignableToType(pair.second) }
                )
                || this.superType?.isAssignableToType(other) ?: false
    }

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

private val defaultTypes = listOf(KGType.INT, KGType.DEC, KGType.BOOL, KGType.STRING, KGType.UNIT, KGType.ANY)

// Convenience method for nullable-chaining
fun String.asKGType(typeParams: List<KGType> = listOf()): KGType? {
    val type = defaultTypes.find { it.name == this }
    if (type != null)
        return type

    try {
        return KGType.stdLibType(StdLibType.valueOf(this), typeParams)
    } catch (e: IllegalArgumentException) {
        throw UnsupportedOperationException("Cannot find stdlib class for type: $this")
    }
}
