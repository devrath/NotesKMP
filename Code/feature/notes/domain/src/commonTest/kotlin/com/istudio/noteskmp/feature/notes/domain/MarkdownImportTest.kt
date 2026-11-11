package com.istudio.noteskmp.feature.notes.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.istudio.noteskmp.core.domain.Result
import com.istudio.noteskmp.core.domain.note.NewNote
import kotlin.test.Test

class MarkdownImportTest {

    private fun decode(name: String, text: String) = decodeImportedFile(name, text.encodeToByteArray())

    @Test
    fun `title is the file name and the body is unchanged`() {
        assertThat(decode("Ideas.md", "  **keep**  spacing\n\n\n")).isEqualTo(Result.Success(NewNote("Ideas", "  **keep**  spacing\n\n\n")))
    }

    @Test
    fun `utf8 byte order mark is removed`() {
        val bytes = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + "Hi".encodeToByteArray()

        assertThat(decodeImportedFile("a.md", bytes)).isEqualTo(Result.Success(NewNote("a", "Hi")))
    }

    @Test
    fun `a bom-like sequence later in the text is kept`() {
        assertThat((decode("a.md", "x﻿y") as Result.Success).data.body).isEqualTo("x﻿y")
    }

    @Test
    fun `windows and old mac line endings become newlines`() {
        assertThat((decode("a.txt", "a\r\nb\rc\n\r\n") as Result.Success).data.body).isEqualTo("a\nb\nc\n\n")
    }

    @Test
    fun `exactly 5 MB is accepted and one byte more is rejected`() {
        val limit = IMPORT_MAX_BYTES.toInt()
        assertThat(decodeImportedFile("a.md", ByteArray(limit) { 'a'.code.toByte() }) is Result.Success).isEqualTo(true)
        assertThat(decodeImportedFile("a.md", ByteArray(limit + 1) { 'a'.code.toByte() })).isEqualTo(Result.Error(ImportError.TOO_LARGE))
    }

    @Test
    fun `invalid utf8 and binary are not text`() {
        assertThat(decodeImportedFile("a.md", byteArrayOf(0xC3.toByte(), 0x28))).isEqualTo(Result.Error(ImportError.NOT_TEXT))
        assertThat(decodeImportedFile("a.md", "PK\u0000\u0003".encodeToByteArray())).isEqualTo(Result.Error(ImportError.NOT_TEXT))
        // UTF-16 with a BOM is full of NULs.
        assertThat(decodeImportedFile("a.md", byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 'a'.code.toByte(), 0))).isEqualTo(Result.Error(ImportError.NOT_TEXT))
    }

    @Test
    fun `empty file imports as an empty note`() {
        assertThat(decode("Empty.md", "")).isEqualTo(Result.Success(NewNote("Empty", "")))
    }

    @Test
    fun `emoji and rtl text decode intact`() {
        assertThat((decode("a.md", "👨‍👩‍👧 שלום") as Result.Success).data.body).isEqualTo("👨‍👩‍👧 שלום")
    }

    @Test
    fun `title drops only the last extension`() {
        assertThat(importTitle("notes.v2.markdown")).isEqualTo("notes.v2")
        assertThat(importTitle("README")).isEqualTo("README")
        assertThat(importTitle(".md")).isEqualTo("")
        assertThat(importTitle("Café ideas.txt")).isEqualTo("Café ideas")
    }
}
