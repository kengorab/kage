package co.kenrg.kagelang

import co.kenrg.kagelang.parser.KageAntlrParserFacade
import co.kenrg.kagelang.parser.toStream
import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree
import co.kenrg.kagelang.tree.TreeMaker
import co.kenrg.kagelang.tree.types.KGType

fun kageFileFromLines(vararg statements: KGTree, bindings: Map<String, KGType> = mapOf()) =
        KGFile(statements.toList(), bindings)

fun kageFileFromCode(code: String, considerPosition: Boolean = false): KGFile {
    val antlrParsingResult = KageAntlrParserFacade.parse(code.toStream())
    val antlrRoot = antlrParsingResult.root!!
    return TreeMaker(considerPosition).toKageFile(antlrRoot)
}
