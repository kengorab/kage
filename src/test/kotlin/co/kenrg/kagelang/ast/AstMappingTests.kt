package co.kenrg.kagelang.ast

import co.kenrg.kagelang.model.position
import co.kenrg.kagelang.parser.KageParserFacade
import co.kenrg.kagelang.tree.KGFile
import co.kenrg.kagelang.tree.KGTree.*
import co.kenrg.kagelang.tree.types.KGType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AstMappingTests {

    @Test fun mapSimpleFileWithPositions() {
        val code = """print(1 + 1)
                     |print(true && false)"""
                .trimMargin("|")
        val ast = KageParserFacade.parse(code).root
        val expectedAst = KGFile(listOf(
                KGPrint(
                        KGBinary(
                                KGLiteral(KGType.INT, 1).withPosition(position(1, 6, 1, 7)),
                                "+",
                                KGLiteral(KGType.INT, 1).withPosition(position(1, 10, 1, 11))
                        ).withPosition(position(1, 6, 1, 11))
                ).withPosition(position(1, 0, 1, 12)),
                KGPrint(
                        KGBinary(
                                KGLiteral(KGType.BOOL, true).withPosition(position(2, 6, 2, 10)),
                                "&&",
                                KGLiteral(KGType.BOOL, false).withPosition(position(2, 14, 2, 19))
                        ).withPosition(position(2, 6, 2, 19))
                ).withPosition(position(2, 0, 2, 20))
        ), mapOf())
        assertEquals(expectedAst, ast)
    }
}
