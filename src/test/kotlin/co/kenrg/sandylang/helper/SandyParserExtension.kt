package co.kenrg.sandylang.helper

import co.kenrg.sandylang.parser.SandyParser
import co.kenrg.sandylang.parser.toStream

fun SandyParser.parseWithoutPosition(code: String) =
        parse(code.toStream(), considerPosition = false)

