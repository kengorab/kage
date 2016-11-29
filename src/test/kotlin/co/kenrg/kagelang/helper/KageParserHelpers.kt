package co.kenrg.kagelang.helper

import co.kenrg.kagelang.KageLexer
import co.kenrg.kagelang.KageParser
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import java.io.StringReader

fun lexerForCode(code: String) = KageLexer(ANTLRInputStream(StringReader(code)))
fun parseCode(code: String): KageParser.KageFileContext =
        KageParser(CommonTokenStream(lexerForCode(code))).kageFile()
