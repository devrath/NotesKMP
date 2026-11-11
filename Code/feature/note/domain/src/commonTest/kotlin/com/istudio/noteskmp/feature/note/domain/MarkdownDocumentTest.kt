package com.istudio.noteskmp.feature.note.domain

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.istudio.noteskmp.feature.note.domain.MarkdownInline.Bold
import com.istudio.noteskmp.feature.note.domain.MarkdownInline.Code
import com.istudio.noteskmp.feature.note.domain.MarkdownInline.Italic
import com.istudio.noteskmp.feature.note.domain.MarkdownInline.Link
import com.istudio.noteskmp.feature.note.domain.MarkdownInline.Strikethrough
import com.istudio.noteskmp.feature.note.domain.MarkdownInline.Text
import kotlin.test.Test

class MarkdownDocumentTest {

    @Test
    fun `headings keep their level and drop the markers`() {
        val blocks = parseMarkdown("# One\n\n## Two ##\n\n### Three\n\n###### Six")

        assertThat(blocks.map { (it as MarkdownBlock.Heading).level }).containsExactly(1, 2, 3, 6)
        assertThat((blocks[1] as MarkdownBlock.Heading).content).containsExactly(Text("Two"))
    }

    @Test
    fun `inline styles nest`() {
        val paragraph = parseMarkdown("Focus on **search** and *import* ~~tags~~ `code`").single() as MarkdownBlock.Paragraph

        assertThat(paragraph.content).containsExactly(
            Text("Focus on "),
            Bold(listOf(Text("search"))),
            Text(" and "),
            Italic(listOf(Text("import"))),
            Text(" "),
            Strikethrough(listOf(Text("tags"))),
            Text(" "),
            Code("code"),
        )
    }

    @Test
    fun `checklist items carry the offset of their box`() {
        val source = "- [x] Ship\n- [ ] Finish"
        val items = parseMarkdown(source).map { it as MarkdownBlock.ListItem }

        val done = items[0].marker as ListMarker.Task
        val open = items[1].marker as ListMarker.Task
        assertThat(done.isChecked).isTrue()
        assertThat(open.isChecked).isEqualTo(false)
        assertThat(source.substring(done.checkboxOffset, done.checkboxOffset + 3)).isEqualTo("[x]")
        assertThat(source.substring(open.checkboxOffset, open.checkboxOffset + 3)).isEqualTo("[ ]")
        assertThat(items[1].content).containsExactly(Text("Finish"))
    }

    @Test
    fun `nested and ordered lists keep depth and numbers`() {
        val items = parseMarkdown("1. Draft\n2. Review\n   - with team\n").map { it as MarkdownBlock.ListItem }

        assertThat(items.map { it.depth }).containsExactly(0, 0, 1)
        assertThat(items[0].marker).isEqualTo(ListMarker.Number("1."))
        assertThat(items[2].marker).isEqualTo(ListMarker.Bullet)
        assertThat(items[2].content).containsExactly(Text("with team"))
    }

    @Test
    fun `quotes raise the quote depth of their blocks`() {
        val block = parseMarkdown("> Writing comes first.").single()

        assertThat(block).isInstanceOf<MarkdownBlock.Paragraph>()
        assertThat(block.quoteDepth).isEqualTo(1)
    }

    @Test
    fun `fenced code keeps its text and never parses markdown inside`() {
        val block = parseMarkdown("```kotlin\nval a = **b**\nval c = 1\n```").single() as MarkdownBlock.CodeBlock

        assertThat(block.code).isEqualTo("val a = **b**\nval c = 1")
    }

    @Test
    fun `safe links open and unsafe links stay text`() {
        val paragraph = parseMarkdown(
            "[spec](https://spec.commonmark.org) [x](javascript:alert(1)) <mailto:a@b.c> https://example.com",
        ).single() as MarkdownBlock.Paragraph

        val links = paragraph.content.filterIsInstance<Link>()
        assertThat(links.map { it.url }).containsExactly("https://spec.commonmark.org", "mailto:a@b.c", "https://example.com")
        assertThat(paragraph.content.plainText().contains("javascript")).isEqualTo(false)
    }

    @Test
    fun `raw html is literal text`() {
        val blocks = parseMarkdown("<script>alert(1)</script>\n\nHi <b>there</b>")

        assertThat(blocks.flatMap { (it as MarkdownBlock.Paragraph).content }.plainText())
            .isEqualTo("<script>alert(1)</script>Hi <b>there</b>")
    }

    @Test
    fun `escaped markers show as plain characters`() {
        val paragraph = parseMarkdown("\\*not italic\\*").single() as MarkdownBlock.Paragraph

        assertThat(paragraph.content.plainText()).isEqualTo("*not italic*")
    }

    @Test
    fun `empty note has no blocks`() {
        assertThat(parseMarkdown("")).hasSize(0)
    }

    @Test
    fun `block lookup finds the block containing an offset`() {
        val source = "# A\n\nfirst\n\nsecond match"
        val blocks = parseMarkdown(source)

        assertThat(blocks.indexOfBlockAt(source.indexOf("match"))).isEqualTo(2)
        assertThat(blocks.indexOfBlockAt(0)).isEqualTo(0)
    }
}
