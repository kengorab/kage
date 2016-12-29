package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.iface.base.Tree
import co.kenrg.kagelang.tree.types.KGTypeTag
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.Label
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes.*
import org.apache.commons.collections4.map.LinkedMap

/**
 * An implementor of the Visitor interface, this visits each node in the tree
 * and generates JVM bytecode. In the first pass of this implementation, everything
 * is contained in the "main" method of the class that gets generated (which, by default
 * is called MyClass).
 *
 * In each of the visitor methods, the `data` parameter contains the bindings
 * currently visible in scope.
 *
 * @see KGTree.Visitor
 */
@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class CodeGenVisitor(
        className: String = "MyClass"
) : KGTree.Visitor<LinkedMap<String, CodeGenBinding>> {
    data class FocusedMethod(val writer: MethodVisitor, val start: Label, val end: Label)

    val cw: ClassWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)

    // This reference will be updated to reflect the current method being written. By default,
    // it is the main method of the class, as seen in the `init` block below. When defining
    // top-level functions, this will be updated to hold the writer and other info for that
    // function. It is imperative that this be set back to the main method's context afterwards.
    var focusedMethod: FocusedMethod

    init {
        cw.visit(V1_8, ACC_PUBLIC, className, null, "java/lang/Object", null)

        val mainMethodWriter = cw.visitMethod(ACC_PUBLIC or ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null)
        mainMethodWriter.visitCode()
        val mainMethodStart = Label()
        val mainMethodEnd = Label()
        mainMethodWriter.visitLabel(mainMethodStart)

        focusedMethod = FocusedMethod(writer = mainMethodWriter, start = mainMethodStart, end = mainMethodEnd)
    }

    /**
     * After calling accept on a <code>Tree</code> with this visitor, call this method to retrieve
     * the bytecode representation of that tree.
     *
     * @see Tree
     * @see KGTree.Visitor
     */
    fun resultBytes(): ByteArray? {
        val (methodWriter, methodStart, methodEnd) = focusedMethod

        methodWriter.visitLabel(methodEnd)
        methodWriter.visitInsn(RETURN)
        methodWriter.visitEnd()
        methodWriter.visitMaxs(-1, -1)
        cw.visitEnd()
        return cw.toByteArray()
    }

    /*****************************
     *
     *    Expression Visitors
     *
     *****************************/

    override fun visitLiteral(literal: KGTree.KGLiteral, data: LinkedMap<String, CodeGenBinding>) {
        val methodWriter = focusedMethod.writer

        when (literal.typeTag) {
            KGTypeTag.INT -> methodWriter.visitLdcInsn(literal.value as java.lang.Integer)
            KGTypeTag.DEC -> methodWriter.visitLdcInsn(literal.value as java.lang.Double)
            KGTypeTag.BOOL -> methodWriter.visitLdcInsn(literal.value as java.lang.Boolean)
            KGTypeTag.STRING -> methodWriter.visitLdcInsn(literal.value as java.lang.String)
            else -> throw UnsupportedOperationException("Literal $literal is not a literal")
        }
    }

    override fun visitUnary(unary: KGTree.KGUnary, data: LinkedMap<String, CodeGenBinding>) {
        val methodWriter = focusedMethod.writer

        if (unary.type == KGTypeTag.UNSET) {
            throw IllegalStateException("Cannot run codegen on Unary subtree with UNSET type; was the tree successfully run through TypeCheckerAttributorVisitor?")
        }

        when (unary.kind()) {
            is Tree.Kind.ArithmeticNegation -> {
                unary.expr.accept(this, data)
                when (unary.expr.type) {
                    KGTypeTag.INT -> methodWriter.visitInsn(INEG)
                    KGTypeTag.DEC -> methodWriter.visitInsn(DNEG)
                    else -> throw IllegalStateException("Cannot run codegen on arithmetic negation which is not numeric")
                }
            }
            is Tree.Kind.BooleanNegation -> {
                val negationEndLbl = Label()
                val negationFalseLbl = Label()

                unary.expr.accept(this, data)
                methodWriter.visitJumpInsn(IFNE, negationFalseLbl)
                methodWriter.visitInsn(ICONST_1)
                methodWriter.visitJumpInsn(GOTO, negationEndLbl)

                methodWriter.visitLabel(negationFalseLbl)
                methodWriter.visitInsn(ICONST_0)

                methodWriter.visitLabel(negationEndLbl)
            }
        }
    }

    fun pushCondAnd(left: KGTree, right: KGTree, methodWriter: MethodVisitor, data: LinkedMap<String, CodeGenBinding>) {
        val condEndLbl = Label()
        val condFalseLbl = Label()
        left.accept(this, data)
        methodWriter.visitJumpInsn(IFEQ, condFalseLbl)
        right.accept(this, data)
        methodWriter.visitJumpInsn(IFEQ, condFalseLbl)
        methodWriter.visitInsn(ICONST_1)
        methodWriter.visitJumpInsn(GOTO, condEndLbl)

        methodWriter.visitLabel(condFalseLbl)
        methodWriter.visitInsn(ICONST_0)

        methodWriter.visitLabel(condEndLbl)
    }

    fun pushCondOr(left: KGTree, right: KGTree, methodWriter: MethodVisitor, data: LinkedMap<String, CodeGenBinding>) {
        val condEndLbl = Label()
        val condTrueLbl = Label()
        val condFalseLbl = Label()
        left.accept(this, data)
        methodWriter.visitJumpInsn(IFNE, condTrueLbl)
        right.accept(this, data)
        methodWriter.visitJumpInsn(IFEQ, condFalseLbl)

        methodWriter.visitLabel(condTrueLbl)
        methodWriter.visitInsn(ICONST_1)
        methodWriter.visitJumpInsn(GOTO, condEndLbl)

        methodWriter.visitLabel(condFalseLbl)
        methodWriter.visitInsn(ICONST_0)

        methodWriter.visitLabel(condEndLbl)
    }

    fun pushArithmeticalOperands(binary: KGTree.KGBinary, methodWriter: MethodVisitor, data: LinkedMap<String, CodeGenBinding>) {
        binary.left.accept(this, data)
        if (binary.left.type == KGTypeTag.INT && binary.type == KGTypeTag.DEC) {
            methodWriter.visitInsn(I2D)
        }

        binary.right.accept(this, data)
        if (binary.right.type == KGTypeTag.INT && binary.type == KGTypeTag.DEC) {
            methodWriter.visitInsn(I2D)
        }
    }

    fun appendStrings(tree: KGTree, methodWriter: MethodVisitor, data: LinkedMap<String, CodeGenBinding>) {
        // Recurse down through the tree and allow it to be handled by this visitor, shortcutting if the tree is a
        // String concatenation, because we don't want to create duplicate StringBuilders.
        if (tree is KGTree.KGBinary && tree.kind() is Tree.Kind.Concatenation) {
            appendStrings(tree.left, methodWriter, data)
            appendStrings(tree.right, methodWriter, data)
            return
        }

        tree.accept(this, data)
        val treeType = jvmTypeDescriptorForType(tree.type)
        methodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "($treeType)Ljava/lang/StringBuilder;", false)
    }

    fun pushStringConcatenation(binary: KGTree.KGBinary, methodWriter: MethodVisitor, data: LinkedMap<String, CodeGenBinding>) {
        // Create and initialize a StringBuilder, leaving a reference on TOS.
        methodWriter.visitTypeInsn(NEW, "java/lang/StringBuilder")
        methodWriter.visitInsn(DUP)
        methodWriter.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)

        // Traverse the tree, appending to the StringBuilder.
        appendStrings(binary, methodWriter, data)

        // After the concatenation traversal is complete, build the String and place on TOS.
        methodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
    }

    override fun visitBinary(binary: KGTree.KGBinary, data: LinkedMap<String, CodeGenBinding>) {
        val methodWriter = focusedMethod.writer

        if (binary.type == KGTypeTag.UNSET) {
            throw IllegalStateException("Cannot run codegen on Binary subtree with UNSET type; was the tree successfully run through TypeCheckerAttributorVisitor?")
        }

        when (binary.kind()) {
            is Tree.Kind.Plus -> {
                pushArithmeticalOperands(binary, methodWriter, data)
                when (binary.type) {
                    KGTypeTag.INT -> methodWriter.visitInsn(IADD)
                    KGTypeTag.DEC -> methodWriter.visitInsn(DADD)
                    else -> throw IllegalStateException("Binary's kind is Plus, but type is non-numeric")
                }
            }
            is Tree.Kind.Minus -> {
                pushArithmeticalOperands(binary, methodWriter, data)
                when (binary.type) {
                    KGTypeTag.INT -> methodWriter.visitInsn(ISUB)
                    KGTypeTag.DEC -> methodWriter.visitInsn(DSUB)
                    else -> throw IllegalStateException("Binary's kind is Minus, but type is non-numeric")
                }
            }
            is Tree.Kind.Multiply -> {
                pushArithmeticalOperands(binary, methodWriter, data)
                when (binary.type) {
                    KGTypeTag.INT -> methodWriter.visitInsn(IMUL)
                    KGTypeTag.DEC -> methodWriter.visitInsn(DMUL)
                    else -> throw IllegalStateException("Binary's kind is Multiply, but type is non-numeric")
                }
            }
            is Tree.Kind.Divide -> {
                pushArithmeticalOperands(binary, methodWriter, data)
                when (binary.type) {
                    KGTypeTag.INT -> methodWriter.visitInsn(IDIV)
                    KGTypeTag.DEC -> methodWriter.visitInsn(DDIV)
                    else -> throw IllegalStateException("Binary's kind is Divide, but type is non-numeric")
                }
            }
            is Tree.Kind.ConditionalAnd -> {
                when (binary.type) {
                    KGTypeTag.BOOL -> pushCondAnd(binary.left, binary.right, methodWriter, data)
                    else -> throw IllegalStateException("Binary's kind is ConditionalAnd, but type is non-boolean")
                }
            }
            is Tree.Kind.ConditionalOr -> {
                when (binary.type) {
                    KGTypeTag.BOOL -> pushCondOr(binary.left, binary.right, methodWriter, data)
                    else -> throw IllegalStateException("Binary's kind is ConditionalOr, but type is non-boolean")
                }
            }
            is Tree.Kind.Concatenation -> {
                when (binary.type) {
                    KGTypeTag.STRING -> pushStringConcatenation(binary, methodWriter, data)
                    else -> throw IllegalStateException("Binary's kind is Concatenation, but type is non-String")
                }
            }
        }
    }

    override fun visitParenthesized(parenthesized: KGTree.KGParenthesized, data: LinkedMap<String, CodeGenBinding>) {
        parenthesized.expr.accept(this, data)
    }

    override fun visitBindingReference(bindingReference: KGTree.KGBindingReference, data: LinkedMap<String, CodeGenBinding>) {
        val methodWriter = focusedMethod.writer

        val name = bindingReference.binding
        val binding = data[name]
                ?: throw IllegalStateException("Binding $name not present in current context")

        when (binding.type) {
            KGTypeTag.INT -> methodWriter.visitVarInsn(ILOAD, binding.index)
            KGTypeTag.DEC -> methodWriter.visitVarInsn(DLOAD, binding.index)
            KGTypeTag.BOOL -> methodWriter.visitVarInsn(ILOAD, binding.index)
            KGTypeTag.STRING -> methodWriter.visitVarInsn(ALOAD, binding.index)
            else -> throw IllegalStateException("Cannot resolve binding $name of type ${binding.type}")
        }
    }

    /*****************************
     *
     *     Statement Visitors
     *
     *****************************/

    private fun jvmTypeDescriptorForType(type: KGTypeTag) = when (type) {
        KGTypeTag.INT -> "I"
        KGTypeTag.DEC -> "D"
        KGTypeTag.BOOL -> "Z"
        KGTypeTag.STRING -> "Ljava/lang/String;"
        else -> throw IllegalStateException("JVM descriptor for type $type not yet implemented")
    }

    override fun visitPrint(print: KGTree.KGPrint, data: LinkedMap<String, CodeGenBinding>) {
        val methodWriter = focusedMethod.writer

        methodWriter.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        print.expr.accept(this, data)

        val type = jvmTypeDescriptorForType(print.expr.type)
        val printlnSignature = "($type)V"

        methodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", printlnSignature, false)
    }

    private fun getIndexForNextLocalVariable(data: LinkedMap<String, CodeGenBinding>, startIndex: Int = 0): Int {
        return if (data.size == 0) {
            startIndex
        } else {
            data.size + data[data.lastKey()]!!.size + startIndex
        }
    }

    override fun visitValDeclaration(valDecl: KGTree.KGValDeclaration, data: LinkedMap<String, CodeGenBinding>) {
        val (methodWriter, methodStart, methodEnd) = focusedMethod

        val valName = valDecl.identifier
        if (data.containsKey(valName)) {
            throw IllegalStateException("Cannot declare duplicate binding $valName")
        }

        val valDeclExpr = valDecl.expression
        val typeDesc = jvmTypeDescriptorForType(valDeclExpr.type)

        // Everything we're doing RIGHT NOW is wrapped in the main method of the generated class,
        // and since the main method receives args, the 0th local variable is args. So any additional
        // local variables we declare must account for that. This will change in the future.
        val bindingIndex = getIndexForNextLocalVariable(data, startIndex = 1)
        data.put(valName, CodeGenBinding(valName, valDeclExpr.type, bindingIndex))

        methodWriter.visitLocalVariable(valName, typeDesc, null, methodStart, methodEnd, bindingIndex)
        valDeclExpr.accept(this, data)
        when (valDeclExpr.type) {
            KGTypeTag.INT -> methodWriter.visitVarInsn(ISTORE, bindingIndex)
            KGTypeTag.DEC -> methodWriter.visitVarInsn(DSTORE, bindingIndex)
            KGTypeTag.BOOL -> methodWriter.visitVarInsn(ISTORE, bindingIndex)
            KGTypeTag.STRING -> methodWriter.visitVarInsn(ASTORE, bindingIndex)
            else -> throw IllegalStateException("Cannot store variable of type ${valDeclExpr.type} in binding $valName")
        }
    }

    override fun visitFnDeclaration(fnDecl: KGTree.KGFnDeclaration, data: LinkedMap<String, CodeGenBinding>) {
        throw UnsupportedOperationException("not implemented")
    }
}