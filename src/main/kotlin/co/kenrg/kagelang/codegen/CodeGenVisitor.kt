package co.kenrg.kagelang.codegen

import co.kenrg.kagelang.ext.pairwiseEq
import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.iface.base.Tree
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.tree.types.StdLibType
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
    val innerClasses = arrayListOf<Pair<String, ByteArray?>>()

    val cw: ClassWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)

    // <clinit> method writer. Static val initializations need to be done in this method.
    val clinitWriter: MethodVisitor = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null)

    init {
        cw.visit(V1_6, ACC_PUBLIC, className, null, "java/lang/Object", null)

        // Initialize <clinit> method writer for class. This method writer will be used to initialize all the static
        // values for the class (namespace).
        clinitWriter.visitCode()
    }

    override fun visitTopLevel(file: KGFile, data: CGScope) {
        // At this point, `data` should be the root scope. Since the creation of the clinit method writer needs to
        // wait until the init method of the CodeGenVisitor, we assign the `method` property to the root scope now.
        if (!data.isRoot())
            throw IllegalStateException("Visiting top level with non-root scope")

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
    fun results(): List<Pair<String, ByteArray?>> {
        clinitWriter.visitInsn(RETURN)
        clinitWriter.visitMaxs(-1, -1)
        clinitWriter.visitEnd()

        cw.visitEnd()
        return listOf(Pair(className, cw.toByteArray())) + innerClasses
    }

    private fun getTypeAssertNotNull(type: KGType?) =
            type ?: throw IllegalStateException("Cannot run codegen on subtree with null type; was the tree successfully run through TypeCheckerAttributorVisitor?")

    /*****************************
     *
     *    Expression Visitors
     *
     *****************************/

    override fun visitLiteral(literal: KGTree.KGLiteral, data: CGScope) {
        val methodWriter = data.method?.writer
                ?: throw IllegalStateException("Attempted to visit literal with no methodVisitor active")

        when (literal.type) {
            KGType.INT -> methodWriter.visitLdcInsn(literal.value as java.lang.Integer)
            KGType.DEC -> methodWriter.visitLdcInsn(literal.value as java.lang.Double)
            KGType.BOOL -> methodWriter.visitLdcInsn(literal.value as java.lang.Boolean)
            KGType.STRING -> methodWriter.visitLdcInsn(literal.value as java.lang.String)
            else -> throw UnsupportedOperationException("Literal $literal is not a literal")
        }
    }

    override fun visitUnary(unary: KGTree.KGUnary, data: CGScope) {
        val methodWriter = data.method?.writer
                ?: throw IllegalStateException("Attempted to visit unary with no methodVisitor active")

        if (unary.type == null)
            throw IllegalStateException("Cannot run codegen on Unary subtree with UNSET type; was the tree successfully run through TypeCheckerAttributorVisitor?")

        when (unary.kind()) {
            is Tree.Kind.ArithmeticNegation -> {
                unary.expr.accept(this, data)
                val unaryExprType = getTypeAssertNotNull(unary.expr.type)
                when (unaryExprType) {
                    KGType.INT -> methodWriter.visitInsn(INEG)
                    KGType.DEC -> methodWriter.visitInsn(DNEG)
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

    fun pushAndCastNumericOperands(binary: KGTree.KGBinary, shouldCast: Boolean, methodWriter: MethodVisitor, data: CGScope) {
        binary.left.accept(this, data)
        if (binary.left.type == KGType.INT && shouldCast) {
            methodWriter.visitInsn(I2D)
        }

        binary.right.accept(this, data)
        if (binary.right.type == KGType.INT && shouldCast) {
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
        val treeType = getTypeAssertNotNull(tree.type)
        val jvmDesc = treeType.jvmDescriptor()
        methodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "($jvmDesc)Ljava/lang/StringBuilder;", false)
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

    fun pushIntegerComparison(kind: Tree.Kind<*>, binary: KGTree.KGBinary, data: CGScope, methodWriter: MethodVisitor) {
        val trueLbl = Label()
        val endLbl = Label()
        binary.left.accept(this, data)
        binary.right.accept(this, data)

        when (kind) {
            is Tree.Kind.GreaterThan -> methodWriter.visitJumpInsn(IF_ICMPGT, trueLbl)
            is Tree.Kind.LessThan -> methodWriter.visitJumpInsn(IF_ICMPLT, trueLbl)
            is Tree.Kind.GreaterThanOrEqualTo -> methodWriter.visitJumpInsn(IF_ICMPGE, trueLbl)
            is Tree.Kind.LessThanOrEqualTo -> methodWriter.visitJumpInsn(IF_ICMPLE, trueLbl)
            is Tree.Kind.Equals -> methodWriter.visitJumpInsn(IF_ICMPEQ, trueLbl)
            is Tree.Kind.NotEquals -> methodWriter.visitJumpInsn(IF_ICMPNE, trueLbl)
        }

        methodWriter.visitInsn(ICONST_0)
        methodWriter.visitJumpInsn(GOTO, endLbl)

        methodWriter.visitLabel(trueLbl)
        methodWriter.visitInsn(ICONST_1)

        methodWriter.visitLabel(endLbl)
    }

    fun pushDecimalComparison(kind: Tree.Kind<*>, methodWriter: MethodVisitor) {
        val trueLbl = Label()
        val endLbl = Label()

        when (kind) {
            is Tree.Kind.GreaterThan -> {
                methodWriter.visitInsn(DCMPG)
                methodWriter.visitJumpInsn(IFGT, trueLbl)
            }
            is Tree.Kind.LessThan -> {
                methodWriter.visitInsn(DCMPL)
                methodWriter.visitJumpInsn(IFLT, trueLbl)
            }
            is Tree.Kind.GreaterThanOrEqualTo -> {
                methodWriter.visitInsn(DCMPG)
                methodWriter.visitJumpInsn(IFGE, trueLbl)
            }
            is Tree.Kind.LessThanOrEqualTo -> {
                methodWriter.visitInsn(DCMPL)
                methodWriter.visitJumpInsn(IFLE, trueLbl)
            }
            is Tree.Kind.Equals -> {
                methodWriter.visitInsn(DCMPL)
                methodWriter.visitJumpInsn(IFEQ, trueLbl)
            }
            is Tree.Kind.NotEquals -> {
                methodWriter.visitInsn(DCMPL)
                methodWriter.visitJumpInsn(IFNE, trueLbl)
            }
        }

        methodWriter.visitInsn(ICONST_0)
        methodWriter.visitJumpInsn(GOTO, endLbl)

        methodWriter.visitLabel(trueLbl)
        methodWriter.visitInsn(ICONST_1)

        methodWriter.visitLabel(endLbl)
    }

    fun pushComparableComparison(kind: Tree.Kind<*>, binary: KGTree.KGBinary, data: CGScope, methodWriter: MethodVisitor) {
        binary.left.accept(this, data)
        binary.right.accept(this, data)

        // Unused rightType kept for symmetry
        val leftType = getTypeAssertNotNull(binary.left.type)
        val rightType = getTypeAssertNotNull(binary.right.type)

        // TODO - During typechecking, we need to verify that the two types here both implement Comparable.
        val typeDesc = leftType.jvmDescriptor()
        val type = leftType.className

        val trueLbl = Label()
        val endLbl = Label()

        when (kind) {
            is Tree.Kind.GreaterThan,
            is Tree.Kind.LessThan,
            is Tree.Kind.GreaterThanOrEqualTo,
            is Tree.Kind.LessThanOrEqualTo ->
                methodWriter.visitMethodInsn(INVOKEVIRTUAL, type, "compareTo", "($typeDesc)I", false)

            is Tree.Kind.Equals -> {
                // If the comparison is an eq (==), call the `equals` method and return early
                methodWriter.visitMethodInsn(INVOKEVIRTUAL, type, "equals", "(Ljava/lang/Object;)Z", false)
                return
            }
            is Tree.Kind.NotEquals -> {
                // If the comparison is a neq (!=), call the `equals` method. If `equals` returns 0 (false), negate
                // by jumping to the true label.
                methodWriter.visitMethodInsn(INVOKEVIRTUAL, type, "equals", "(Ljava/lang/Object;)Z", false)
                methodWriter.visitJumpInsn(IFEQ, trueLbl)
            }
        }

        when (kind) {
            is Tree.Kind.GreaterThan -> methodWriter.visitJumpInsn(IFGT, trueLbl)
            is Tree.Kind.LessThan -> methodWriter.visitJumpInsn(IFLT, trueLbl)
            is Tree.Kind.GreaterThanOrEqualTo -> methodWriter.visitJumpInsn(IFGE, trueLbl)
            is Tree.Kind.LessThanOrEqualTo -> methodWriter.visitJumpInsn(IFLE, trueLbl)
        }

        methodWriter.visitInsn(ICONST_0)
        methodWriter.visitJumpInsn(GOTO, endLbl)

        methodWriter.visitLabel(trueLbl)
        methodWriter.visitInsn(ICONST_1)

        methodWriter.visitLabel(endLbl)
    }

    fun pushComparison(kind: Tree.Kind<*>, binary: KGTree.KGBinary, data: CGScope, methodWriter: MethodVisitor) {
        // TODO - Consolidate these 3 approaches to comparisons (integers, decimals, and Comparables (Strings))?

        val leftType = getTypeAssertNotNull(binary.left.type)
        val rightType = getTypeAssertNotNull(binary.right.type)

        if (leftType.isNumeric && rightType.isNumeric) {
            if (binary.left.type == KGType.INT && binary.right.type == KGType.INT) {
                pushIntegerComparison(kind, binary, data, methodWriter)
            } else {
                val shouldCast = listOf(binary.left.type, binary.right.type).pairwiseEq(KGType.INT, KGType.DEC)
                pushAndCastNumericOperands(binary, shouldCast, methodWriter, data)
                pushDecimalComparison(kind, methodWriter)
            }
        } else if (binary.left.type == binary.right.type) {
            pushComparableComparison(kind, binary, data, methodWriter)
        } else {
            throw IllegalStateException("Cannot compare types ${binary.left.type} and ${binary.right.type}")
        }
    }

    override fun visitBinary(binary: KGTree.KGBinary, data: CGScope) {
        val methodWriter = data.method?.writer
                ?: throw IllegalStateException("Attempted to visit binary with no methodVisitor active")

        val binaryType = getTypeAssertNotNull(binary.type)

        when (binary.kind()) {
            is Tree.Kind.Plus -> {
                pushAndCastNumericOperands(binary, binaryType == KGType.DEC, methodWriter, data)
                when (binaryType) {
                    KGType.INT -> methodWriter.visitInsn(IADD)
                    KGType.DEC -> methodWriter.visitInsn(DADD)
                    else -> throw IllegalStateException("Binary's kind is Plus, but type is non-numeric")
                }
            }
            is Tree.Kind.Minus -> {
                pushAndCastNumericOperands(binary, binaryType == KGType.DEC, methodWriter, data)
                when (binaryType) {
                    KGType.INT -> methodWriter.visitInsn(ISUB)
                    KGType.DEC -> methodWriter.visitInsn(DSUB)
                    else -> throw IllegalStateException("Binary's kind is Minus, but type is non-numeric")
                }
            }
            is Tree.Kind.Multiply -> {
                pushAndCastNumericOperands(binary, binaryType == KGType.DEC, methodWriter, data)
                when (binaryType) {
                    KGType.INT -> methodWriter.visitInsn(IMUL)
                    KGType.DEC -> methodWriter.visitInsn(DMUL)
                    else -> throw IllegalStateException("Binary's kind is Multiply, but type is non-numeric")
                }
            }
            is Tree.Kind.Divide -> {
                pushAndCastNumericOperands(binary, binaryType == KGType.DEC, methodWriter, data)
                when (binaryType) {
                    KGType.INT -> methodWriter.visitInsn(IDIV)
                    KGType.DEC -> methodWriter.visitInsn(DDIV)
                    else -> throw IllegalStateException("Binary's kind is Divide, but type is non-numeric")
                }
            }
            is Tree.Kind.ConditionalAnd -> {
                when (binaryType) {
                    KGType.BOOL -> pushCondAnd(binary.left, binary.right, methodWriter, data)
                    else -> throw IllegalStateException("Binary's kind is ConditionalAnd, but type is non-boolean")
                }
            }
            is Tree.Kind.ConditionalOr -> {
                when (binaryType) {
                    KGType.BOOL -> pushCondOr(binary.left, binary.right, methodWriter, data)
                    else -> throw IllegalStateException("Binary's kind is ConditionalOr, but type is non-boolean")
                }
            }
            is Tree.Kind.Concatenation -> {
                when (binaryType) {
                    KGType.STRING -> pushStringConcatenation(binary, methodWriter, data)
                    else -> throw IllegalStateException("Binary's kind is Concatenation, but type is non-String")
                }
            }
            is Tree.Kind.GreaterThan,
            is Tree.Kind.LessThan,
            is Tree.Kind.GreaterThanOrEqualTo,
            is Tree.Kind.LessThanOrEqualTo,
            is Tree.Kind.Equals,
            is Tree.Kind.NotEquals -> {
                pushComparison(binary.kind(), binary, data, methodWriter)
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
            is ValBinding.Static ->
                methodWriter.visitFieldInsn(GETSTATIC, className, binding.name, binding.type.jvmDescriptor())
            is ValBinding.Local -> when (binding.type) {
                KGType.INT -> methodWriter.visitVarInsn(ILOAD, binding.index)
                KGType.DEC -> methodWriter.visitVarInsn(DLOAD, binding.index)
                KGType.BOOL -> methodWriter.visitVarInsn(ILOAD, binding.index)
                KGType.STRING -> methodWriter.visitVarInsn(ALOAD, binding.index)
                KGType.UNIT -> throw IllegalStateException("Cannot resolve binding $name of type Unit")
                else -> methodWriter.visitVarInsn(ALOAD, binding.index)
            }
        }
    }

    override fun visitInvocation(invocation: KGTree.KGInvocation, data: CGScope) {
        when (invocation.invokee) {
            is KGTree.KGBindingReference -> {
                val methodWriter = data.method?.writer
                        ?: throw IllegalStateException("Attempted to visit invocation with no methodVisitor active")

                fun invokeConstructor(type: KGType) {
                    methodWriter.visitTypeInsn(NEW, type.className)
                    methodWriter.visitInsn(DUP)

                    invocation.params.forEach {
                        it.accept(this, data)
                        val paramType = it.type
                                ?: throw IllegalStateException("Type of invocation arguments cannot be null")
                        if (type.isGeneric && paramType.isPrimitive) {
                            val signature = "(${paramType.jvmDescriptor()})L${paramType.className};"
                            methodWriter.visitMethodInsn(INVOKESTATIC, paramType.className, "valueOf", signature, false)
                        }
                    }

                    val paramsSignature = type.props.values.map {
                        if (it.isGeneric) "Ljava/lang/Object;"
                        else it.type.jvmDescriptor()
                    }
                    val constructorSignature = "(${paramsSignature.joinToString("")})V"
                    methodWriter.visitMethodInsn(INVOKESPECIAL, type.className, "<init>", constructorSignature, false)
                }

                // If the invocation target is a type name, invoke the constructor for that type.
                // Otherwise, if the target is a built-in standard type, invoke that constructor;
                // otherwise, invoke the function with the target's name.
                val typeForName = data.getType(invocation.invokee.binding)
                val stdLibTypeForName = StdLibType.values().find { it.name == invocation.invokee.binding }
                if (typeForName != null) {
                    invokeConstructor(typeForName)
                } else if (stdLibTypeForName != null) {
                    invokeConstructor(KGType.stdLibType(stdLibTypeForName, listOf()))
                } else {
                    // Grab the first function. At this point, it's already passed typechecking so there
                    // should definitely be a function matching the necessary param signature.
                    val fnForName = data.getFnsForName(invocation.invokee.binding)
                            ?.filter {
                                val invocationParamTypes = invocation.params.map { it.type }
                                val signatureParamTypes = it.signature.params.map { it.second }

                                invocationParamTypes.size == signatureParamTypes.size &&
                                        invocationParamTypes.zip(signatureParamTypes).fold(true) { acc, pair ->
                                            acc && pair.first?.isAssignableToType(pair.second) ?: false
                                        }
                            }
                            ?.elementAtOrNull(0)
                            ?: throw IllegalStateException("No function available with name ${invocation.invokee.binding} accepting ${invocation.params.map { it.type }}")

                    invocation.params.forEach {
                        it.accept(this, data)
                    }

                    val signature = fnForName.signature.jvmTypeDescriptor()
                    methodWriter.visitMethodInsn(INVOKESTATIC, className, fnForName.name, signature, false)
                }
            }
            else -> throw IllegalStateException("Expression is not invokable")
        }
    }

    override fun visitLetIn(letIn: KGTree.KGLetIn, data: CGScope) {
        letIn.statements.forEach { it.accept(this, data) }
        letIn.body.accept(this, data)
    }

    override fun visitIfThenElse(ifElse: KGTree.KGIfThenElse, data: CGScope) {
        val thenBranchLbl = Label()
        val elseBranchLbl = Label()
        val endLbl = Label()

        val methodWriter = data.method?.writer
                ?: throw IllegalStateException("Attempted to visit ifThenElse with no methodVisitor active")

        val hasElse = ifElse.elseBody != null

        // Evaluate the condition, leaving either a 0 (false) or 1 (true) on TOS
        ifElse.condition.accept(this, data)

        // If TOS == 0 (false), jump to the else branch (if the elseBody exists) or the end
        val condFalseLbl = if (hasElse) elseBranchLbl else endLbl
        methodWriter.visitJumpInsn(IFEQ, condFalseLbl)

        // The then branch of the if-expression. Right now, any bindings defined within branches will be stored w.r.t.
        // the outer scope. Meaning, they will be accessible within the else branch.
        methodWriter.visitLabel(thenBranchLbl)

        val thenScope = data.createChildScope(vals = data.vals.clone(), method = data.method)
        ifElse.thenBody.accept(this, thenScope)
        methodWriter.visitJumpInsn(GOTO, endLbl)

        // The else branch of the if-expression (only perform codegen if else branch exists for if-expression).
        // Create a new stack frame here, for the else branch. As of java 6, it's required for bytecode to explicitly
        // specify the local values as well as the values on the stack, when doing bytecode level jump instructions.
        if (hasElse) {
            methodWriter.visitLabel(elseBranchLbl)

            // TODO - Pass count (and types) of local variables here, instead of 0 and null
            methodWriter.visitFrame(F_SAME, 0, null, 0, null)

            val elseScope = data.createChildScope(vals = data.vals.clone(), method = data.method)
            ifElse.elseBody!!.accept(this, elseScope)
        }

        methodWriter.visitLabel(endLbl)
        // Visit frame after the if expression has ended
        methodWriter.visitFrame(F_SAME, 0, null, 0, null)
    }

    override fun visitDot(dot: KGTree.KGDot, data: CGScope) {
        val methodWriter = data.method?.writer
                ?: throw IllegalStateException("Attempted to visit dotExpression with no methodVisitor active")

        dot.target.accept(this, data)
        val dotType = getTypeAssertNotNull(dot.target.type)

        if (dotType.props.containsKey(dot.prop)) {
            val propType = dotType.props[dot.prop]!!

            val accessorName = "get${dot.prop.capitalize()}"
            if (propType.isGeneric) {
                val accessorSignature = "()Ljava/lang/Object;"
                methodWriter.visitMethodInsn(INVOKEVIRTUAL, dotType.className, accessorName, accessorSignature, false)
                methodWriter.visitTypeInsn(CHECKCAST, propType.type.className)

                // If the type was a boxed primitive, unbox it.
                if (propType.type.isPrimitive) {
                    val unboxDesc = "(L${propType.type.className};)${propType.type.jvmDescriptor()}"
                    methodWriter.visitMethodInsn(INVOKESTATIC, "kage/lang/util/Primitives", "unbox", unboxDesc, false)
                }
            } else {
                val accessorSignature = "()${propType.type.jvmDescriptor()}"
                methodWriter.visitMethodInsn(INVOKEVIRTUAL, dotType.className, accessorName, accessorSignature, false)
            }
        } else {
            throw IllegalStateException("Prop ${dot.prop} is not available on target ${dotType.className}")
        }
    }

    override fun visitIndex(index: KGTree.KGIndex, data: CGScope) {
        val methodWriter = data.method?.writer
                ?: throw IllegalStateException("Attempted to visit indexExpression with no methodVisitor active")

        index.target.accept(this, data)
        val targetType = getTypeAssertNotNull(index.target.type)

        index.index.accept(this, data)
        val indexType = getTypeAssertNotNull(index.index.type)

        val indexTypeClass = indexType.className
        if (indexType.isPrimitive) {
            val valueOfSignature = "(${indexType.jvmDescriptor()})L$indexTypeClass;"
            methodWriter.visitMethodInsn(INVOKESTATIC, indexTypeClass, "valueOf", valueOfSignature, false)
        }

        val getAtDesc = "(Ljava/lang/Object;)L${StdLibType.Maybe.className};"
        methodWriter.visitMethodInsn(INVOKEVIRTUAL, targetType.className, "getAt", getAtDesc, false)
    }

    override fun visitTuple(tuple: KGTree.KGTuple, data: CGScope) {
        if (tuple.items.size > 6)
            throw IllegalStateException("Tuples larger than 6 items not supported; you should consider creating a type instead")

        val methodWriter = data.method?.writer
                ?: throw IllegalStateException("Attempted to visit tuple with no methodVisitor active")


        val tupleKind = when (tuple.items.size) {
            2 -> StdLibType.Pair
            3 -> StdLibType.Triple
            4 -> StdLibType.Tuple4
            5 -> StdLibType.Tuple5
            6 -> StdLibType.Tuple6
            else -> throw UnsupportedOperationException("Tuples larger than 6 items not supported; you should consider creating a type instead")
        }
        methodWriter.visitTypeInsn(NEW, tupleKind.className)
        methodWriter.visitInsn(DUP)

        tuple.items.forEach {
            it.accept(this, data)
            // Since INT, DEC, and BOOL are primitives, we need to convert them into objects
            when (it.type!!) {
                KGType.INT -> methodWriter.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
                KGType.DEC -> methodWriter.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false)
                KGType.BOOL -> methodWriter.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false)
            }
        }

        val constructorSignature = "(${tuple.items.map { "Ljava/lang/Object;" }.joinToString("")})V"
        methodWriter.visitMethodInsn(INVOKESPECIAL, tupleKind.className, "<init>", constructorSignature, false)
    }

    override fun visitArray(array: KGTree.KGArray, data: CGScope) {
        val methodWriter = data.method?.writer
                ?: throw IllegalStateException("Attempted to visit array with no methodVisitor active")

        methodWriter.visitTypeInsn(NEW, StdLibType.Array.className)
        methodWriter.visitInsn(DUP)

        val size = array.items.size
        methodWriter.visitLdcInsn(size)

        val itemType = getTypeAssertNotNull(array.type?.typeParams?.get(0)) // All array items should be the same type
        val itemTypeClass = itemType.className

        methodWriter.visitTypeInsn(ANEWARRAY, itemType.className)
        array.items.forEachIndexed { i, item ->
            methodWriter.visitInsn(DUP)
            methodWriter.visitLdcInsn(i)
            item.accept(this, data)

            if (itemType.isPrimitive) {
                val valueOfSignature = "(${itemType.jvmDescriptor()})L$itemTypeClass;"
                methodWriter.visitMethodInsn(INVOKESTATIC, itemTypeClass, "valueOf", valueOfSignature, false)
            }

            methodWriter.visitInsn(AASTORE)
        }

        val constructorSignature = "([Ljava/lang/Object;)V"
        methodWriter.visitMethodInsn(INVOKESPECIAL, StdLibType.Array.className, "<init>", constructorSignature, false)
    }

    /*****************************
     *
     *     Statement Visitors
     *
     *****************************/

    override fun visitPrint(print: KGTree.KGPrint, data: CGScope) {
        val methodWriter = data.method?.writer
                ?: throw IllegalStateException("Attempted to visit print with no methodVisitor active")

        methodWriter.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        print.expr.accept(this, data)

        val printType = getTypeAssertNotNull(print.expr.type)
        val printlnSignature = when (printType) {
            KGType.INT, KGType.DEC, KGType.BOOL, KGType.STRING -> "(${printType.jvmDescriptor()})V"
            else -> "(Ljava/lang/Object;)V"
        }

        methodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", printlnSignature, false)
    }

    private fun getIndexForNextLocalVariable(data: CGScope, startIndex: Int = 0): Int {
        return data.vals.values
                .map {
                    val localVal = it as? ValBinding.Local
                            ?: throw IllegalStateException("Expected local variable but saw Static")
                    localVal.size
                }
                .fold(startIndex, Int::plus)
    }

    override fun visitValDeclaration(valDecl: KGTree.KGValDeclaration, data: CGScope) {
        val valName = valDecl.identifier
        if (data.vals.containsKey(valName)) {
            throw IllegalStateException("Cannot declare duplicate binding $valName")
            // TODO - Check for duplicated names up the parent scopes and warn if shadowing?
        }

        val valDeclExpr = valDecl.expression

        val valDeclExprType = getTypeAssertNotNull(valDeclExpr.type)
        val jvmDesc = valDeclExprType.jvmDescriptor()

        if (data.isRoot()) {
            // If we're at the root scope, declare val as static val in the class
            cw.visitField(ACC_PUBLIC or ACC_STATIC, valName, jvmDesc, null, null)
            valDeclExpr.accept(this, data)
            clinitWriter.visitFieldInsn(PUTSTATIC, className, valName, jvmDesc)

            data.vals.put(valName, ValBinding.Static(valName, valDeclExprType))
        } else {
            val bindingIndex = getIndexForNextLocalVariable(data, startIndex = 0)

            if (data.method == null)
                throw IllegalStateException("No method writer at non-root scope")

            val writer = data.method!!.writer

            // Unnecessary, unless we want to add debug information. It also makes it more complicated to duplicate
            // binding names in adjacent inner scopes (as in if-then-else branches).
            // writer.visitLocalVariable(valName, typeDesc, null, start, end, bindingIndex)

            valDeclExpr.accept(this, data)
            when (valDeclExpr.type) {
                KGType.INT -> writer.visitVarInsn(ISTORE, bindingIndex)
                KGType.DEC -> writer.visitVarInsn(DSTORE, bindingIndex)
                KGType.BOOL -> writer.visitVarInsn(ISTORE, bindingIndex)
                KGType.STRING -> writer.visitVarInsn(ASTORE, bindingIndex)
                KGType.UNIT -> throw IllegalStateException("Cannot store variable of type Unit in binding $valName")
                else -> writer.visitVarInsn(ASTORE, bindingIndex)
            }

            // TODO - Fix indexing of local vars with many nested scopes
            data.vals.put(valName, ValBinding.Local(valName, valDeclExprType, valDeclExprType.size, bindingIndex))
        }
    }

    private fun visitMainMethod(fnDecl: KGTree.KGFnDeclaration, data: CGScope) {
        val fnWriter = cw.visitMethod(ACC_PUBLIC or ACC_STATIC, fnDecl.name, "([Ljava/lang/String;)V", null, null)
        val fnStart = Label()
        val fnEnd = Label()
        fnWriter.visitCode()
        fnWriter.visitLabel(fnStart)

        val fnScope = data.createChildScope(method = FocusedMethod(fnWriter, fnStart, fnEnd))
        val argsType = KGType.stdLibType(StdLibType.Array, listOf(KGType.STRING))

        if (fnDecl.params.isNotEmpty()) {
            // If this is the main method, and a parameter was defined, create that local variable as a Array[String],
            // using whatever name was given...
            val argsName = fnDecl.params[0].name
            fnScope.vals.put(argsName, ValBinding.Local(argsName, argsType, 1, 0))
            fnWriter.visitLocalVariable(argsName, argsType.jvmDescriptor(), null, fnStart, fnEnd, 0)

            // ...by passing the 0th local variable into the constructor for Array[String] & store it back in the 0th spot.
            // Doing it this way means that the main method doesn't need to specify an args param if it doesn't need it.
            fnWriter.visitTypeInsn(NEW, StdLibType.Array.className)
            fnWriter.visitInsn(DUP)
            fnWriter.visitVarInsn(ALOAD, 0)
            val constructorSignature = "([Ljava/lang/Object;)V"
            fnWriter.visitMethodInsn(INVOKESPECIAL, StdLibType.Array.className, "<init>", constructorSignature, false)
            fnWriter.visitVarInsn(ASTORE, 0)
        }

        fnDecl.body.accept(this, fnScope)
        if (fnDecl.body.type != KGType.UNIT)
            throw IllegalStateException("Main method requires return type of Unit, actual: ${fnDecl.body.type}")

        fnWriter.visitInsn(RETURN)
        fnWriter.visitMaxs(-1, -1)
        fnWriter.visitEnd()

        data.functions.put(fnDecl.name, FunctionBinding(fnDecl.name, fnDecl.signature))

        fnWriter.visitLabel(fnEnd)
    }

    override fun visitFnDeclaration(fnDecl: KGTree.KGFnDeclaration, data: CGScope) {
        // If fn declared at root, encode it as a static method of the class. Otherwise, it needs to be created
        // dynamically as an anonymous inner subclass of the yet-existent kageruntime.jvm.function.Function.
        if (!data.isRoot())
            throw UnsupportedOperationException("Non-top-level fns not yet implemented")

        // There are special things that should happen if this is the main method
        if (fnDecl.name == "main") {
            visitMainMethod(fnDecl, data)
            return
        }

        val fnWriter = cw.visitMethod(ACC_PUBLIC or ACC_STATIC, fnDecl.name, fnDecl.signature.jvmTypeDescriptor(), null, null)
        val fnStart = Label()
        val fnEnd = Label()
        fnWriter.visitCode()
        fnWriter.visitLabel(fnStart)

        val fnScope = data.createChildScope(method = FocusedMethod(fnWriter, fnStart, fnEnd))

        // Since all functions at the moment are static methods, their local variable index should start at 0.
        fnDecl.params.forEach {
            val index = getIndexForNextLocalVariable(fnScope, startIndex = 0)
            val paramType = it.type.hydrate(data) { throw IllegalStateException("Unknown type $it") }
                    ?: throw IllegalStateException("Unknown type ${it.name}")

            fnScope.vals.put(it.name, ValBinding.Local(it.name, paramType, paramType.size, index))
            fnWriter.visitLocalVariable(it.name, paramType.jvmDescriptor(), null, fnStart, fnEnd, index)
        }

        fnDecl.body.accept(this, fnScope)

        fnWriter.visitInsn(fnDecl.signature.returnType.getReturnInsn())
        fnWriter.visitMaxs(-1, -1)
        fnWriter.visitEnd()

        data.functions.put(fnDecl.name, FunctionBinding(fnDecl.name, fnDecl.signature))

        fnWriter.visitLabel(fnEnd)
    }

    override fun visitTypeDeclaration(typeDecl: KGTree.KGTypeDeclaration, data: CGScope) {
        val typeName = typeDecl.name
        val innerClassName = "$className\$$typeName"
        cw.visitInnerClass(innerClassName, className, typeDecl.name, ACC_PUBLIC or ACC_STATIC)

        val visitor = CodeGenVisitor(innerClassName)
        visitor.cw.visitInnerClass(innerClassName, className, typeDecl.name, ACC_PUBLIC or ACC_STATIC)

        val type = KGType(
                typeName,
                className = innerClassName,
                props = typeDecl.props
                        .map {
                            val type = it.type.hydrate(data) { throw IllegalStateException("Unknown type $it") }
                                    ?: throw IllegalStateException("Unknown type ${it.type}")
                            // TODO - Handle generic type props
                            it.name to KGType.PropType(type, false)
                        }
                        .toMap()
        )

        val typeClassWriter = TypeClassWriter(visitor, type, innerClassName)
        typeClassWriter.writeConstructor()
        typeClassWriter.writeToStringMethod()
        typeClassWriter.writeEqualsMethod()
        typeClassWriter.writePropertyAccessorMethods()

        innerClasses.addAll(typeClassWriter.getResultingBytecode())
        data.types.put(typeName, type)
    }
}