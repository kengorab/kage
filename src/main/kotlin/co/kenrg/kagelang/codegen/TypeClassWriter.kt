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
        typeProps.forEach {
            val (index, name, type) = it
            val typeSignature = if (type.isGeneric) type.genericSignature() else null
            codeGenVisitor.cw.visitField(ACC_PRIVATE, name, type.jvmDescriptor(), typeSignature, null)
        }

        val constructorParams = type.props.values.map {
            if (it.isGeneric) it.genericSignature()
            else it.jvmDescriptor()
        }
        val constructorDesc = "(${type.props.values.map(KGType::jvmDescriptor).joinToString("")})V"
        val constructorSignature =
                if (type.props.values.any { it.isGeneric }) "(${constructorParams.joinToString("")})V"
                else null

        val initWriter = codeGenVisitor.cw.visitMethod(ACC_PUBLIC, "<init>", constructorDesc, constructorSignature, null)
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
            initWriter.visitFieldInsn(PUTFIELD, innerClassName, name, type.jvmDescriptor())
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

        if (typeProps.isEmpty()) {
            toStringWriter.visitLdcInsn("${type.name}()")
        } else {
            typeProps.forEach {
                val (index, name, type) = it
                val jvmDesc = when (type) {
                    KGType.INT, KGType.DEC, KGType.BOOL, KGType.STRING -> type.jvmDescriptor()
                    else -> "Ljava/lang/Object;"
                }

                if (index == 1) toStringWriter.visitLdcInsn("${this.type.name}($name: ")
                else toStringWriter.visitLdcInsn("$name: ")
                toStringWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(${KGType.STRING.jvmDescriptor()})Ljava/lang/StringBuilder;", false)

                if (type == KGType.STRING) {
                    toStringWriter.visitLdcInsn("\"")
                    toStringWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(${KGType.STRING.jvmDescriptor()})Ljava/lang/StringBuilder;", false)
                }

                toStringWriter.visitVarInsn(ALOAD, 0)
                toStringWriter.visitFieldInsn(GETFIELD, innerClassName, name, type.jvmDescriptor())
                toStringWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "($jvmDesc)Ljava/lang/StringBuilder;", false)

                if (type == KGType.STRING) {
                    toStringWriter.visitLdcInsn("\"")
                    toStringWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(${KGType.STRING.jvmDescriptor()})Ljava/lang/StringBuilder;", false)
                }

                if (index < typeProps.size) {
                    toStringWriter.visitLdcInsn(", ")
                    toStringWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(${KGType.STRING.jvmDescriptor()})Ljava/lang/StringBuilder;", false)
                }
            }

            toStringWriter.visitLdcInsn(")")
            toStringWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(${KGType.STRING.jvmDescriptor()})Ljava/lang/StringBuilder;", false)
            toStringWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        }

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

        equalsWriter.visitVarInsn(ALOAD, 1)
        equalsWriter.visitTypeInsn(CHECKCAST, innerClassName)
        equalsWriter.visitVarInsn(ASTORE, 2)

        typeProps.forEach {
            val (index, name, type) = it
            equalsWriter.visitVarInsn(ALOAD, 0)
            equalsWriter.visitFieldInsn(GETFIELD, innerClassName, name, type.jvmDescriptor())

            equalsWriter.visitVarInsn(ALOAD, 2)
            equalsWriter.visitFieldInsn(GETFIELD, innerClassName, name, type.jvmDescriptor())

            when (type) {
                KGType.INT -> equalsWriter.visitJumpInsn(IF_ICMPNE, equalsFalseLbl)
                KGType.DEC -> {
                    equalsWriter.visitInsn(DCMPL)
                    equalsWriter.visitJumpInsn(IFNE, equalsFalseLbl)
                }
                KGType.BOOL -> equalsWriter.visitJumpInsn(IF_ICMPNE, equalsFalseLbl)
                else -> {
                    equalsWriter.visitMethodInsn(INVOKEVIRTUAL, type.jvmDescriptor(), "equals", "(Ljava/lang/Object;)Z", false)
                    equalsWriter.visitJumpInsn(IFEQ, equalsFalseLbl)
                }
            }
        }

        equalsWriter.visitLabel(equalsTrueLbl)
        equalsWriter.visitInsn(ICONST_1)
        equalsWriter.visitInsn(IRETURN)

        equalsWriter.visitLabel(equalsFalseLbl)
        equalsWriter.visitInsn(ICONST_0)
        equalsWriter.visitInsn(IRETURN)

        equalsWriter.visitMaxs(-1, -1)
        equalsWriter.visitEnd()
    }

    fun writePropertyAccessorMethods() {
        typeProps.forEach {
            val (index, name, type) = it

            val accessorName = "get${name.capitalize()}"
            val accessorDesc = "()${type.jvmDescriptor()}"
            val accessorSignature = if (type.isGeneric) "()${type.genericSignature()}" else null
            val propAccessorWriter = codeGenVisitor.cw.visitMethod(ACC_PUBLIC, accessorName, accessorDesc, accessorSignature, null)

            propAccessorWriter.visitVarInsn(ALOAD, 0)
            propAccessorWriter.visitFieldInsn(GETFIELD, innerClassName, name, type.jvmDescriptor())
            propAccessorWriter.visitInsn(type.getReturnInsn())

            propAccessorWriter.visitMaxs(-1, -1)
            propAccessorWriter.visitEnd()
        }
    }
}