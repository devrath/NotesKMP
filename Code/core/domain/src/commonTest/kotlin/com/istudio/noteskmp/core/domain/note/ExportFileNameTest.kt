package com.istudio.noteskmp.core.domain.note

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class ExportFileNameTest {

    @Test
    fun `plain title becomes title dot md`() {
        assertThat(exportFileName("Weekly plan", "Untitled")).isEqualTo("Weekly plan.md")
    }

    @Test
    fun `every illegal character becomes a dash`() {
        assertThat(exportFileName("a/b\\c:d*e?f\"g<h>i|j", "Untitled")).isEqualTo("a-b-c-d-e-f-g-h-i-j.md")
    }

    @Test
    fun `control characters become dashes`() {
        assertThat(exportFileName("tab\there\u0000nul\u007F", "Untitled")).isEqualTo("tab-here-nul-.md")
    }

    @Test
    fun `name is trimmed to 100 characters before the extension`() {
        val name = exportFileName("x".repeat(150), "Untitled")

        assertThat(name).isEqualTo("x".repeat(100) + ".md")
    }

    @Test
    fun `trimming never splits an emoji`() {
        val name = exportFileName("a".repeat(99) + "😀", "Untitled")

        assertThat(name).isEqualTo("a".repeat(99) + ".md")
    }

    @Test
    fun `blank or dot-only names fall back to untitled`() {
        assertThat(exportFileName("   ", "Untitled")).isEqualTo("Untitled.md")
        assertThat(exportFileName("..", "Untitled")).isEqualTo("Untitled.md")
    }

    @Test
    fun `unicode and rtl titles are kept`() {
        assertThat(exportFileName("Café — שלום", "Untitled")).isEqualTo("Café — שלום.md")
    }

    @Test
    fun `export content normalizes line endings only`() {
        assertThat(exportContent("a\r\nb\rc\n  d  ")).isEqualTo("a\nb\nc\n  d  ")
        assertThat(exportContent("**unchanged**")).isEqualTo("**unchanged**")
    }
}
