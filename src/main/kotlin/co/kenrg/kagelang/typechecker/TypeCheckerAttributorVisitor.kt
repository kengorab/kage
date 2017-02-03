package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.model.Error
import co.kenrg.kagelang.model.Signature
import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.Visitor
import co.kenrg.kagelang.tree.KGTree.VisitorErrorHandler
import co.kenrg.kagelang.tree.iface.base.Tree
import co.kenrg.kagelang.tree.types.KGType
import co.kenrg.kagelang.tree.types.StdLibTypes
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
        val namespaceName: String,
        val errorHandler: VisitorErrorHandler<Error>? = null
) : Visitor<TCScope>, VisitorErrorHandler<Error> {

    val typeErrors = LinkedList<Error>()
    override fun handleError(error: Error) {
        typeErrors.add(error)
        errorHandler?.handleError(error)
    }

    fun isValid() = typeErrors.isEmpty()

    var result: KGType? = null

    fun attribExpr(tree: KGTree, data: TCScope): KGType? {
        tree.accept(this, data)
        return result
    }

    override fun visitTopLevel(file: KGFile, data: TCScope) {
        file.statements.forEach { it.accept(this, data) }
    }

    // Expression visitors

    override fun visitLiteral(literal: KGTree.KGLiteral, data: TCScope) {
        literal.type = literal.litType
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
                if (exprType == KGType.INT || exprType == KGType.DEC) {
                    ownType = exprType
                } else {
                    handleError(Error(error = "Numeric type expected for arithmetic negation", position = unary.position.start))
                }
            is Tree.Kind.BooleanNegation ->
                if (exprType == KGType.BOOL) {
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

        if (leftType == null || rightType == null) {
            if (leftType == null)
                handleError(Error("Could not determine type for binary left", binary.left.position.start))

            if (rightType == null)
                handleError(Error("Could not determine type for binary right", binary.right.position.start))

            result = binary.type
            return
        }

        var ownType = binary.type
        when (binary.kind()) {
            is Tree.Kind.ConditionalAnd,
            is Tree.Kind.ConditionalOr ->
                if (leftType == KGType.BOOL && rightType == KGType.BOOL) {
                    ownType = KGType.BOOL
                } else {
                    handleError(Error(error = "Booleans expected", position = binary.position.start))
                }
            is Tree.Kind.Plus,
            is Tree.Kind.Minus,
            is Tree.Kind.Multiply ->
                if (!leftType.isNumeric) {
                    handleError(Error(error = "Numeric type expected for left expression", position = binary.position.start))
                } else if (!rightType.isNumeric) {
                    handleError(Error(error = "Numeric type expected for right expression", position = binary.position.start))
                } else if (leftType == KGType.INT && rightType == KGType.INT) {
                    ownType = KGType.INT
                } else if (leftType == KGType.DEC && rightType == KGType.DEC) {
                    ownType = KGType.DEC
                } else {
                    ownType = KGType.DEC
                }
            is Tree.Kind.Divide ->
                if (!leftType.isNumeric) {
                    handleError(Error(error = "Numeric type expected for left expression", position = binary.position.start))
                } else if (!rightType.isNumeric) {
                    handleError(Error(error = "Numeric type expected for right expression", position = binary.position.start))
                } else {
                    ownType = KGType.DEC
                }
            is Tree.Kind.Concatenation ->
                ownType = KGType.STRING
            is Tree.Kind.GreaterThan,
            is Tree.Kind.LessThan,
            is Tree.Kind.GreaterThanOrEqualTo,
            is Tree.Kind.LessThanOrEqualTo ->
                if (!leftType.isComparable) {
                    handleError(Error(error = "Comparable type expected for left expression", position = binary.position.start))
                } else if (!rightType.isComparable) {
                    handleError(Error(error = "Comparable type expected for right expression", position = binary.position.start))
                } else {
                    if (leftType.isNumeric && !rightType.isNumeric || rightType.isNumeric && !leftType.isNumeric) {
                        handleError(Error(error = "Cannot compare unlike types", position = binary.position.start))
                    } else {
                        ownType = KGType.BOOL
                    }
                }
            is Tree.Kind.Equals,
            is Tree.Kind.NotEquals ->
                // Cannot compare unlike types, unless they're numeric. This should be handled better.
                if (!((leftType == rightType) || (leftType.isNumeric && rightType.isNumeric))) {
                    handleError(Error(error = "Cannot compare unlike types $leftType and $rightType", position = binary.position.start))
                } else {
                    ownType = KGType.BOOL
                }
            else ->
                throw UnsupportedOperationException("${binary.kind().javaClass.canonicalName} is not a BinaryTree")
        }

        binary.type = ownType
        result = ownType
    }

    override fun visitBindingReference(bindingReference: KGTree.KGBindingReference, data: TCScope) {
        val binding = data.getVal(bindingReference.binding)
                ?: data.getFnsForName(bindingReference.binding)
                ?: data.getType(bindingReference.binding)

        if (binding == null) {
            handleError(Error("Binding with name ${bindingReference.binding} not visible in current context", bindingReference.position.start))
        } else {
            when (binding) {
                is TCBinding.StaticValBinding -> {
                    if (binding.type == null)
                        throw IllegalStateException("Binding expression's type is UNSET, somehow...")

                    bindingReference.type = binding.type
                    result = binding.type
                }
                is TCBinding.FunctionBinding -> {
                    // TODO - Come back to this. The type of a binding is only the signature's return type if all params supplied.
                    bindingReference.type = binding.signature.returnType
                    result = binding.signature.returnType
                }
                is KGType -> {
                    bindingReference.type = binding
                    result = binding
                }
            }
        }
    }

    override fun visitInvocation(invocation: KGTree.KGInvocation, data: TCScope) {
        invocation.invokee.accept(this, data)
        invocation.params.forEach { it.accept(this, data) }

        when (invocation.invokee) {
            is KGTree.KGBindingReference -> {
                val invokeeName = invocation.invokee.binding

                // Check to see if invocation target is a type constructor function.
                val typeForName = data.getType(invokeeName)
                if (typeForName != null) {
                    if (invocation.params.size == typeForName.props.size) {
                        invocation.params.zip(typeForName.props.values).forEach { pair ->
                            val (param, requiredType) = pair

                            if (param.type != requiredType)
                                handleError(Error("Type mismatch. Required: $requiredType, Actual: ${param.type}", invocation.position.start))
                        }
                    } else {
                        handleError(Error("Not enough arguments provided to constructor for $invokeeName", invocation.position.start))
                    }

                    invocation.type = typeForName
                    result = typeForName
                    return
                }

                // If the invocation target is not a type constructor function, carry on with function typechecking.
                val fnsForName = data.getFnsForName(invokeeName)
                if (fnsForName == null) {
                    handleError(Error("Binding with name $invokeeName not visible in current context", invocation.invokee.position.start))
                } else {
                    val fnsMatchingParamsSig = fnsForName.filter {
                        invocation.params.map { it.type } == it.signature.params.map { it.second }
                    }

                    if (fnsMatchingParamsSig.size > 1) {
                        handleError(Error("Multiple functions available with name $invokeeName and params signature", invocation.invokee.position.start))
                    } else if (fnsMatchingParamsSig.isEmpty()) {
                        // To not throw a bunch of errors, only try to compare against the first possible match for the
                        // function's name. In the future, this should probably show all possible matches for function name.
                        val firstFnForName = fnsForName.first()
                        if (invocation.params.size == firstFnForName.signature.params.size) {
                            invocation.params.zip(firstFnForName.signature.params).forEach { pair ->
                                val (param, required) = pair
                                val (name, requiredType) = required

                                if (param.type != requiredType)
                                    handleError(Error("Type mismatch. Required: $requiredType, Actual: ${param.type}", invocation.position.start))
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
            else -> handleError(Error("Expression is not invokable", invocation.invokee.position.start))
        }
    }

    override fun visitLetIn(letIn: KGTree.KGLetIn, data: TCScope) {
        val childScope = data.createChildScope()
        letIn.statements.forEach { it.accept(this, childScope) }
        letIn.body.accept(this, childScope)

        letIn.type = letIn.body.type
        result = letIn.body.type
    }

    override fun visitIfThenElse(ifElse: KGTree.KGIfThenElse, data: TCScope) {
        attribExpr(ifElse.condition, data)
        if (ifElse.condition.type != KGType.BOOL)
            handleError(Error("If expression's condition must be Bool", ifElse.position.start))

        ifElse.thenBody.accept(this, data)
        val thenBodyType = ifElse.thenBody.type

        if (ifElse.elseBody == null) {
            if (thenBodyType != KGType.UNIT)
                handleError(Error("If expression returning a value must have `then` and `else` branches", ifElse.position.start))
            else {
                ifElse.type = KGType.UNIT
                result = KGType.UNIT
            }
        } else {
            ifElse.elseBody.accept(this, data)
            val elseBodyType = ifElse.elseBody.type

            if (elseBodyType != thenBodyType)
                handleError(Error("Type mismatch: type of `else` branch must match that of `then` branch", ifElse.elseBody.position.start))
            else {
                ifElse.type = thenBodyType
                result = thenBodyType
            }
        }
    }

    override fun visitDot(dot: KGTree.KGDot, data: TCScope) {
        val targetType = attribExpr(dot.target, data)
        if (targetType == null) {
            handleError(Error("Could not determine type for dot target", dot.target.position.start))
            dot.type = null
            result = null
            return
        }

        if (targetType.props.isEmpty()) {
            handleError(Error("No available props for dot target", dot.position.start))
            dot.type = null
            result = null
            return
        }

        if (!targetType.props.keys.contains(dot.prop)) {
            handleError(Error("Prop ${dot.prop} not available on dot target", dot.position.start))
            dot.type = null
            result = null
        } else {
            val dotType = targetType.props[dot.prop]
            dot.type = dotType
            result = dotType
        }
    }

    override fun visitTuple(tuple: KGTree.KGTuple, data: TCScope) {
        if (tuple.items.size > 6)
            handleError(Error("Tuples larger than 6 items not supported; you should consider creating a type instead", tuple.position.start))

        val itemTypes = tuple.items.mapIndexed { i, item ->
            val type = attribExpr(item, data)
            if (type == null)
                handleError(Error("Could not determine type for tuple item at position $i", tuple.position.start))
            type!!
        }

        val tupleKind = when (tuple.items.size) {
            2 -> StdLibTypes.Pair
            3 -> StdLibTypes.Triple
            4 -> StdLibTypes.Tuple4
            5 -> StdLibTypes.Tuple5
            6 -> StdLibTypes.Tuple6
            else -> throw UnsupportedOperationException("Tuples larger than 6 items not supported; you should consider creating a type instead")
        }

        val tupleType = KGType.stdLibType(tupleKind)
                .copy(typeParams = itemTypes)
        tuple.type = tupleType
        result = tupleType
    }

    // Statement visitors

    override fun visitPrint(print: KGTree.KGPrint, data: TCScope) {
        // Typecheck the internal expression; type of print statement will always be Unit
        attribExpr(print.expr, data)
        print.type = KGType.UNIT
        result = KGType.UNIT
    }

    override fun visitValDeclaration(valDecl: KGTree.KGValDeclaration, data: TCScope) {
        attribExpr(valDecl.expression, data)

        if (valDecl.typeAnnotation != null) {
            val annotatedType = valDecl.typeAnnotation.hydrate(data) {
                handleError(Error("Type with name $it not visible in this context", valDecl.position.start))
            }

            if (annotatedType == null)
                handleError(Error("Type with name ${valDecl.typeAnnotation} not visible in this context", valDecl.position.start))

            if (valDecl.expression.type != annotatedType)
                handleError(Error("Expression not assignable to type $annotatedType", valDecl.position.start))
        }

        if (data.vals.containsKey(valDecl.identifier)) {
            handleError(Error("Duplicate binding: val \"${valDecl.identifier}\" already defined in this context", valDecl.position.start))
        } else {
            data.vals.put(valDecl.identifier, TCBinding.StaticValBinding(valDecl.identifier, valDecl.expression.type))
        }

        valDecl.type = KGType.UNIT
        result = KGType.UNIT
    }

    override fun visitFnDeclaration(fnDecl: KGTree.KGFnDeclaration, data: TCScope) {
        if (fnDecl.params.isNotEmpty()) {
            fnDecl.params.forEachIndexed { index, param ->
                if (fnDecl.params.subList(0, index).map { it.name }.contains(param.name)) {
                    handleError(Error("Cannot have duplicated parameter name ${param.name}", fnDecl.position.start))
                }
            }
        }

        val vals = fnDecl.params
                .map {
                    val type = it.type.hydrate(data) {
                        handleError(Error("Type with name $it not visible in this context", fnDecl.position.start))
                    }
                    if (type == null)
                        handleError(Error("No type definition found for ${it.type}. Is it referenced before being declared?", fnDecl.position.start))

                    it.name to TCBinding.StaticValBinding(it.name, type)
                }
                .toMap(HashMap<String, TCBinding.StaticValBinding>())

        val fnScope = data.createChildScope(vals)
        attribExpr(fnDecl.body, fnScope)
        val fnBodyType = fnDecl.body.type
        if (fnBodyType == null) {
            handleError(Error("Could not determine type", fnDecl.body.position.start))
            result = KGType.UNIT
            return
        }

        // TODO - Continue even after type annotation validation fails? Check in val decl above, too.
        if (fnDecl.retTypeAnnotation != null) {
            val fnRetType = fnDecl.retTypeAnnotation.hydrate(data) {
                handleError(Error("Type with name $it not visible in this context", fnDecl.position.start))
            }

            if (fnRetType != fnDecl.body.type)
                handleError(Error("Expected return type of $fnRetType, saw ${fnDecl.body.type}", fnDecl.body.position.start))
        }

        // TODO - Don't duplicate mapping over the fn params (on line 412)
        val params = fnDecl.params.map {
            val type = it.type.hydrate(data) {
                handleError(Error("Type with name $it not visible in this context", fnDecl.position.start))
            }
            if (type == null)
                handleError(Error("No type definition found for ${it.type}. Is it referenced before being declared?", fnDecl.position.start))
            Pair(it.name, type!!)
        }

        val fnSignature = Signature(params = params, returnType = fnBodyType)

        if (data.types.containsKey(fnDecl.name)) {
            handleError(Error("Binding \"${fnDecl.name}\" conflicts with a type name in this context", fnDecl.position.start))
        } else if (data.functions.containsKey(fnDecl.name)) {
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
        result = KGType.UNIT
    }

    override fun visitTypeDeclaration(typeDecl: KGTree.KGTypeDeclaration, data: TCScope) {
        if (!data.isRoot())
            handleError(Error("Type declarations must occur at root", typeDecl.position.start))

        val typeName = typeDecl.name
        if (data.types.containsKey(typeName))
            handleError(Error("Type $typeName conflicts with another type in this context", typeDecl.position.start))

        if (typeName[0].isLowerCase())
            handleError(Error("Naming: Type $typeName should begin with a capital letter", typeDecl.position.start))

        val typeProps = typeDecl.props
                .map {
                    val type = it.type.hydrate(data) {
                        handleError(Error("No type definition found for $it. Is it referenced before being declared?", typeDecl.position.start))
                    }
                    it.name to type!!
                }
                .toMap()

        val innerClassName = "$namespaceName\$$typeName"
        val type = KGType(typeName, className = innerClassName, props = typeProps)
        data.types.put(typeName, type)

        typeDecl.type = KGType.UNIT
        result = KGType.UNIT
    }
}