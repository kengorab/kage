package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.model.Signature
import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.iface.base.Tree
import co.kenrg.kagelang.tree.types.KGTypeTag
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.Label
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes.*

/**
 * An implementor of the Visitor interface, this visits each node in the tree
 * and generates JVM bytecode. In the first pass of this implementation, everything
 * is contained in the "main" method of the class that gets generated (which, by default
 * is called MyClass).
 *
 * In each of the visitor methods, the `data` parameter contains the currently visible scope.
 * When visiting a function declaration statement, for example, the `data` passed to child tree(s)
 * of that statement will be the function's scope.
 *
 * @see KGTree.Visitor
 */
@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class CodeGenVisitor(
        val className: String = "MyClass"
) : KGTree.Visitor<CGScope> {
    val cw: ClassWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)

    // <clinit> method writer. Static val initializations need to be done in this method.
    val clinitWriter: MethodVisitor = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null)

    init {
        cw.visit(V1_8, ACC_PUBLIC, className, null, "java/lang/Object", null)

        // Initialize <clinit> method writer for class. This method writer will be used to initialize all the static
        // values for the class (namespace).
        clinitWriter.visitCode()
    }

    override fun visitTopLevel(file: KGFile, data: CGScope) {
        data.method = FocusedMethod(clinitWriter, null, null)
        file.statements.forEach { it.accept(this, data) }
    }

    /**
     * After calling accept on a <code>Tree</code> with this visitor, call this method to retrieve
     * the bytecode representation of that tree.
     *
     * @see Tree
     * @see KGTree.Visitor
     */
    fun resultBytes(): ByteArray? {
        clinitWriter.visitInsn(RETURN)
        clinitWriter.visitMaxs(-1, -1)
        clinitWriter.visitEnd()

        cw.visitEnd()
        return cw.toByteArray()
    }

    /*****************************
     *
     *    Expression Visitors
     *
     *****************************/

    override fun visitLiteral(literal: KGTree.KGLiteral, data: CGScope) {
        val methodWriter = data.method?.writer
                ?: throw IllegalStateException("Attempted to visit literal with no methodVisitor active")

        when (literal.typeTag) {
            KGTypeTag.INT -> methodWriter.visitLdcInsn(literal.value as java.lang.Integer)
            KGTypeTag.DEC -> methodWriter.visitLdcInsn(literal.value as java.lang.Double)
            KGTypeTag.BOOL -> methodWriter.visitLdcInsn(literal.value as java.lang.Boolean)
            KGTypeTag.STRING -> methodWriter.visitLdcInsn(literal.value as java.lang.String)
            else -> throw UnsupportedOperationException("Literal $literal is not a literal")
        }
    }

    override fun visitUnary(unary: KGTree.KGUnary, data: CGScope) {
        val methodWriter = data.method?.writer
                ?: throw IllegalStateException("Attempted to visit unary with no methodVisitor active")

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

    fun pushCondAnd(left: KGTree, right: KGTree, methodWriter: MethodVisitor, data: CGScope) {
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

    fun pushCondOr(left: KGTree, right: KGTree, methodWriter: MethodVisitor, data: CGScope) {
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

    fun pushArithmeticalOperands(binary: KGTree.KGBinary, methodWriter: MethodVisitor, data: CGScope) {
        binary.left.accept(this, data)
        if (binary.left.type == KGTypeTag.INT && binary.type == KGTypeTag.DEC) {
            methodWriter.visitInsn(I2D)
        }

        binary.right.accept(this, data)
        if (binary.right.type == KGTypeTag.INT && binary.type == KGTypeTag.DEC) {
            methodWriter.visitInsn(I2D)
        }
    }

    fun appendStrings(tree: KGTree, methodWriter: MethodVisitor, data: CGScope) {
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

    fun pushStringConcatenation(binary: KGTree.KGBinary, methodWriter: MethodVisitor, data: CGScope) {
        // Create and initialize a StringBuilder, leaving a reference on TOS.
        methodWriter.visitTypeInsn(NEW, "java/lang/StringBuilder")
        methodWriter.visitInsn(DUP)
        methodWriter.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)

        // Traverse the tree, appending to the StringBuilder.
        appendStrings(binary, methodWriter, data)

        // After the concatenation traversal is complete, build the String and place on TOS.
        methodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
    }

    override fun visitBinary(binary: KGTree.KGBinary, data: CGScope) {
        val methodWriter = data.method?.writer
                ?: throw IllegalStateException("Attempted to visit binary with no methodVisitor active")

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

    override fun visitParenthesized(parenthesized: KGTree.KGParenthesized, data: CGScope) {
        parenthesized.expr.accept(this, data)
    }

    override fun visitBindingReference(bindingReference: KGTree.KGBindingReference, data: CGScope) {
        val methodWriter = data.method?.writer
                ?: throw IllegalStateException("Attempted to visit binding-reference with no methodVisitor active")

        val name = bindingReference.binding
        val binding = data.getVal(name)
                ?: throw IllegalStateException("Binding $name not present in current context")

        when (binding) {
            is StaticValBinding ->
                methodWriter.visitFieldInsn(GETSTATIC, className, binding.name, jvmTypeDescriptorForType(binding.type))
            else -> throw UnsupportedOperationException("Cannot reference binding: $name, $binding")
        }
    }

    override fun visitInvocation(invocation: KGTree.KGInvocation, data: CGScope) {
        when (invocation.invokee) {
            is KGTree.KGBindingReference -> {
                val target = data.getFn(invocation.invokee.binding)
                if (target == null) {
                    throw IllegalStateException("Binding with name ${invocation.invokee.binding} not visible in current context")
                } else {
                    when (target) {
                        is FunctionBinding -> {
                            val methodWriter = data.method?.writer
                                    ?: throw IllegalStateException("Attempted to visit invocation with no methodVisitor active")
                            val signature = jvmTypeDescriptorForSignature(target.signature)
                            methodWriter.visitMethodInsn(INVOKESTATIC, className, target.name, signature, false)
                        }
                        else ->
                            throw IllegalStateException("Expression is not invokable")
                    }
                }
            }
            else ->
                throw IllegalStateException("Expression is not invokable")
        }
    }

    override fun visitLetIn(letIn: KGTree.KGLetIn, data: CGScope) {
        throw UnsupportedOperationException("not implemented")
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
        KGTypeTag.UNIT -> "V"
        else -> throw IllegalStateException("JVM descriptor for type $type not yet implemented")
    }

    private fun jvmTypeDescriptorForSignature(signature: Signature): String {
        return "()${jvmTypeDescriptorForType(signature.returnType)}"
    }

    override fun visitPrint(print: KGTree.KGPrint, data: CGScope) {
        val methodWriter = data.method?.writer
                ?: throw IllegalStateException("Attempted to visit print with no methodVisitor active")

        methodWriter.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        print.expr.accept(this, data)

        val type = jvmTypeDescriptorForType(print.expr.type)
        val printlnSignature = "($type)V"

        methodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", printlnSignature, false)
    }

    // TODO - Use this function when scoped bindings are a thing
//    private fun getIndexForNextLocalVariable(data: Namespace, startIndex: Int = 0): Int {
//        return if (data.size == 0) {
//            startIndex
//        } else {
//            val previousLocalVariable = data[data.lastKey()]!!
//            val previousLocalVarSize = when (previousLocalVariable) {
//                is CodeGenBinding.LocalValBinding -> previousLocalVariable.size
//                else -> throw UnsupportedOperationException("Need to come back and fix up local variable resolution...currently cannot declare vals after declaring functions...")
//            }
//            data.size + previousLocalVarSize + startIndex
//        }
//    }

    override fun visitValDeclaration(valDecl: KGTree.KGValDeclaration, data: CGScope) {
        val valName = valDecl.identifier
        if (data.vals.containsKey(valName)) {
            throw IllegalStateException("Cannot declare duplicate binding $valName")
            // TODO - Check for duplicated names up the parent scopes and warn if shadowing?
        }

        val valDeclExpr = valDecl.expression
        val typeDesc = jvmTypeDescriptorForType(valDeclExpr.type)

        if (data.isRoot()) {
            // If we're at the root scope, declare val as static val in the class
            cw.visitField(ACC_PUBLIC or ACC_STATIC, valName, typeDesc, null, null)
            valDeclExpr.accept(this, data)
            clinitWriter.visitFieldInsn(PUTSTATIC, className, valName, typeDesc)

            data.vals.put(valName, StaticValBinding(valName, valDeclExpr.type))
        } else {
            throw UnsupportedOperationException("Non-root val declaration not yet implemented")
            // The commented-out block below may be useful in implementing bound val declarations
//        val bindingIndex = getIndexForNextLocalVariable(data, startIndex = 1)
//        focusedMethod = prevMethodWriter
//
//        methodWriter.visitLocalVariable(valName, typeDesc, null, methodStart, methodEnd, bindingIndex)
//        valDeclExpr.accept(this, data)
//        when (valDeclExpr.type) {
//            KGTypeTag.INT -> methodWriter.visitVarInsn(ISTORE, bindingIndex)
//            KGTypeTag.DEC -> methodWriter.visitVarInsn(DSTORE, bindingIndex)
//            KGTypeTag.BOOL -> methodWriter.visitVarInsn(ISTORE, bindingIndex)
//            KGTypeTag.STRING -> methodWriter.visitVarInsn(ASTORE, bindingIndex)
//            else -> throw IllegalStateException("Cannot store variable of type ${valDeclExpr.type} in binding $valName")
//        }
        }
    }

    override fun visitFnDeclaration(fnDecl: KGTree.KGFnDeclaration, data: CGScope) {
        val isMain = fnDecl.name == "main"

        val signature = if (isMain) "([Ljava/lang/String;)V" else jvmTypeDescriptorForSignature(fnDecl.signature)
        val fnWriter = cw.visitMethod(ACC_PUBLIC or ACC_STATIC, fnDecl.name, signature, null, null)
        val fnStart = Label()
        val fnEnd = Label()
        fnWriter.visitCode()
        fnWriter.visitLabel(fnStart)

        val fnScope = CGScope(parent = data, method = FocusedMethod(fnWriter, fnStart, fnEnd))
        fnDecl.body.accept(this, fnScope)

        if (isMain) {
            if (fnDecl.body.type != KGTypeTag.UNIT) {
                throw IllegalStateException("Main method requires return type of Unit, actual: ${fnDecl.body.type}")
            }

            fnWriter.visitInsn(RETURN)
        } else {
            when (fnDecl.signature.returnType) {
                KGTypeTag.INT -> fnWriter.visitInsn(IRETURN)
                KGTypeTag.DEC -> fnWriter.visitInsn(DRETURN)
                KGTypeTag.BOOL -> fnWriter.visitInsn(IRETURN)
                KGTypeTag.STRING -> fnWriter.visitInsn(ARETURN)
                else -> throw UnsupportedOperationException("Cannot perform return for type ${fnDecl.signature.returnType}")
            }
        }

        fnWriter.visitMaxs(-1, -1)
        fnWriter.visitEnd()

        data.functions.put(fnDecl.name, FunctionBinding(fnDecl.name, fnDecl.signature))

        fnWriter.visitLabel(fnEnd)
    }
}