package com.example.watchdiary

import com.example.watchdiary.data.model.ContentType
import com.example.watchdiary.util.ImportParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ImportParserTest {
    @Test
    fun exportedJsonCanBeImportedWithoutKeepingOldIds() {
        val json = """[{"id":42,"title":"Атака титанов","contentType":"ANIME","watchDate":"2025-09-28","rating":5,"comment":"Отлично"}]"""
        val entry = ImportParser.parseJson(json).single()
        assertEquals(0, entry.id)
        assertEquals(ContentType.ANIME, entry.contentType)
        assertEquals("Атака титанов", entry.title)
        assertEquals("Отлично", entry.comment)
    }

    @Test
    fun invalidRatingFailsEntireImport() {
        val json = """[{"title":"Example","contentType":"MOVIE","watchDate":"2024-01-01","rating":8,"comment":""}]"""
        assertThrows(IllegalArgumentException::class.java) { ImportParser.parseJson(json) }
    }

    @Test
    fun textImportPreservesSeparatorsInsideComment() {
        val input = "Example | MOVIE | 2024-02-01 | 4 | first | second"
        val entry = ImportParser.parse(input).single()
        assertEquals("first | second", entry.comment)
    }

    @Test
    fun textImportRejectsInvalidRatingsAndBlankTitles() {
        val input = "Example | MOVIE | 2024-02-01 | 9 | bad\n" +
            " | ANIME | 2024-02-02 | 5 | no title\n" +
            "Valid | SERIES | 2024-02-03 | 3 | good"
        assertEquals(listOf("Valid"), ImportParser.parse(input).map { it.title })
    }

    @Test
    fun textImportReportsInvalidLinesWithoutLosingValidOnes() {
        val input = "Valid | MOVIE | 2024-02-01 | 5 | comment\n" +
            "Broken | SERIES | not-a-date | 4 | comment\n" +
            "Another | ANIME | 2024-02-03 | 3 |"
        val result = ImportParser.parseWithReport(input)
        assertEquals(2, result.entries.size)
        assertEquals(1, result.invalidLines)
    }

    @Test
    fun textImportIgnoresEmptyLinesWhenCountingInvalidRows() {
        val result = ImportParser.parseWithReport("\n\nBroken line\n  \n")
        assertEquals(0, result.entries.size)
        assertEquals(1, result.invalidLines)
    }

    @Test
    fun jsonImportRejectsInvalidTypeAndPreservesPipesInComments() {
        val valid = """[{"title":"Movie","contentType":"MOVIE","watchDate":"2024-01-01","rating":5,"comment":"one | two"}]"""
        assertEquals("one | two", ImportParser.parseJson(valid).single().comment)
        val invalid = """[{"title":"Movie","contentType":"UNKNOWN","watchDate":"2024-01-01","rating":5}]"""
        assertThrows(IllegalArgumentException::class.java) { ImportParser.parseJson(invalid) }
    }

    @Test
    fun malformedJsonIsRejected() {
        assertThrows(Exception::class.java) { ImportParser.parseJson("{not an array}") }
    }
}
