package co.kenrg.kagelang.model

data class Error2(val error: String, val position: Point?)
data class Error(val error: String, val position: co.kenrg.kagelang.ast.Point?)

// Temporary, until the two different implementations are merged
fun Error.toError2(): Error2 =
        Error2(this.error, Point(this.position!!.line, this.position.column))

fun Error2.toError(): Error =
        Error(this.error, co.kenrg.kagelang.ast.Point(this.position!!.line, this.position.column))

