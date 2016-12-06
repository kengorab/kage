package co.kenrg.kagelang.parser

import co.kenrg.kagelang.KageLexer
import co.kenrg.kagelang.KageParser
import co.kenrg.kagelang.ast.KageFile
import co.kenrg.kagelang.ast.Point
import co.kenrg.kagelang.ast.extensions.toAst
import co.kenrg.kagelang.ast.validate
import co.kenrg.kagelang.model.Error
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*

data class KageAntlrParsingResult(val root: KageParser.KageFileContext?, val errors: List<Error>) {
    fun isCorrect() = errors.isEmpty() && root != null
}

fun String.toStream(charset: Charset = Charsets.UTF_8) = ByteArrayInputStream(toByteArray(charset))

private object KageAntlrParserFacade {
    fun parse(code: String): KageAntlrParsingResult = parse(code.toStream())

    fun parse(file: File): KageAntlrParsingResult = parse(FileInputStream(file))

    fun parse(inputStream: InputStream): KageAntlrParsingResult {
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

        return KageAntlrParsingResult(root, lexicalAndSyntacticErrors)
    }
}

data class ParsingResult(val root: KageFile?, val errors: List<Error>) {
    fun isCorrect() = errors.isEmpty() && root != null
}

object KageParserFacade {
    fun parse(code: String): ParsingResult = parse(code.toStream())

    fun parse(file: File): ParsingResult = parse(FileInputStream(file))

    fun parse(inputStream: InputStream, considerPosition: Boolean = true): ParsingResult {
        val antlrParsingResult = KageAntlrParserFacade.parse(inputStream)
        val lexicalAndSyntacticErrors = antlrParsingResult.errors
        val antlrRoot = antlrParsingResult.root
        val astRoot = antlrRoot?.toAst(considerPosition = considerPosition)
        val semanticErrors = astRoot?.validate() ?: emptyList()
        return ParsingResult(astRoot, lexicalAndSyntacticErrors + semanticErrors)
    }
}
