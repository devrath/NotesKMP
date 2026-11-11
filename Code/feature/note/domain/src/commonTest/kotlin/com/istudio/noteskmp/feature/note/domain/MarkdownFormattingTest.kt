package com.istudio.noteskmp.feature.note.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.istudio.noteskmp.feature.note.domain.MarkdownFormat.Bold
import com.istudio.noteskmp.feature.note.domain.MarkdownFormat.BulletedList
import com.istudio.noteskmp.feature.note.domain.MarkdownFormat.Checklist
import com.istudio.noteskmp.feature.note.domain.MarkdownFormat.CodeBlock
import com.istudio.noteskmp.feature.note.domain.MarkdownFormat.Heading
import com.istudio.noteskmp.feature.note.domain.MarkdownFormat.InlineCode
import com.istudio.noteskmp.feature.note.domain.MarkdownFormat.Italic
import com.istudio.noteskmp.feature.note.domain.MarkdownFormat.Link
import com.istudio.noteskmp.feature.note.domain.MarkdownFormat.NumberedList
import com.istudio.noteskmp.feature.note.domain.MarkdownFormat.Quote
import kotlin.test.Test

class MarkdownFormattingTest {

    private fun format(text: String, start: Int, end: Int = start, format: MarkdownFormat, clipboard: String? = null) =
        applyFormat(EditorValue(text, start, end), format, clipboard)

    // ---- Inline ----

    @Test
    fun `bold wraps the selection and keeps it selected`() {
        assertThat(format("make bold", 5, 9, Bold)).isEqualTo(EditorValue("make **bold**", 7, 11))
    }

    @Test
    fun `bold again unwraps - whether markers are outside or inside the selection`() {
        assertThat(format("make **bold**", 7, 11, Bold)).isEqualTo(EditorValue("make bold", 5, 9))
        assertThat(format("make **bold**", 5, 13, Bold)).isEqualTo(EditorValue("make bold", 5, 9))
    }

    @Test
    fun `no selection inserts the pair with the cursor between`() {
        assertThat(format("ab", 1, format = Bold)).isEqualTo(EditorValue("a****b", 3))
        assertThat(format("", 0, format = InlineCode)).isEqualTo(EditorValue("``", 1))
    }

    @Test
    fun `italic inside bold adds a level instead of unwrapping bold`() {
        assertThat(format("**word**", 2, 6, Italic).text).isEqualTo("***word***")
    }

    @Test
    fun `italic and bold come off bold-italic independently`() {
        assertThat(format("***word***", 3, 7, Italic).text).isEqualTo("**word**")
        assertThat(format("***word***", 3, 7, Bold).text).isEqualTo("*word*")
    }

    @Test
    fun `italic unwraps single stars`() {
        assertThat(format("*word*", 1, 5, Italic)).isEqualTo(EditorValue("word", 0, 4))
    }

    @Test
    fun `selection reversed by the user still wraps`() {
        assertThat(format("make bold", 9, 5, Bold).text).isEqualTo("make **bold**")
    }

    @Test
    fun `wrapping keeps emoji intact`() {
        assertThat(format("hi 👍🏽", 3, 7, Bold).text).isEqualTo("hi **👍🏽**")
    }

    // ---- Lines ----

    @Test
    fun `bullet applies to every touched line and toggles off`() {
        val on = format("one\ntwo\nthree", 1, 5, BulletedList)
        assertThat(on.text).isEqualTo("- one\n- two\nthree")

        assertThat(format(on.text, 2, 8, BulletedList).text).isEqualTo("one\ntwo\nthree")
    }

    @Test
    fun `selection ending right after a newline does not touch the next line`() {
        assertThat(format("one\ntwo", 0, 4, BulletedList).text).isEqualTo("- one\ntwo")
    }

    @Test
    fun `numbered list counts up`() {
        assertThat(format("a\nb\nc", 0, 5, NumberedList).text).isEqualTo("1. a\n2. b\n3. c")
    }

