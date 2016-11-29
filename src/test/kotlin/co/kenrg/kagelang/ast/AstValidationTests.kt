package co.kenrg.kagelang.ast

import co.kenrg.kagelang.parser.KageParserFacade
import org.junit.Test
import kotlin.test.assertEquals

class AstValidationTests {

    @Test fun duplicateVar() {
        val errors = KageParserFacade.parse(
                """var a = 1
                  |var a = 2""".trimMargin("|")
        ).errors

        assertEquals(
                listOf(Error("A variable named 'a' has already been declared", Point(2, 0))),
                errors
        )
    }

    @Test fun nonexistentVarReference() {
        val errors = KageParserFacade.parse("var a = b + 2").errors
        assertEquals(listOf(Error("There is no variable named 'b'", Point(1, 8))), errors)
    }

    @Test fun varReferenceBeforeDeclaration() {
        val errors = KageParserFacade.parse(
                """var a = b + 2
                  |var b = 2""".trimMargin("|")
        ).errors
        assertEquals(listOf(Error("You cannot refer to variable 'b' before its declaration", Point(1, 8))), errors)
    }

    @Test fun nonexistentVarAssignment() {
        val errors = KageParserFacade.parse("a = 3").errors
        assertEquals(listOf(Error("There is no variable named 'a'", Point(1, 0))), errors)
    }

    @Test fun varAssignmentBeforeDeclaration() {
        val errors = KageParserFacade.parse(
                """a = 1
                  |var a =2""".trimMargin("|")
        ).errors
        assertEquals(listOf(Error("You cannot refer to variable 'a' before its declaration", Point(1, 0))), errors)
    }
}

