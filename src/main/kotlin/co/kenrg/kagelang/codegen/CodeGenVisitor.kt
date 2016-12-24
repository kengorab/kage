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

    val cw: ClassWriter
    val mainMethodWriter: MethodVisitor
    val mainMethodStart: Label
    val mainMethodEnd: Label

    init {
        cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        cw.visit(V1_8, ACC_PUBLIC, className, null, "java/lang/Object", null)

        mainMethodWriter = cw.visitMethod(ACC_PUBLIC or ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null)
        mainMethodWriter.visitCode()
        mainMethodStart = Label()
        mainMethodEnd = Label()
        mainMethodWriter.visitLabel(mainMethodStart)
    }

    /**
     * After calling accept on a <code>Tree</code> with this visitor, call this method to retrieve
     * the bytecode representation of that tree.
     *
     * @see Tree
     * @see KGTree.Visitor
     */
    fun resultBytes(): ByteArray? {
        mainMethodWriter.visitLabel(mainMethodEnd)
        mainMethodWriter.visitInsn(RETURN)
        mainMethodWriter.visitEnd()
        mainMethodWriter.visitMaxs(-1, -1)
        cw.visitEnd()
        return cw.toByteArray()
    }

    /*****************************
     *
     *    Expression Visitors
     *
     *****************************/

    override fun visitLiteral(literal: KGTree.KGLiteral, data: LinkedMap<String, CodeGenBinding>) {
        when (literal.typeTag) {
            KGTypeTag.INT -> mainMethodWriter.visitLdcInsn(literal.value as java.lang.Integer)
            KGTypeTag.DEC -> mainMethodWriter.visitLdcInsn(literal.value as java.lang.Double)
            KGTypeTag.BOOL -> mainMethodWriter.visitLdcInsn(literal.value as java.lang.Boolean)
            KGTypeTag.STRING -> mainMethodWriter.visitLdcInsn(literal.value as java.lang.String)
            else -> throw UnsupportedOperationException("Literal $literal is not a literal")
        }
    }

    override fun visitUnary(unary: KGTree.KGUnary, data: LinkedMap<String, CodeGenBinding>) {
        if (unary.type == KGTypeTag.UNSET) {
            throw IllegalStateException("Cannot run codegen on Unary subtree with UNSET type; was the tree successfully run through TypeCheckerAttributorVisitor?")
        }

        when (unary.kind()) {
            is Tree.Kind.ArithmeticNegation -> {
                unary.expr.accept(this, data)
                when (unary.expr.type) {
                    KGTypeTag.INT -> mainMethodWriter.visitInsn(INEG)
                    KGTypeTag.DEC -> mainMethodWriter.visitInsn(DNEG)
                    else -> throw IllegalStateException("Cannot run codegen on arithmetic negation which is not numeric")
                }
            }
            is Tree.Kind.BooleanNegation -> {
                val negationEndLbl = Label()
                val negationFalseLbl = Label()

                unary.expr.accept(this, data)
                mainMethodWriter.visitJumpInsn(IFNE, negationFalseLbl)
                mainMethodWriter.visitInsn(ICONST_1)
                mainMethodWriter.visitJumpInsn(GOTO, negationEndLbl)

                mainMethodWriter.visitLabel(negationFalseLbl)
                mainMethodWriter.visitInsn(ICONST_0)

                mainMethodWriter.visitLabel(negationEndLbl)
            }
        }
    }

    fun pushCondAnd(left: KGTree, right: KGTree, mainMethodWriter: MethodVisitor, data: LinkedMap<String, CodeGenBinding>) {
        val condEndLbl = Label()
        val condFalseLbl = Label()
        left.accept(this, data)
        mainMethodWriter.visitJumpInsn(IFEQ, condFalseLbl)
        right.accept(this, data)
        mainMethodWriter.visitJumpInsn(IFEQ, condFalseLbl)
        mainMethodWriter.visitInsn(ICONST_1)
        mainMethodWriter.visitJumpInsn(GOTO, condEndLbl)

        mainMethodWriter.visitLabel(condFalseLbl)
        mainMethodWriter.visitInsn(ICONST_0)

        mainMethodWriter.visitLabel(condEndLbl)
    }

    fun pushCondOr(left: KGTree, right: KGTree, mainMethodWriter: MethodVisitor, data: LinkedMap<String, CodeGenBinding>) {
        val condEndLbl = Label()
        val condTrueLbl = Label()
        val condFalseLbl = Label()
        left.accept(this, data)
        mainMethodWriter.visitJumpInsn(IFNE, condTrueLbl)
        right.accept(this, data)
        mainMethodWriter.visitJumpInsn(IFEQ, condFalseLbl)

        mainMethodWriter.visitLabel(condTrueLbl)
        mainMethodWriter.visitInsn(ICONST_1)
        mainMethodWriter.visitJumpInsn(GOTO, condEndLbl)

        mainMethodWriter.visitLabel(condFalseLbl)
        mainMethodWriter.visitInsn(ICONST_0)

        mainMethodWriter.visitLabel(condEndLbl)
    }

    fun pushArithmeticalOperands(binary: KGTree.KGBinary, mainMethodWriter: MethodVisitor, data: LinkedMap<String, CodeGenBinding>) {
        binary.left.accept(this, data)
        if (binary.left.type == KGTypeTag.INT && binary.type == KGTypeTag.DEC) {
            mainMethodWriter.visitInsn(I2D)
        }

        binary.right.accept(this, data)
        if (binary.right.type == KGTypeTag.INT && binary.type == KGTypeTag.DEC) {
            mainMethodWriter.visitInsn(I2D)
        }
    }

    override fun visitBinary(binary: KGTree.KGBinary, data: LinkedMap<String, CodeGenBinding>) {
        if (binary.type == KGTypeTag.UNSET) {
            throw IllegalStateException("Cannot run codegen on Binary subtree with UNSET type; was the tree successfully run through TypeCheckerAttributorVisitor?")
        }

        when (binary.kind()) {
            is Tree.Kind.Plus -> {
                pushArithmeticalOperands(binary, mainMethodWriter, data)
                when (binary.type) {
                    KGTypeTag.INT -> mainMethodWriter.visitInsn(IADD)
                    KGTypeTag.DEC -> mainMethodWriter.visitInsn(DADD)
                    else -> throw IllegalStateException("Binary's kind is Plus, but type is non-numeric")
                }
            }
            is Tree.Kind.Minus -> {
                pushArithmeticalOperands(binary, mainMethodWriter, data)
                when (binary.type) {
                    KGTypeTag.INT -> mainMethodWriter.visitInsn(ISUB)
                    KGTypeTag.DEC -> mainMethodWriter.visitInsn(DSUB)
                    else -> throw IllegalStateException("Binary's kind is Minus, but type is non-numeric")
                }
            }
            is Tree.Kind.Multiply -> {
                pushArithmeticalOperands(binary, mainMethodWriter, data)
                when (binary.type) {
                    KGTypeTag.INT -> mainMethodWriter.visitInsn(IMUL)
                    KGTypeTag.DEC -> mainMethodWriter.visitInsn(DMUL)
                    else -> throw IllegalStateException("Binary's kind is Multiply, but type is non-numeric")
                }
            }
            is Tree.Kind.Divide -> {
                pushArithmeticalOperands(binary, mainMethodWriter, data)
                when (binary.type) {
                    KGTypeTag.INT -> mainMethodWriter.visitInsn(IDIV)
                    KGTypeTag.DEC -> mainMethodWriter.visitInsn(DDIV)
                    else -> throw IllegalStateException("Binary's kind is Divide, but type is non-numeric")
                }
            }
            is Tree.Kind.ConditionalAnd -> {
                when (binary.type) {
                    KGTypeTag.BOOL -> pushCondAnd(binary.left, binary.right, mainMethodWriter, data)
                    else -> throw IllegalStateException("Binary's kind is ConditionalAnd, but type is non-boolean")
                }
            }
            is Tree.Kind.ConditionalOr -> {
                when (binary.type) {
                    KGTypeTag.BOOL -> pushCondOr(binary.left, binary.right, mainMethodWriter, data)
                    else -> throw IllegalStateException("Binary's kind is ConditionalOr, but type is non-boolean")
                }
            }
        }
    }

    override fun visitParenthesized(parenthesized: KGTree.KGParenthesized, data: LinkedMap<String, CodeGenBinding>) {
        parenthesized.expr.accept(this, data)
    }

    override fun visitBindingReference(bindingReference: KGTree.KGBindingReference, data: LinkedMap<String, CodeGenBinding>) {
        val name = bindingReference.binding
        val binding = data[name]
                ?: throw IllegalStateException("Binding $name not present in current context")

        when (binding.type) {
            KGTypeTag.INT -> mainMethodWriter.visitVarInsn(ILOAD, binding.index)
            KGTypeTag.DEC -> mainMethodWriter.visitVarInsn(DLOAD, binding.index)
            KGTypeTag.BOOL -> mainMethodWriter.visitVarInsn(ILOAD, binding.index)
            KGTypeTag.STRING -> mainMethodWriter.visitVarInsn(ALOAD, binding.index)
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
        mainMethodWriter.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        print.expr.accept(this, data)

        val type = jvmTypeDescriptorForType(print.expr.type)
        val printlnSignature = "($type)V"

        mainMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", printlnSignature, false)
    }

    private fun getIndexForNextLocalVariable(data: LinkedMap<String, CodeGenBinding>, startIndex: Int = 0): Int {
        return if (data.size == 0) {
            startIndex
        } else {
            data.size + data[data.lastKey()]!!.size + startIndex
        }
    }

    override fun visitValDeclaration(valDecl: KGTree.KGValDeclaration, data: LinkedMap<String, CodeGenBinding>) {
        val valName = valDecl.identifier
        if (data.containsKey(valName)) {
            throw IllegalStateException("Cannot declare duplicate binding $valName")
        }

        val valDeclExpr = valDecl.expression
        val typeDesc = jvmTypeDescriptorForType(valDeclExpr.type)

        // Everything we're doing RIGHT NOW is wrapped in the main method of the generated class,
        // and since the main method receives args, the 0th local variable is args. So any additional
        // local variables we declare must account for that. This will change in the future.
        val bindingIndex = getIndexForNextLocalVariable(data, startIndex = 1) //data.size + 1
        data.put(valName, CodeGenBinding(valName, valDeclExpr.type, bindingIndex))

        mainMethodWriter.visitLocalVariable(valName, typeDesc, null, mainMethodStart, mainMethodEnd, bindingIndex)
        valDeclExpr.accept(this, data)
        when (valDeclExpr.type) {
            KGTypeTag.INT -> mainMethodWriter.visitVarInsn(ISTORE, bindingIndex)
            KGTypeTag.DEC -> mainMethodWriter.visitVarInsn(DSTORE, bindingIndex)
            KGTypeTag.BOOL -> mainMethodWriter.visitVarInsn(ISTORE, bindingIndex)
            KGTypeTag.STRING -> mainMethodWriter.visitVarInsn(ASTORE, bindingIndex)
            else -> throw IllegalStateException("Cannot store variable of type ${valDeclExpr.type} in binding $valName")
        }
    }
}