package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.iface.base.Tree
import co.kenrg.kagelang.tree.types.KGTypeTag
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.Label
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes.*

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class CodeGenVisitor(
        className: String = "MyClass"
) : KGTree.Visitor<Map<String, KGTypeTag>> {
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

    fun resultBytes(): ByteArray? {
        mainMethodWriter.visitLabel(mainMethodEnd)
        mainMethodWriter.visitInsn(RETURN)
        mainMethodWriter.visitEnd()
        mainMethodWriter.visitMaxs(-1, -1)
        cw.visitEnd()
        return cw.toByteArray()
    }

    override fun visitLiteral(literal: KGTree.KGLiteral, data: Map<String, KGTypeTag>) {
        when (literal.typeTag) {
            KGTypeTag.INT -> mainMethodWriter.visitLdcInsn(literal.value as java.lang.Integer)
            KGTypeTag.DEC -> mainMethodWriter.visitLdcInsn(literal.value as java.lang.Double)
            KGTypeTag.BOOL -> mainMethodWriter.visitLdcInsn(literal.value as java.lang.Boolean)
            else -> throw UnsupportedOperationException("Literal $literal is not a literal")
        }
    }

    override fun visitUnary(unary: KGTree.KGUnary, data: Map<String, KGTypeTag>) {
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

    fun pushCondAnd(left: KGTree, right: KGTree, mainMethodWriter: MethodVisitor, data: Map<String, KGTypeTag>) {
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

    fun pushCondOr(left: KGTree, right: KGTree, mainMethodWriter: MethodVisitor, data: Map<String, KGTypeTag>) {
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

    fun pushArithmeticalOperands(binary: KGTree.KGBinary, mainMethodWriter: MethodVisitor, data: Map<String, KGTypeTag>) {
        binary.left.accept(this, data)
        if (binary.left.type == KGTypeTag.INT && binary.type == KGTypeTag.DEC) {
            mainMethodWriter.visitInsn(I2D)
        }

        binary.right.accept(this, data)
        if (binary.right.type == KGTypeTag.INT && binary.type == KGTypeTag.DEC) {
            mainMethodWriter.visitInsn(I2D)
        }
    }

    override fun visitBinary(binary: KGTree.KGBinary, data: Map<String, KGTypeTag>) {
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

    override fun visitParenthesized(parenthesized: KGTree.KGParenthesized, data: Map<String, KGTypeTag>) {
        parenthesized.expr.accept(this, data)
    }

    override fun visitPrint(print: KGTree.KGPrint, data: Map<String, KGTypeTag>) {
        mainMethodWriter.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        print.expr.accept(this, data)

        val type = when (print.expr.type) {
            KGTypeTag.INT -> "I"
            KGTypeTag.DEC -> "D"
            KGTypeTag.BOOL -> "Z"
            else -> throw IllegalStateException("Primitive type ${print.expr.type} not supported")
        }
        val printlnSignature = "($type)V"

        mainMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", printlnSignature, false)
    }
}