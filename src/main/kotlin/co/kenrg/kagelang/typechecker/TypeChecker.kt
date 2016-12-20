package co.kenrg.kagelang.typechecker

//import co.kenrg.kagelang.model.Error
import co.kenrg.kagelang.model.Error
//import co.kenrg.kagelang.model.toError
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.KGTree.VisitorErrorHandler
import co.kenrg.kagelang.tree.types.KGTypeTag
import java.util.*

sealed class TypeCheckingResult {
    class Failure(val errors: List<Error>) : TypeCheckingResult()
    class Success(val type: KGTypeTag) : TypeCheckingResult()
}

class TypeChecker : VisitorErrorHandler<Error> {
    val errors = LinkedList<Error>()

    override fun handleError(error: Error) {
        errors.add(error)
    }

    companion object {
        fun typeCheck(tree: KGTree) = typeCheckAndAttributeTypes(tree)

        fun typeCheckAndAttributeTypes(tree: KGTree): TypeCheckingResult {
            val attributor = TypeCheckerAttributorVisitor()
            tree.accept(attributor, mapOf())
            return if (attributor.typeErrors.isNotEmpty()) {
                TypeCheckingResult.Failure(attributor.typeErrors)
            } else if (attributor.result != KGTypeTag.UNSET) {
                TypeCheckingResult.Success(attributor.result)
            } else {
                TypeCheckingResult.Failure(attributor.typeErrors)
            }
        }
    }
}