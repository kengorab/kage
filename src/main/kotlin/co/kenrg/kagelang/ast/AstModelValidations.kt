package co.kenrg.kagelang.ast

import co.kenrg.kagelang.model.Error
import java.util.*

fun Node.isBefore(other: Node): Boolean = position?.start?.isBefore(other.position!!.start) ?: false
fun Point.isBefore(other: Point): Boolean = line < other.line || (line == other.line && column < other.column)

fun KageFile.validate(): List<Error> {
    val errors = LinkedList<Error>()

    // Check for duplicate variables
    val declaredVars = HashMap<String, VarDeclarationStatement>()
    this.processNodeType(VarDeclarationStatement::class.java) {
        if (declaredVars.containsKey(it.varName)) {
            val error = Error("A variable named '${it.varName}' has already been declared", it.position?.start)
            errors.add(error)
        } else {
            declaredVars[it.varName] = it
        }
    }

    // Check for declaration of vars before reference
    this.processNodeType(VarReferenceExpression::class.java) {
        if (!declaredVars.containsKey(it.varName)) {
            val error = Error("There is no variable named '${it.varName}'", it.position?.start)
            errors.add(error)
        } else if (it.isBefore(declaredVars[it.varName]!!)) {
            val error = Error("You cannot refer to variable '${it.varName}' before its declaration", it.position?.start)
            errors.add(error)
        }
    }
    this.processNodeType(AssignmentStatement::class.java) {
        if (!declaredVars.containsKey(it.varName)) {
            val error = Error("There is no variable named '${it.varName}'", it.position?.start)
            errors.add(error)
        } else if (it.isBefore(declaredVars[it.varName]!!)) {
            val error = Error("You cannot refer to variable '${it.varName}' before its declaration", it.position?.start)
            errors.add(error)
        }
    }

    return errors
}