    @Test
    fun `switching list kinds replaces the marker`() {
        assertThat(format("- a", 0, format = NumberedList).text).isEqualTo("1. a")
        assertThat(format("1. a", 0, format = Checklist).text).isEqualTo("- [ ] a")
        assertThat(format("- [x] a", 0, format = BulletedList).text).isEqualTo("- a")
    }

    @Test
    fun `checklist toggles off ticked items too`() {
        assertThat(format("- [x] done", 3, format = Checklist).text).isEqualTo("done")
    }

    @Test
    fun `list marker keeps indentation`() {
        assertThat(format("  nested", 4, format = BulletedList).text).isEqualTo("  - nested")
    }

    @Test
    fun `cursor stays on the same text after a marker is added`() {
        assertThat(format("abc", 1, format = BulletedList)).isEqualTo(EditorValue("- abc", 3))
        assertThat(format("", 0, format = Checklist)).isEqualTo(EditorValue("- [ ] ", 6))
    }

    @Test
    fun `quote toggles on and off`() {
        val quoted = format("a\nb", 0, 3, Quote)
        assertThat(quoted.text).isEqualTo("> a\n> b")
        assertThat(format(quoted.text, 0, 7, Quote).text).isEqualTo("a\nb")
    }

    @Test
    fun `heading cycles through three levels and off`() {
        var value = EditorValue("Title", 0)
        val seen = mutableListOf<String>()
        repeat(4) {
            value = applyFormat(value, Heading)
            seen += value.text
        }
        assertThat(seen).isEqualTo(listOf("# Title", "## Title", "### Title", "Title"))
    }

    @Test
    fun `deeper headings go straight to none`() {
        assertThat(format("#### Deep", 0, format = Heading).text).isEqualTo("Deep")
    }

    // ---- Blocks ----

    @Test
    fun `code block with no selection puts the cursor inside - on its own lines`() {
        val result = format("ab", 1, format = CodeBlock)

        assertThat(result.text).isEqualTo("a\n```\n\n```\nb")
        assertThat(result.selectionStart).isEqualTo(6)
    }

    @Test
    fun `code block fences the selected lines`() {
        assertThat(format("x\nval a = 1\nval b = 2\ny", 3, 15, CodeBlock).text).isEqualTo("x\n```\nval a = 1\nval b = 2\n```\ny")
    }

    @Test
    fun `link wraps the selection with the cursor in the url`() {
        assertThat(format("see spec", 4, 8, Link)).isEqualTo(EditorValue("see [spec]()", 11))
    }

    @Test
    fun `link without selection leaves the cursor in the url`() {
        assertThat(format("", 0, format = Link)).isEqualTo(EditorValue("[]()", 3))
    }

    @Test
    fun `a url on the clipboard becomes the target`() {
        assertThat(format("spec", 0, 4, Link, clipboard = " https://commonmark.org ")).isEqualTo(EditorValue("[spec](https://commonmark.org)", 29))
        // No label yet, so the cursor goes where the label is typed.
        assertThat(format("", 0, format = Link, clipboard = "https://a.b")).isEqualTo(EditorValue("[](https://a.b)", 1))
    }

    @Test
    fun `clipboard text that isn't a safe url is ignored`() {
        assertThat(format("", 0, format = Link, clipboard = "javascript:alert(1)").text).isEqualTo("[]()")
        assertThat(format("", 0, format = Link, clipboard = "just words").text).isEqualTo("[]()")
    }

    @Test
    fun `only http - https and mailto are safe`() {
        assertThat(isSafeLinkUrl("HTTPS://x.org")).isTrue()
        assertThat(isSafeLinkUrl("mailto:a@b.c")).isTrue()
        assertThat(isSafeLinkUrl("javascript:alert(1)")).isFalse()
        assertThat(isSafeLinkUrl("file:///etc/passwd")).isFalse()
        assertThat(isSafeLinkUrl("relative/path")).isFalse()
    }
}
