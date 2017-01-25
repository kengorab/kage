package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.tree.types.KGType
import jdk.internal.org.objectweb.asm.Label
import jdk.internal.org.objectweb.asm.Opcodes.*

class TypeClassWriter(val codeGenVisitor: CodeGenVisitor, val type: KGType, val innerClassName: String) {
    val typeProps: List<Triple<Int, String, KGType>>

    init {
        typeProps = type.props.entries.mapIndexed { index, prop ->
            // The 0th param to the constructor method is `this`
            Triple(index + 1, prop.key, prop.value)
        }
    }

    fun getResultingBytecode() = codeGenVisitor.results()

    fun writeConstructor() {
        val constructorSignature = "(${type.props.values.map { it.jvmDescriptor }.joinToString("")})V"

        typeProps.forEach {
            val (index, name, type) = it
            codeGenVisitor.cw.visitField(ACC_PRIVATE, name, type.jvmDescriptor, null, null)
        }

        val initWriter = codeGenVisitor.cw.visitMethod(ACC_PUBLIC, "<init>", constructorSignature, null, null)
        initWriter.visitCode()
        initWriter.visitVarInsn(ALOAD, 0)
        initWriter.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)

        typeProps.forEach {
            val (index, name, type) = it
            initWriter.visitVarInsn(ALOAD, 0)

            when (type) {
                KGType.INT -> initWriter.visitVarInsn(ILOAD, index)
                KGType.DEC -> initWriter.visitVarInsn(DLOAD, index)
                KGType.BOOL -> initWriter.visitVarInsn(ILOAD, index)
                else -> initWriter.visitVarInsn(ALOAD, index)   // Including KGType.STRING
            }
            initWriter.visitFieldInsn(PUTFIELD, innerClassName, name, type.jvmDescriptor)
        }

        initWriter.visitInsn(RETURN)
        initWriter.visitMaxs(-1, -1)
        initWriter.visitEnd()
    }

    fun writeToStringMethod() {
        val toStringWriter = codeGenVisitor.cw.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null)
        toStringWriter.visitCode()
        toStringWriter.visitTypeInsn(NEW, "java/lang/StringBuilder")
        toStringWriter.visitInsn(DUP)
        toStringWriter.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)

        typeProps.forEach {
            val (index, name, type) = it
            val jvmDesc = when (type) {
                KGType.INT, KGType.DEC, KGType.BOOL, KGType.STRING -> type.jvmDescriptor
                else -> "Ljava/lang/Object;"
            }

            if (index == 1) toStringWriter.visitLdcInsn("${this.type.name}($name: ")
            else toStringWriter.visitLdcInsn("$name: ")
            toStringWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(${KGType.STRING.jvmDescriptor})Ljava/lang/StringBuilder;", false)

            if (type == KGType.STRING) {
                toStringWriter.visitLdcInsn("\"")
                toStringWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(${KGType.STRING.jvmDescriptor})Ljava/lang/StringBuilder;", false)
            }

            toStringWriter.visitVarInsn(ALOAD, 0)
            toStringWriter.visitFieldInsn(GETFIELD, innerClassName, name, type.jvmDescriptor)
            toStringWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "($jvmDesc)Ljava/lang/StringBuilder;", false)

            if (type == KGType.STRING) {
                toStringWriter.visitLdcInsn("\"")
                toStringWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(${KGType.STRING.jvmDescriptor})Ljava/lang/StringBuilder;", false)
            }

            if (index < typeProps.size) {
                toStringWriter.visitLdcInsn(", ")
                toStringWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(${KGType.STRING.jvmDescriptor})Ljava/lang/StringBuilder;", false)
            }
        }

        toStringWriter.visitLdcInsn(")")
        toStringWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(${KGType.STRING.jvmDescriptor})Ljava/lang/StringBuilder;", false)
        toStringWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)

        toStringWriter.visitInsn(ARETURN)
        toStringWriter.visitMaxs(-1, -1)
        toStringWriter.visitEnd()
    }

    fun writeEqualsMethod() {
        val equalsWriter = codeGenVisitor.cw.visitMethod(ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null)
        val equalsTrueLbl = Label()
        val equalsFalseLbl = Label()

        equalsWriter.visitVarInsn(ALOAD, 0)
        equalsWriter.visitVarInsn(ALOAD, 1)
        equalsWriter.visitJumpInsn(IF_ACMPEQ, equalsTrueLbl)

        equalsWriter.visitVarInsn(ALOAD, 1)
        equalsWriter.visitTypeInsn(INSTANCEOF, innerClassName)
        equalsWriter.visitJumpInsn(IFEQ, equalsFalseLbl)

        // Compare properties here

        equalsWriter.visitLabel(equalsTrueLbl)
        equalsWriter.visitInsn(ICONST_1)
        equalsWriter.visitInsn(IRETURN)

        equalsWriter.visitLabel(equalsFalseLbl)
        equalsWriter.visitInsn(ICONST_0)
        equalsWriter.visitInsn(IRETURN)

        equalsWriter.visitMaxs(-1, -1)
        equalsWriter.visitEnd()
    }
}