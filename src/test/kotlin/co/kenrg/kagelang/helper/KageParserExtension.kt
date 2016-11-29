package co.kenrg.kagelang.helper

import co.kenrg.kagelang.parser.KageParserFacade
import co.kenrg.kagelang.parser.toStream

fun KageParserFacade.parseWithoutPosition(code: String) =
        parse(code.toStream(), considerPosition = false)

