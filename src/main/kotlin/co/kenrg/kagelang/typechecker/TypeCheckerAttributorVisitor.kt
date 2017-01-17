package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.model.Error
import co.kenrg.kagelang.model.Signature
import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.Visitor
import co.kenrg.kagelang.tree.KGTree.VisitorErrorHandler
import co.kenrg.kagelang.tree.iface.base.Tree
import co.kenrg.kagelang.tree.types.KGTypeTag
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap
import java.util.*

/**
 * An implementor of the Visitor interface, this visits each node in the tree
 * and determines the type of that node. If the node is correctly typed, it
 * will be "attributed" with that type (meaning, the `type` property of that
 * node will be set. Note, that the `type` property for any subtree defaults
 * to UNSET, and will remain that way unless processed by this class).
 *
 * After being visited by this Visitor, the tree will have its `type` property
 * set, as will all of its subtrees. If the typechecking was unsuccessful, this
 * class's `typeErrors` list will be nonempty.
 *
 * In each of the visitor methods, the `data` parameter contains the currently visible scope.
 * When visiting a function declaration statement, for example, the `data` passed to child tree(s)
 * of that statement will be the function's scope.
 *
 * @see KGTree.Visitor
 */
class TypeCheckerAttributorVisitor(
        val errorHandler: VisitorErrorHandler<Error>? = null
) : Visitor<TCScope>, VisitorErrorHandler<Error> {

    val typeErrors = LinkedList<Error>()
    override fun handleError(error: Error) {
        typeErrors.add(error)
        errorHandler?.handleError(error)
    }

    fun isValid() = typeErrors.isEmpty()

    var result: KGTypeTag = KGTypeTag.UNSET

    fun attribExpr(tree: KGTree, data: TCScope): KGTypeTag {
        tree.accept(this, data)
        return result
    }

    override fun visitTopLevel(file: KGFile, data: TCScope) {
        file.statements.forEach { it.accept(this, data) }
    }

    // Expression visitors

    override fun visitLiteral(literal: KGTree.KGLiteral, data: TCScope) {
        literal.type = literal.typeTag
        result = literal.type
    }

    override fun visitParenthesized(parenthesized: KGTree.KGParenthesized, data: TCScope) {
        val ownType = attribExpr(parenthesized.expr, data)
        parenthesized.type = ownType
        result = ownType
    }

    override fun visitUnary(unary: KGTree.KGUnary, data: TCScope) {
        val exprType = attribExpr(unary.expr, data)

        var ownType = unary.type
        when (unary.kind()) {
            is Tree.Kind.ArithmeticNegation ->
                if (exprType == KGTypeTag.INT || exprType == KGTypeTag.DEC) {
                    ownType = exprType
                } else {
                    handleError(Error(error = "Numeric type expected for arithmetic negation", position = unary.position.start))
                }
            is Tree.Kind.BooleanNegation ->
                if (exprType == KGTypeTag.BOOL) {
                    ownType = exprType
                } else {
                    handleError(Error(error = "Boolean type expected for boolean negation", position = unary.position.start))
                }
        }
        unary.type = ownType
        result = ownType
    }

    override fun visitBinary(binary: KGTree.KGBinary, data: TCScope) {
        val leftType = attribExpr(binary.left, data)
        val rightType = attribExpr(binary.right, data)

        var ownType = binary.type
        when (binary.kind()) {
            is Tree.Kind.ConditionalAnd,
            is Tree.Kind.ConditionalOr ->
                if (leftType == KGTypeTag.BOOL && rightType == KGTypeTag.BOOL) {
                    ownType = KGTypeTag.BOOL
                } else {
                    handleError(Error(error = "Booleans expected", position = binary.position.start))
                }
            is Tree.Kind.Plus,
            is Tree.Kind.Minus,
            is Tree.Kind.Multiply ->
                if (!KGTypeTag.numericTypes.contains(leftType)) {
                    handleError(Error(error = "Numeric type expected for left expression", position = binary.position.start))
                } else if (!KGTypeTag.numericTypes.contains(rightType)) {
                    handleError(Error(error = "Numeric type expected for right expression", position = binary.position.start))
                } else if (leftType == KGTypeTag.INT && rightType == KGTypeTag.INT) {
                    ownType = KGTypeTag.INT
                } else if (leftType == KGTypeTag.DEC && rightType == KGTypeTag.DEC) {
                    ownType = KGTypeTag.DEC
                } else {
                    ownType = KGTypeTag.DEC
                }
            is Tree.Kind.Divide ->
                if (!KGTypeTag.numericTypes.contains(leftType)) {
                    handleError(Error(error = "Numeric type expected for left expression", position = binary.position.start))
                } else if (!KGTypeTag.numericTypes.contains(rightType)) {
                    handleError(Error(error = "Numeric type expected for right expression", position = binary.position.start))
                } else {
                    ownType = KGTypeTag.DEC
                }
            is Tree.Kind.Concatenation ->
                ownType = KGTypeTag.STRING
            is Tree.Kind.GreaterThan,
            is Tree.Kind.LessThan ->
                if (!KGTypeTag.comparableTypes.contains(leftType)) {
                    handleError(Error(error = "Comparable type expected for left expression", position = binary.position.start))
                } else if (!KGTypeTag.comparableTypes.contains(rightType)) {
                    handleError(Error(error = "Comparable type expected for right expression", position = binary.position.start))
                } else {
                    ownType = KGTypeTag.BOOL
                }
            else ->
                throw UnsupportedOperationException("${binary.kind().javaClass.canonicalName} is not a BinaryTree")
        }

        binary.type = ownType
        result = ownType
    }

    override fun visitBindingReference(bindingReference: KGTree.KGBindingReference, data: TCScope) {
        val binding = data.getVal(bindingReference.binding) ?: data.getFnsForName(bindingReference.binding)

        if (binding == null) {
            handleError(Error("Binding with name ${bindingReference.binding} not visible in current context", bindingReference.position.start))
        } else {
//            if (binding.expression.type == KGTypeTag.UNSET) {
//                throw IllegalStateException("Binding expression's type is UNSET, somehow...")
//            }
//
//            bindingReference.type = binding.expression.type
//            result = binding.expression.type
            when (binding) {
                is TCBinding.StaticValBinding -> {
                    if (binding.type == KGTypeTag.UNSET) {
                        throw IllegalStateException("Binding expression's type is UNSET, somehow...")
                    }

                    bindingReference.type = binding.type
                    result = binding.type
                }
                is TCBinding.FunctionBinding -> {
                    // TODO - Come back to this. The type of a binding is only the signature's return type if all params supplied.
                    bindingReference.type = binding.signature.returnType
                    result = binding.signature.returnType
                }
            }
        }
    }

    override fun visitInvocation(invocation: KGTree.KGInvocation, data: TCScope) {
        invocation.invokee.accept(this, data)
        when (invocation.invokee) {
            is KGTree.KGBindingReference -> {
                val fnsForName = data.getFnsForName(invocation.invokee.binding)
                if (fnsForName == null) {
                    handleError(Error("Binding with name ${invocation.invokee.binding} not visible in current context", invocation.invokee.position.start))
                } else {
                    val fnsMatchingParamsSig = fnsForName.filter {
                        invocation.params.map { it.type } == it.signature.params.map { it.type }
                    }

                    if (fnsMatchingParamsSig.size > 1) {
                        handleError(Error("Multiple functions available with name ${invocation.invokee.binding} and params signature", invocation.invokee.position.start))
                    } else if (fnsMatchingParamsSig.isEmpty()) {
                        // To not throw a bunch of errors, only try to compare against the first possible match for the
                        // function's name. In the future, this should probably show all possible matches for function name.
                        val firstFnForName = fnsForName.first()
                        if (invocation.params.size == firstFnForName.signature.params.size) {
                            invocation.params.zip(firstFnForName.signature.params).forEach { pair ->
                                val (param, requiredType) = pair
                                if (param.type != requiredType.type) {
                                    handleError(Error("Type mismatch. Required: $requiredType, Actual: ${param.type}", invocation.position.start))
                                }
                            }
                        } else {
                            if (invocation.params.size < firstFnForName.signature.params.size) {
                                // TODO - Allow for currying in the (distant?) future
                                handleError(Error("Not enough arguments provided", invocation.position.start))
                            } else {
                                handleError(Error("Too many arguments provided", invocation.position.start))
                            }
                        }

                        // This is a terrible way to assign a type to the invocation; it takes the first possibility
                        // for a match based on the function's name and uses its return type. Really, it should attempt
                        // to pick a function whose return type matches the inferred type of the invocation. Inference
                        // is going to be a whole other animal.
                        invocation.type = firstFnForName.signature.returnType
                        result = firstFnForName.signature.returnType
                    } else {
                        // If there's exactly 1 function matching the params signature
                        invocation.type = fnsMatchingParamsSig.first().signature.returnType
                        result = fnsMatchingParamsSig.first().signature.returnType
                    }
                }
            }
            else ->
                handleError(Error("Expression is not invokable", invocation.invokee.position.start))
        }
    }

    override fun visitLetIn(letIn: KGTree.KGLetIn, data: TCScope) {
        val childScope = TCScope(vals = HashMap(), functions = ArrayListValuedHashMap(), parent = data)
        letIn.statements.forEach { it.accept(this, childScope) }
        letIn.body.accept(this, childScope)

        letIn.type = letIn.body.type
        result = letIn.body.type
    }

    // Statement visitors

    override fun visitPrint(print: KGTree.KGPrint, data: TCScope) {
        // Typecheck the internal expression; type of print statement will always be Unit
        attribExpr(print.expr, data)
        result = KGTypeTag.UNIT
    }

    override fun visitValDeclaration(valDecl: KGTree.KGValDeclaration, data: TCScope) {
        attribExpr(valDecl.expression, data)

        if (valDecl.typeAnnotation != null) {
            if (valDecl.expression.type != valDecl.typeAnnotation) {
                handleError(Error("Expression not assignable to type ${valDecl.typeAnnotation}", valDecl.position.start))
            }
        }

        if (data.vals.containsKey(valDecl.identifier)) {
            handleError(Error("Duplicate binding: val \"${valDecl.identifier}\" already defined in this context", valDecl.position.start))
        } else {
            data.vals.put(valDecl.identifier, TCBinding.StaticValBinding(valDecl.identifier, valDecl.expression.type))
        }

        result = KGTypeTag.UNIT
    }

    override fun visitFnDeclaration(fnDecl: KGTree.KGFnDeclaration, data: TCScope) {
        if (fnDecl.params.isNotEmpty()) {
            fnDecl.params.forEachIndexed { index, param ->
                if (fnDecl.params.subList(0, index).map { it.name }.contains(param.name)) {
                    handleError(Error("Cannot have duplicated parameter name ${param.name}", fnDecl.position.start))
                }
            }
        }

        val vals = fnDecl.params.map {
            it.name to TCBinding.StaticValBinding(it.name, it.type)
        }.toMap(HashMap<String, TCBinding.StaticValBinding>())

        val fnScope = TCScope(vals = vals, parent = data)

        attribExpr(fnDecl.body, fnScope)

        // TODO - Continue even after type annotation validation fails? Check in val decl above, too.
        if (fnDecl.retTypeAnnotation != null && fnDecl.retTypeAnnotation != fnDecl.body.type) {
            handleError(Error("Expected return type of ${fnDecl.retTypeAnnotation}, saw ${fnDecl.body.type}", fnDecl.body.position.start))
        }

        val fnSignature = Signature(params = fnDecl.params, returnType = fnDecl.body.type)

        if (data.functions.containsKey(fnDecl.name)) {
            data.functions[fnDecl.name].forEach {
                if (it.signature.returnType != fnSignature.returnType) {
                    handleError(Error("Binding \"${fnDecl.name}\" conflicts with another in this context", fnDecl.position.start))
                } else if (it.signature.params == fnSignature.params) {
                    handleError(Error("Binding \"${fnDecl.name}\" conflicts with another in this context", fnDecl.position.start))
                } else {
                    // If two functions have the same name and return type, but different param signatures, allow it.
                }
            }
        }

        fnDecl.signature = fnSignature
        data.functions.put(fnDecl.name, TCBinding.FunctionBinding(fnDecl.name, fnSignature))
        result = KGTypeTag.UNIT
    }
}