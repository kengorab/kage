package co.kenrg.kagelang.tree

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TypeDeclarationTreeMakerTests {

    @Test fun testParseTypeDeclaration_emptyDeclaration() {
        val kageFile = kageFileFromCode("type Empty")
        val expected = kageFileFromLines(KGTree.KGTypeDeclaration("Empty"))
        assertEquals(expected, kageFile)
    }
}