package com.istudio.noteskmp.core.domain.note

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test

class MarkdownStrippingTest {

    @Test
    fun `block markers are removed`() {
        assertThat(stripMarkdownLine("## This week ##")).isEqualTo("This week")
        assertThat(stripMarkdownLine("> > quoted")).isEqualTo("quoted")
        assertThat(stripMarkdownLine("  - [x] Ship it")).isEqualTo("Ship it")
        assertThat(stripMarkdownLine("12. Twelfth")).isEqualTo("Twelfth")
        assertThat(stripMarkdownLine("> - [ ] quoted task")).isEqualTo("quoted task")
    }

    @Test
    fun `inline markers are removed but the text stays`() {
        assertThat(stripMarkdownLine("Focus on **search** and *import* and __more__ ~~tags~~"))
            .isEqualTo("Focus on search and import and more tags")
        assertThat(stripMarkdownLine("Run `notes.search()` now")).isEqualTo("Run notes.search() now")
        assertThat(stripMarkdownLine("See [the spec](https://x.org) or ![logo](a.png)")).isEqualTo("See the spec or logo")
        assertThat(stripMarkdownLine("Mail <mailto:a@b.c>")).isEqualTo("Mail mailto:a@b.c")
    }

    @Test
    fun `snake case and lone symbols survive`() {
        assertThat(stripMarkdownLine("use snake_case_names")).isEqualTo("use snake_case_names")
        assertThat(stripMarkdownLine("5 * 3 = 15")).isEqualTo("5 * 3 = 15")
        assertThat(stripMarkdownLine("#hashtag is not a heading")).isEqualTo("#hashtag is not a heading")
    }

    @Test
    fun `escaped markers become plain characters`() {
        assertThat(stripMarkdownLine("\\*not italic\\*")).isEqualTo("*not italic*")
    }

    @Test
    fun `fences and rules become empty`() {
        assertThat(stripMarkdownLine("```kotlin")).isEqualTo("")
        assertThat(stripMarkdownLine("---")).isEqualTo("")
        assertThat(stripMarkdownLine("* * *")).isEqualTo("")
    }

    @Test
    fun `fallback title is the first non-empty stripped line and is left out of the preview`() {
        val summary = summarizeNoteText("", "\n\n# Groceries for the weekend\n\nLemons, **oat milk**\n- sourdough")

        assertThat(summary.fallbackTitle).isEqualTo("Groceries for the weekend")
        assertThat(summary.preview).isEqualTo("Lemons, oat milk sourdough")
    }

    @Test
    fun `typed title means no fallback and the whole body previews`() {
        val summary = summarizeNoteText("Ideas", "First line\nSecond")

        assertThat(summary.fallbackTitle).isNull()
        assertThat(summary.preview).isEqualTo("First line Second")
    }

    @Test
    fun `whitespace title counts as no title`() {
        assertThat(summarizeNoteText("   ", "Body").fallbackTitle).isEqualTo("Body")
    }

    @Test
    fun `body of only markers gives no fallback`() {
        val summary = summarizeNoteText("", "---\n```\n```\n#")

        assertThat(summary.fallbackTitle).isNull()
        assertThat(summary.preview).isEqualTo("")
    }

    @Test
    fun `preview is capped and never splits an emoji`() {
        val body = "a".repeat(PREVIEW_MAX_LENGTH - 1) + "👍🏽 tail"
        val preview = summarizeNoteText("T", body).preview

        assertThat(preview.length <= PREVIEW_MAX_LENGTH).isTrue()
        assertThat(preview.last().isHighSurrogate()).isEqualTo(false)
    }

    @Test
    fun `huge bodies stop reading once the preview is full`() {
        val body = "word ".repeat(1_000_000)

        assertThat(summarizeNoteText("T", body).preview.length).isEqualTo(PREVIEW_MAX_LENGTH)
    }

    @Test
    fun `title picks typed - then fallback - then untitled`() {
        assertThat(noteTitle(" Plan ", "x")).isEqualTo(NoteTitle.Typed("Plan"))
        assertThat(noteTitle("", "First line")).isEqualTo(NoteTitle.Fallback("First line"))
        assertThat(noteTitle("", null)).isInstanceOf<NoteTitle.Untitled>()
        assertThat(noteTitle("", "  ")).isInstanceOf<NoteTitle.Untitled>()
    }

    @Test
    fun `whole body strips line by line and keeps line breaks`() {
        assertThat(stripMarkdown("# A\n**b**\n- c")).isEqualTo("A\nb\nc")
    }
}
