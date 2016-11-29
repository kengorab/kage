package co.kenrg.kagelang.parser

import co.kenrg.kagelang.KageParser
import co.kenrg.kagelang.helper.parseCode
import co.kenrg.kagelang.helper.toParseTree
import org.junit.Test
import kotlin.test.assertEquals

class BinaryOperationExpressionTests {
    // TODO - Tests must use vars, but we're only interested in the binary operation. Remove when top-level expressions supported.
    fun firstBinaryOperation(kageFile: KageParser.KageFileContext): KageParser.BinaryOperationContext =
            (kageFile.line(0).statement() as KageParser.VarDeclarationStatementContext)
                    .varDeclaration().assignment().expression() as KageParser.BinaryOperationContext

    @Test fun parsesBinaryOperation_IntAddition() {
        val code = "var a = 1 + 2"
        val binaryOperation = firstBinaryOperation(parseCode(code))
        val parseTreeStr = toParseTree(binaryOperation).multiLineString()
        assertEquals(
                """BinaryOperation
                  |  IntLiteral
                  |    Leaf[1]
                  |  Leaf[+]
                  |  IntLiteral
                  |    Leaf[2]
                  |""".trimMargin("|"),
                parseTreeStr)
    }

    @Test fun parsesBinaryOperation_DecimalAddition() {
        val code = "var a = 1.1 + 2.2"
        val binaryOperation = firstBinaryOperation(parseCode(code))
        val parseTreeStr = toParseTree(binaryOperation).multiLineString()
        assertEquals(
                """BinaryOperation
                  |  DecLiteral
                  |    Leaf[1.1]
                  |  Leaf[+]
                  |  DecLiteral
                  |    Leaf[2.2]
                  |""".trimMargin("|"),
                parseTreeStr)
    }
}