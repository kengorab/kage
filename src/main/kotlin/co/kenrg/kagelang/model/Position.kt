package co.kenrg.kagelang.model

data class Point(val line: Int, val column: Int)
data class Position(val start: Point, val end: Point) {
    companion object {
        val DEFAULT = position(0, 0, 0, 0)
    }
}

fun position(startLine: Int, startCol: Int, endLine: Int, endCol: Int) =
        Position(Point(startLine, startCol), Point(endLine, endCol))

