package co.kenrg.kagelang.parser

import co.kenrg.kagelang.KageParser
import co.kenrg.kagelang.helper.parseCode
import co.kenrg.kagelang.helper.toParseTree
import org.junit.Test
import kotlin.test.assertEquals

class ParenExpressionTests {
    // TODO - Tests must use vars, but we're only interested in the paren operation. Remove when top-level expressions supported.
    fun firstParenOperation(kageFile: KageParser.KageFileContext): KageParser.ParenExpressionContext =
            (kageFile.line(0).statement() as KageParser.VarDeclarationStatementContext)
                    .varDeclaration().assignment().expression() as KageParser.ParenExpressionContext

    @Test fun parsesParenExpression_IntAddition() {
        val code = "var a = (1 + 2)"
        val binaryOperation = firstParenOperation(parseCode(code))
        val parseTreeStr = toParseTree(binaryOperation).multiLineString()
        assertEquals(
                """ParenExpression
                  |  Leaf[(]
                  |  BinaryOperation
                  |    IntLiteral
                  |      Leaf[1]
                  |    Leaf[+]
                  |    IntLiteral
                  |      Leaf[2]
                  |  Leaf[)]
                  |""".trimMargin("|"),
                parseTreeStr)
    }
}