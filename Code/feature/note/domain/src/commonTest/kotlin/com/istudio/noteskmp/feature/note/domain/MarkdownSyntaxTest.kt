package com.istudio.noteskmp.feature.note.domain

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEmpty
import assertk.assertions.isTrue
import kotlin.test.Test

class MarkdownSyntaxTest {

    private fun spansOf(text: String, kind: SyntaxKind) =
        markdownSyntaxSpans(text).filter { it.kind == kind }.map { text.substring(it.start, it.end) }

    @Test
    fun `heading markers are muted and the line is a heading`() {
        assertThat(spansOf("## Week", SyntaxKind.Marker)).contains("##")
        assertThat(spansOf("## Week", SyntaxKind.Heading2)).contains("## Week")
    }

    @Test
    fun `list - checklist and quote markers are muted`() {
        val markers = spansOf("- [x] done\n> quote\n12. twelve", SyntaxKind.Marker)

        assertThat(markers).contains("- [x]")
        assertThat(markers).contains(">")
        assertThat(markers).contains("12.")
    }

    @Test
    fun `emphasis gets style and muted markers`() {
        val text = "a **bold** and *it* ~~gone~~"

        assertThat(spansOf(text, SyntaxKind.Bold)).contains("**bold**")
        assertThat(spansOf(text, SyntaxKind.Italic)).contains("*it*")
        assertThat(spansOf(text, SyntaxKind.Strikethrough)).contains("~~gone~~")
        assertThat(spansOf(text, SyntaxKind.Marker)).contains("**")
    }

    @Test
    fun `code is monospace and markdown inside it is not styled`() {
        val text = "run `a **b**`\n```\n**not bold**\n```"

        assertThat(spansOf(text, SyntaxKind.Code)).contains("`a **b**`")
        assertThat(spansOf(text, SyntaxKind.Code)).contains("**not bold**")
        assertThat(spansOf(text, SyntaxKind.Bold)).isEmpty()
    }

    @Test
    fun `links show text in link color and url muted`() {
        val text = "see [spec](https://x.org) or https://y.org"

        assertThat(spansOf(text, SyntaxKind.LinkText)).contains("spec")
        assertThat(spansOf(text, SyntaxKind.LinkUrl)).contains("https://x.org")
        assertThat(spansOf(text, SyntaxKind.LinkText)).contains("https://y.org")
    }

    @Test
    fun `snake case and lone stars stay plain`() {
        assertThat(spansOf("snake_case_name and 5 * 3", SyntaxKind.Italic)).isEmpty()
    }

    @Test
    fun `spans stay inside the text`() {
        val text = "# A\n- [ ] b **c**\n`d`\n"
        assertThat(markdownSyntaxSpans(text).all { it.start >= 0 && it.end <= text.length && it.start <= it.end }).isTrue()
    }

    @Test
    fun `very large notes skip highlighting`() {
        assertThat(markdownSyntaxSpans("**a** ".repeat(SYNTAX_HIGHLIGHT_MAX_LENGTH))).isEmpty()
    }
}
