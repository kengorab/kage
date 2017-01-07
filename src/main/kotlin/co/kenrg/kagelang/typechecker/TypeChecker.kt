package co.kenrg.kagelang.typechecker

import co.kenrg.kagelang.codegen.TC
import co.kenrg.kagelang.model.Error
//import co.kenrg.kagelang.model.toError
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.VisitorErrorHandler
import co.kenrg.kagelang.tree.types.KGTypeTag
import java.util.*

sealed class TypeCheckingResult {
    class Failure(val errors: List<Error>) : TypeCheckingResult()
    class Success(val type: KGTypeTag, val namespace: TC.Namespace) : TypeCheckingResult()
}

class TypeChecker : VisitorErrorHandler<Error> {
    val errors = LinkedList<Error>()

    override fun handleError(error: Error) {
        errors.add(error)
    }

    companion object {
        fun typeCheck(tree: KGTree, namespace: TC.Namespace): TypeCheckingResult {
            val attributor = TypeCheckerAttributorVisitor()

            tree.accept(attributor, namespace.rootScope)

            return if (attributor.typeErrors.isNotEmpty()) {
                TypeCheckingResult.Failure(attributor.typeErrors)
            } else if (attributor.result != KGTypeTag.UNSET) {
                TypeCheckingResult.Success(attributor.result, namespace)
            } else {
                TypeCheckingResult.Failure(attributor.typeErrors)
            }
        }
    }
}