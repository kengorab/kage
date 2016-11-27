package co.kenrg.sandylang.parser

import co.kenrg.sandylang.SandyLexer
import co.kenrg.sandylang.SandyParser
import co.kenrg.sandylang.ast.Error
import co.kenrg.sandylang.ast.Point
import co.kenrg.sandylang.ast.SandyFile
import co.kenrg.sandylang.ast.extensions.toAst
import co.kenrg.sandylang.ast.validate
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*

data class SandyAntlrParsingResult(val root: SandyParser.SandyFileContext?, val errors: List<Error>) {
    fun isCorrect() = errors.isEmpty() && root != null
}

fun String.toStream(charset: Charset = Charsets.UTF_8) = ByteArrayInputStream(toByteArray(charset))

private object SandyAntlrParserFacade {
    fun parse(code: String): SandyAntlrParsingResult = parse(code.toStream())

    fun parse(file: File): SandyAntlrParsingResult = parse(FileInputStream(file))

    fun parse(inputStream: InputStream): SandyAntlrParsingResult {
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

        val lexer = SandyLexer(ANTLRInputStream(inputStream))
        lexer.removeErrorListeners()
        lexer.addErrorListener(errorListener)
        val parser = SandyParser(CommonTokenStream(lexer))
        parser.removeErrorListeners()
        parser.addErrorListener(errorListener)
        val root = parser.sandyFile()

        return SandyAntlrParsingResult(root, lexicalAndSyntacticErrors)
    }
}

data class ParsingResult(val root: SandyFile?, val errors: List<Error>) {
    fun isCorrect() = errors.isEmpty() && root != null
}

object SandyParser {
    fun parse(code: String): ParsingResult = parse(code.toStream())

    fun parse(file: File): ParsingResult = parse(FileInputStream(file))

    fun parse(inputStream: InputStream, considerPosition: Boolean = true): ParsingResult {
        val antlrParsingResult = SandyAntlrParserFacade.parse(inputStream)
        val lexicalAndSyntacticErrors = antlrParsingResult.errors
        val antlrRoot = antlrParsingResult.root
        val astRoot = antlrRoot?.toAst(considerPosition = considerPosition)
        val semanticErrors = astRoot?.validate() ?: emptyList()
        return ParsingResult(astRoot, lexicalAndSyntacticErrors + semanticErrors)
    }
}
