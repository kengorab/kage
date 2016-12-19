package co.kenrg.kagelang.parser

import co.kenrg.kagelang.KageLexer
import co.kenrg.kagelang.KageParser
import co.kenrg.kagelang.ast.Point
import co.kenrg.kagelang.model.Error
import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.TreeMaker
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*

data class KageAntlrParsingResult2(val root: KageParser.KageFileContext?, val errors: List<Error>) {
    fun isCorrect() = errors.isEmpty() && root != null
}

//fun String.toStream(charset: Charset = Charsets.UTF_8) = ByteArrayInputStream(toByteArray(charset))

private object KageAntlrParserFacade2 {
    fun parse(inputStream: InputStream): KageAntlrParsingResult2 {
        val lexicalAndSyntacticErrors = LinkedList<Error>()
        val errorListener = object : ANTLRErrorListener {
            override fun reportAmbiguity(p0: Parser?, p1: DFA?, p2: Int, p3: Int, p4: Boolean, p5: BitSet?, p6: ATNConfigSet?) {
                // Ignored for now
            }

            override fun reportAttemptingFullContext(p0: Parser?, p1: DFA?, p2: Int, p3: Int, p4: BitSet?, p5: ATNConfigSet?) {
                // Ignored for now
            }

            override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInline: Int, msg: String?, ex: RecognitionException?) {
                lexicalAndSyntacticErrors.add(Error(msg!!, Point(line, charPositionInline)))
            }

            override fun reportContextSensitivity(p0: Parser?, p1: DFA?, p2: Int, p3: Int, p4: Int, p5: ATNConfigSet?) {
                // Ignored for now
            }
        }

        val lexer = KageLexer(ANTLRInputStream(inputStream))
        lexer.removeErrorListeners()
        lexer.addErrorListener(errorListener)
        val parser = KageParser(CommonTokenStream(lexer))
        parser.removeErrorListeners()
        parser.addErrorListener(errorListener)
        val root = parser.kageFile()

        return KageAntlrParsingResult2(root, lexicalAndSyntacticErrors)
    }
}

data class ParsingResult2(val root: KGFile, val errors: List<Error>) {
    fun isCorrect() = errors.isEmpty()
}

object KageParserFacade2 {
    fun parse(code: String): ParsingResult2 = parse(code.toStream())

    fun parse(file: File): ParsingResult2 = parse(FileInputStream(file))

    fun parse(inputStream: InputStream, considerPosition: Boolean = true): ParsingResult2 {
        val antlrParsingResult = KageAntlrParserFacade2.parse(inputStream)
        val lexicalAndSyntacticErrors = antlrParsingResult.errors
        val antlrRoot = antlrParsingResult.root ?: throw IllegalStateException("Null antlr root found during parsing; cannot proceed.")
        //        val astRoot = antlrRoot?.toAst(considerPosition = considerPosition)
//        val semanticErrors = astRoot?.validate() ?: emptyList()
        val kageFile = TreeMaker(considerPosition).toKageFile(antlrRoot)
        return ParsingResult2(kageFile, lexicalAndSyntacticErrors)
    }
}
