package co.kenrg.kagelang.tree.types

import co.kenrg.kagelang.tree.iface.LiteralTree
import co.kenrg.kagelang.tree.iface.base.Tree
import jdk.internal.org.objectweb.asm.Opcodes

data class KGType(
        val name: String,
        val jvmDescriptor: String,
        val className: String? = null,
        val isNumeric: Boolean = false,
        val isComparable: Boolean = false,
        val size: Int = 1,
        val props: Map<String, KGType> = hashMapOf()
) {
    companion object {
        val INT = KGType(name = "Int", jvmDescriptor = "I", isNumeric = true, isComparable = true)
        val DEC = KGType(name = "Dec", jvmDescriptor = "D", isNumeric = true, isComparable = true, size = 2)
        val BOOL = KGType(name = "Bool", jvmDescriptor = "Z")
        val STRING = KGType(name = "String", jvmDescriptor = "Ljava/lang/String;", className = "java/lang/String", isComparable = true)

        val UNIT = KGType(name = "Unit", jvmDescriptor = "V")
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
