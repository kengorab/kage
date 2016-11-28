package co.kenrg.sandylang.asm

import co.kenrg.sandylang.ast.*
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.Label
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes.*
import java.util.*

interface SandyJvmType {
    val jvmDescription: String
}

data class Var(val type: SandyJvmType, val index: Int)

object IntJvmType : SandyJvmType {
    override val jvmDescription: String get() = "I"
}

object DecimalJvmType : SandyJvmType {
    override val jvmDescription: String get() = "D"
}

fun Type.toJvmType(): SandyJvmType = when (this) {
    is IntType -> IntJvmType
    is DecimalType -> DecimalJvmType
    else -> throw UnsupportedOperationException("Unrecognized type: ${this}")
}

val validBinaryExpressionTypes = listOf(IntJvmType, DecimalJvmType)

fun getExpressionType(expr: Expression, vars: Map<String, Var>): SandyJvmType = when (expr) {
    is IntLiteralExpression -> IntJvmType
    is DecimalLiteralExpression -> DecimalJvmType
    is VarReferenceExpression -> vars[expr.varName]!!.type  // By this point, we've validated that the var should exist
    is TypeConversionExpression -> expr.targetType.toJvmType()
    is UnaryMinusExpression -> getExpressionType(expr, vars)
    is BinaryExpression -> {
        val leftType = getExpressionType(expr.left, vars)
        val rightType = getExpressionType(expr.right, vars)

        if (!validBinaryExpressionTypes.contains(leftType) || !validBinaryExpressionTypes.contains(rightType)) {
            throw UnsupportedOperationException("Cannot typecheck binary expression for types (left: $leftType, right: $rightType)")
        }

        if (leftType == IntJvmType && rightType == IntJvmType) {
            IntJvmType
        } else {
            DecimalJvmType
        }
    }
    else -> throw UnsupportedOperationException("Cannot get type for ${expr.javaClass.canonicalName}")
}

fun pushExpressionAs(methodWriter: MethodVisitor, expr: Expression, vars: Map<String, Var>, desiredType: SandyJvmType) {
    pushExpression(methodWriter, expr, vars)
    val currentType = getExpressionType(expr, vars)
    if (currentType != desiredType) {
        if (currentType == IntJvmType && desiredType == DecimalJvmType) {
            methodWriter.visitInsn(I2D)
        } else if (currentType == DecimalJvmType && desiredType == IntJvmType) {
            methodWriter.visitInsn(D2I)
        } else {
            throw UnsupportedOperationException("Type conversion from $currentType to $desiredType")
        }
    }
}

fun pushExpression(methodWriter: MethodVisitor, expr: Expression, vars: Map<String, Var>) {
    when (expr) {
        is IntLiteralExpression -> methodWriter.visitLdcInsn(Integer.parseInt(expr.value))
        is DecimalLiteralExpression -> methodWriter.visitLdcInsn(java.lang.Double.parseDouble(expr.value))
        is SumExpression -> {
            pushExpressionAs(methodWriter, expr.left, vars, getExpressionType(expr, vars))
            pushExpressionAs(methodWriter, expr.right, vars, getExpressionType(expr, vars))
            when (getExpressionType(expr, vars)) {
                is IntJvmType -> methodWriter.visitInsn(IADD)
                is DecimalJvmType -> methodWriter.visitInsn(DADD)
                else -> throw UnsupportedOperationException("Summing ${getExpressionType(expr, vars)}")
            }
        }
        is SubtractionExpression -> {
            pushExpressionAs(methodWriter, expr.left, vars, getExpressionType(expr, vars))
            pushExpressionAs(methodWriter, expr.right, vars, getExpressionType(expr, vars))
            when (getExpressionType(expr, vars)) {
                is IntJvmType -> methodWriter.visitInsn(ISUB)
                is DecimalJvmType -> methodWriter.visitInsn(DSUB)
                else -> throw UnsupportedOperationException("Subtracting ${getExpressionType(expr, vars)}")
            }
        }
        is DivisionExpression -> {
            pushExpressionAs(methodWriter, expr.left, vars, getExpressionType(expr, vars))
            pushExpressionAs(methodWriter, expr.right, vars, getExpressionType(expr, vars))
            when (getExpressionType(expr, vars)) {
                is IntJvmType -> methodWriter.visitInsn(IDIV)
                is DecimalJvmType -> methodWriter.visitInsn(DDIV)
                else -> throw UnsupportedOperationException("Dividing ${getExpressionType(expr, vars)}")
            }
        }
        is MultiplicationExpression -> {
            pushExpressionAs(methodWriter, expr.left, vars, getExpressionType(expr, vars))
            pushExpressionAs(methodWriter, expr.right, vars, getExpressionType(expr, vars))
            when (getExpressionType(expr, vars)) {
                is IntJvmType -> methodWriter.visitInsn(IMUL)
                is DecimalJvmType -> methodWriter.visitInsn(DMUL)
                else -> throw UnsupportedOperationException("Multiplying ${getExpressionType(expr, vars)}")
            }
        }
        is VarReferenceExpression -> {
            val type = vars[expr.varName]!!.type
            when (type) {
                is IntJvmType -> methodWriter.visitVarInsn(ILOAD, vars[expr.varName]!!.index)
                is DecimalJvmType -> methodWriter.visitVarInsn(DLOAD, vars[expr.varName]!!.index)
                else -> throw UnsupportedOperationException("Loading ${expr.javaClass.canonicalName}")
            }
        }
        is TypeConversionExpression -> {
            pushExpressionAs(methodWriter, expr, vars, expr.targetType.toJvmType())
        }
        else -> throw UnsupportedOperationException(expr.javaClass.canonicalName)
    }
}

