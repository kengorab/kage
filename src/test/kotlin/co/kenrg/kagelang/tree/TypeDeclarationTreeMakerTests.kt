package co.kenrg.kagelang.tree

import co.kenrg.kagelang.kageFileFromCode
import co.kenrg.kagelang.kageFileFromLines
import co.kenrg.kagelang.model.TypedName
import co.kenrg.kagelang.tree.KGTree.KGTypeDeclaration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TypeDeclarationTreeMakerTests {

    @Test fun testParseTypeDeclaration_emptyDeclaration() {
        val kageFile = kageFileFromCode("type Empty")
        val expected = kageFileFromLines(KGTypeDeclaration("Empty"))
        assertEquals(expected, kageFile)
    }

    @Test fun testParseTypeDeclaration_oneProp() {
        val kageFile = kageFileFromCode("type OneProp { count: Int }")
        val expected = kageFileFromLines(KGTypeDeclaration("OneProp", listOf(TypedName("count", "Int"))))
        assertEquals(expected, kageFile)
    }

    @Test fun testParseTypeDeclaration_twoPropsSameLine() {
        val kageFile = kageFileFromCode("type TwoProps { count: Int, label: String }")
        val expected = kageFileFromLines(KGTypeDeclaration("TwoProps", listOf(TypedName("count", "Int"), TypedName("label", "String"))))
        assertEquals(expected, kageFile)
    }

    @Test fun testParseTypeDeclaration_twoPropsWithNewline() {
        val kageFile = kageFileFromCode("type TwoProps { count: Int,\nlabel: String }")
        val expected = kageFileFromLines(KGTypeDeclaration("TwoProps", listOf(TypedName("count", "Int"), TypedName("label", "String"))))
        assertEquals(expected, kageFile)
    }
}