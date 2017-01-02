package co.kenrg.kagelang.tree

import co.kenrg.kagelang.parser.KageAntlrParserFacade
import co.kenrg.kagelang.parser.toStream
import co.kenrg.kagelang.tree.types.KGTypeTag

fun kageFileFromLines(vararg statements: KGTree, bindings: Map<String, KGTypeTag> = mapOf()) =
        KGFile(statements.toList(), bindings)

fun kageFileFromCode(code: String, considerPosition: Boolean = false): KGFile {
    val antlrParsingResult = KageAntlrParserFacade.parse(code.toStream())
    val antlrRoot = antlrParsingResult.root!!
    return TreeMaker(considerPosition).toKageFile(antlrRoot)
}