object JvmCompiler {
    fun compileClass(root: SandyFile, className: String): ByteArray {
        val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        classWriter.visit(V1_8, ACC_PUBLIC, className, null, "java/lang/Object", null)

        val mainMethodWriter = classWriter.visitMethod(ACC_PUBLIC or ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null)
        mainMethodWriter.visitCode()
        val methodStart = Label()
        val methodEnd = Label()
        mainMethodWriter.visitLabel(methodStart)

        var nextVarIndex = 0
        val vars = HashMap<String, Var>()
        root.processNodeType(VarDeclarationStatement::class.java) {
            val index = nextVarIndex++
            val exprType = getExpressionType(it.value, vars)
            vars[it.varName] = Var(exprType, index)
            mainMethodWriter.visitLocalVariable(it.varName, exprType.jvmDescription, null, methodStart, methodEnd, index)
        }

        root.statements.forEach { stmt ->
            when (stmt) {
                is VarDeclarationStatement -> {
                    val type = vars[stmt.varName]!!.type
                    pushExpressionAs(mainMethodWriter, stmt.value, vars, type)
                    when (type) {
                        is IntJvmType -> mainMethodWriter.visitVarInsn(ISTORE, vars[stmt.varName]!!.index)
                        is DecimalJvmType -> mainMethodWriter.visitVarInsn(DSTORE, vars[stmt.varName]!!.index)
                        else -> throw UnsupportedOperationException("Cannot store variable ${stmt.varName} of type $type")
                    }
                }
                is PrintStatement -> {
                    mainMethodWriter.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
                    pushExpression(mainMethodWriter, stmt.value, vars)
                    val printlnSignature = "(${getExpressionType(stmt.value, vars).jvmDescription})V"
                    mainMethodWriter.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", printlnSignature, false)
                }
                is AssignmentStatement -> {
                    val type = vars[stmt.varName]!!.type
                    pushExpressionAs(mainMethodWriter, stmt.value, vars, type)
                    when (type) {
                        is IntJvmType -> mainMethodWriter.visitVarInsn(ISTORE, vars[stmt.varName]!!.index)
                        is DecimalJvmType -> mainMethodWriter.visitVarInsn(DSTORE, vars[stmt.varName]!!.index)
                        else -> throw UnsupportedOperationException("Cannot store variable ${stmt.varName} of type $type")
                    }
                }
                else -> throw UnsupportedOperationException(stmt.javaClass.canonicalName)
            }
        }

        mainMethodWriter.visitLabel(methodEnd)
        mainMethodWriter.visitInsn(RETURN)
        mainMethodWriter.visitEnd()
        mainMethodWriter.visitMaxs(-1, -1)
        classWriter.visitEnd()
        return classWriter.toByteArray()
    }
}