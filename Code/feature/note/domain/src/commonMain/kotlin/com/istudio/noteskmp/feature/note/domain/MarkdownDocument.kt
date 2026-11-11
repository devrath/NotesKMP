package com.istudio.noteskmp.feature.note.domain

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.parser.MarkdownParser

/** Inline content of a block. */
sealed interface MarkdownInline {
    data class Text(val text: String) : MarkdownInline
    data class Bold(val children: List<MarkdownInline>) : MarkdownInline
    data class Italic(val children: List<MarkdownInline>) : MarkdownInline
    data class Strikethrough(val children: List<MarkdownInline>) : MarkdownInline
    data class Code(val code: String) : MarkdownInline

    /** Only created for `http`, `https` and `mailto` targets; other links stay text. */
    data class Link(val url: String, val children: List<MarkdownInline>) : MarkdownInline
    data object LineBreak : MarkdownInline
}

/**
 * One block of the preview, flattened so the preview can render it in a lazy list.
 * [sourceStart] is where it begins in the note's Markdown, used to map taps and scrolling
 * back to the text. [quoteDepth] is how many quotes contain it.
 */
sealed interface MarkdownBlock {
    val sourceStart: Int
    val quoteDepth: Int

    data class Heading(
        val level: Int,
        val content: List<MarkdownInline>,
        override val sourceStart: Int,
        override val quoteDepth: Int,
    ) : MarkdownBlock

    data class Paragraph(
        val content: List<MarkdownInline>,
        override val sourceStart: Int,
        override val quoteDepth: Int,
    ) : MarkdownBlock

    /**
     * A list item's first paragraph. [depth] is the nesting level (0 for top level).
     * [checkboxOffset] is where `[ ]` / `[x]` starts in the source, for checklist items.
     */
    data class ListItem(
        val depth: Int,
        val marker: ListMarker,
        val content: List<MarkdownInline>,
        override val sourceStart: Int,
        override val quoteDepth: Int,
    ) : MarkdownBlock

    /** Code shown as is, scrolling sideways instead of wrapping. */
    data class CodeBlock(
        val code: String,
        override val sourceStart: Int,
        override val quoteDepth: Int,
    ) : MarkdownBlock

    data class ThematicBreak(
        override val sourceStart: Int,
        override val quoteDepth: Int,
    ) : MarkdownBlock
}

sealed interface ListMarker {
    data object Bullet : ListMarker
    data class Number(val number: String) : ListMarker
    data class Task(val isChecked: Boolean, val checkboxOffset: Int) : ListMarker
}

/**
 * Parses [markdown] (CommonMark plus the GitHub extensions the app supports) into blocks
 * for the preview. Raw HTML stays literal text and is never interpreted. Link definitions
 * and images, which the app doesn't support, show their text.
 */
fun parseMarkdown(markdown: String): List<MarkdownBlock> {
    val flavour = GFMFlavourDescriptor()
    val root = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)
    return BlockBuilder(markdown).apply { blocks(root.children, quoteDepth = 0, listDepth = -1) }.result
}

/** The block that contains [offset] in the source, for scrolling to a search match. */
fun List<MarkdownBlock>.indexOfBlockAt(offset: Int): Int {
    val index = indexOfLast { it.sourceStart <= offset }
    return if (index < 0) 0 else index
}

private class BlockBuilder(private val source: String) {
    val result = mutableListOf<MarkdownBlock>()

    fun blocks(nodes: List<ASTNode>, quoteDepth: Int, listDepth: Int) {
        for (node in nodes) block(node, quoteDepth, listDepth)
    }

    private fun block(node: ASTNode, quoteDepth: Int, listDepth: Int) {
        when (val type = node.type) {
            MarkdownElementTypes.PARAGRAPH ->
                result += MarkdownBlock.Paragraph(inlines(node.children), node.startOffset, quoteDepth)

            MarkdownElementTypes.ATX_1, MarkdownElementTypes.ATX_2, MarkdownElementTypes.ATX_3,
            MarkdownElementTypes.ATX_4, MarkdownElementTypes.ATX_5, MarkdownElementTypes.ATX_6,
            -> {
                val content = node.children.firstOrNull { it.type == MarkdownTokenTypes.ATX_CONTENT }
                result += MarkdownBlock.Heading(
                    level = headingLevel(type),
                    content = content?.let { inlines(it.children).trimmed() }.orEmpty(),
                    sourceStart = node.startOffset,
                    quoteDepth = quoteDepth,
                )
            }

            MarkdownElementTypes.SETEXT_1, MarkdownElementTypes.SETEXT_2 -> {
                val content = node.children.firstOrNull { it.type == MarkdownTokenTypes.SETEXT_CONTENT }
                result += MarkdownBlock.Heading(
                    level = if (type == MarkdownElementTypes.SETEXT_1) 1 else 2,
                    content = content?.let { inlines(it.children).trimmed() }.orEmpty(),
                    sourceStart = node.startOffset,
                    quoteDepth = quoteDepth,
                )
            }

            MarkdownElementTypes.BLOCK_QUOTE -> blocks(node.children, quoteDepth + 1, listDepth)

            MarkdownElementTypes.UNORDERED_LIST, MarkdownElementTypes.ORDERED_LIST ->
                node.children.filter { it.type == MarkdownElementTypes.LIST_ITEM }
                    .forEach { listItem(it, quoteDepth, listDepth + 1) }

            MarkdownElementTypes.CODE_FENCE -> result += MarkdownBlock.CodeBlock(
                code = node.children
                    .filter { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT || it.type == MarkdownTokenTypes.EOL }
                    .dropWhile { it.type == MarkdownTokenTypes.EOL }
                    .joinToString("") { it.text() }
                    .trimEnd('\n'),
                sourceStart = node.startOffset,
                quoteDepth = quoteDepth,
            )

            MarkdownElementTypes.CODE_BLOCK -> result += MarkdownBlock.CodeBlock(
                code = node.children.joinToString("") { it.text() }.trimEnd('\n'),
                sourceStart = node.startOffset,
                quoteDepth = quoteDepth,
            )

            MarkdownTokenTypes.HORIZONTAL_RULE ->
                result += MarkdownBlock.ThematicBreak(node.startOffset, quoteDepth)

            // Raw HTML and tables are shown as their literal text, never rendered.
            MarkdownElementTypes.HTML_BLOCK, GFMElementTypes.TABLE, MarkdownElementTypes.LINK_DEFINITION ->
                result += MarkdownBlock.Paragraph(
                    listOf(MarkdownInline.Text(node.text().trimEnd('\n'))),
                    node.startOffset,
                    quoteDepth,
                )

            else -> Unit
        }
    }

    private fun listItem(node: ASTNode, quoteDepth: Int, depth: Int) {
        val checkbox = node.children.firstOrNull { it.type == GFMTokenTypes.CHECK_BOX }
        val numberNode = node.children.firstOrNull { it.type == MarkdownTokenTypes.LIST_NUMBER }
        val marker = when {
            checkbox != null -> {
                val box = checkbox.text()
                val bracket = box.indexOf('[')
                ListMarker.Task(
                    isChecked = box.getOrNull(bracket + 1)?.let { it == 'x' || it == 'X' } == true,
                    checkboxOffset = checkbox.startOffset + bracket,
                )
            }
            numberNode != null -> ListMarker.Number(numberNode.text().trim())
            else -> ListMarker.Bullet
        }
        val content = node.children.filterNot {
            it.type == GFMTokenTypes.CHECK_BOX || it.type == MarkdownTokenTypes.LIST_NUMBER ||
                it.type == MarkdownTokenTypes.LIST_BULLET || it.type == MarkdownTokenTypes.EOL ||
                it.type == MarkdownTokenTypes.WHITE_SPACE
        }
        val first = content.firstOrNull()
        val firstInlines = if (first?.type == MarkdownElementTypes.PARAGRAPH) inlines(first.children) else emptyList()
        result += MarkdownBlock.ListItem(depth, marker, firstInlines, node.startOffset, quoteDepth)
        val rest = if (first?.type == MarkdownElementTypes.PARAGRAPH) content.drop(1) else content
        // Further paragraphs and nested lists of the item.
        for (child in rest) {
            if (child.type == MarkdownElementTypes.PARAGRAPH) {
                result += MarkdownBlock.Paragraph(inlines(child.children), child.startOffset, quoteDepth)
            } else {
                block(child, quoteDepth, depth)
            }
        }
    }

    // ---- Inlines ----

    private fun inlines(nodes: List<ASTNode>): List<MarkdownInline> {
        val out = mutableListOf<MarkdownInline>()
        for (node in nodes) inline(node, out)
        return out.mergeText()
    }

    private fun inline(node: ASTNode, out: MutableList<MarkdownInline>) {
        when (node.type) {
            MarkdownElementTypes.STRONG -> out += MarkdownInline.Bold(inlines(node.children.withoutDelimiters()))
            MarkdownElementTypes.EMPH -> out += MarkdownInline.Italic(inlines(node.children.withoutDelimiters()))
            GFMElementTypes.STRIKETHROUGH ->
                out += MarkdownInline.Strikethrough(inlines(node.children.filterNot { it.type == GFMTokenTypes.TILDE }))
            MarkdownElementTypes.CODE_SPAN -> {
                val code = node.children.filterNot { it.type == MarkdownTokenTypes.BACKTICK }
                    .joinToString("") { it.text() }
                out += MarkdownInline.Code(code.trimSingleSpace())
            }
            MarkdownElementTypes.INLINE_LINK -> {
                val label = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT }
                val labelInlines = label?.let { inlines(it.children.withoutBrackets()) }.orEmpty()
                val destination = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_DESTINATION }
                    ?.text()?.trim()?.removeSurrounding("<", ">")
                out += if (destination != null && isSafeLinkUrl(destination)) {
                    MarkdownInline.Link(destination, labelInlines.ifEmpty { listOf(MarkdownInline.Text(destination)) })
                } else {
                    // Unsafe or missing targets: keep the visible label only.
                    MarkdownInline.Text(labelInlines.plainText())
                }
            }
            MarkdownElementTypes.AUTOLINK -> {
                val url = node.text().removeSurrounding("<", ">")
                out += if (isSafeLinkUrl(url)) MarkdownInline.Link(url, listOf(MarkdownInline.Text(url))) else MarkdownInline.Text(node.text())
            }
            GFMTokenTypes.GFM_AUTOLINK -> {
                val url = node.text()
                out += if (isSafeLinkUrl(url)) MarkdownInline.Link(url, listOf(MarkdownInline.Text(url))) else MarkdownInline.Text(url)
            }
            MarkdownElementTypes.IMAGE -> {
                val label = node.children.firstOrNull { it.type == MarkdownElementTypes.INLINE_LINK }
                    ?.children?.firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT }
                out += MarkdownInline.Text(label?.let { inlines(it.children.withoutBrackets()).plainText() }.orEmpty())
            }
            MarkdownElementTypes.FULL_REFERENCE_LINK, MarkdownElementTypes.SHORT_REFERENCE_LINK -> {
                val label = node.children.firstOrNull { it.type == MarkdownElementTypes.LINK_TEXT || it.type == MarkdownElementTypes.LINK_LABEL }
                out += MarkdownInline.Text(label?.let { inlines(it.children.withoutBrackets()).plainText() } ?: node.text())
            }
            MarkdownTokenTypes.HARD_LINE_BREAK -> out += MarkdownInline.LineBreak
            // A soft line break reads as a space in the preview.
            MarkdownTokenTypes.EOL -> out += MarkdownInline.Text(" ")
            MarkdownTokenTypes.ESCAPED_BACKTICKS -> out += MarkdownInline.Text(node.text().replace("\\", ""))
            else -> {
                if (node.children.isEmpty()) {
                    out += MarkdownInline.Text(unescape(node))
                } else {
                    node.children.forEach { inline(it, out) }
                }
            }
        }
    }

    // Backslash escapes such as `\*` stay inside text tokens; show the character itself.
    private fun unescape(node: ASTNode): String = node.text().replace(BackslashEscape) { it.groupValues[1] }

    private fun ASTNode.text(): String = source.substring(startOffset, endOffset)

    private fun List<ASTNode>.withoutDelimiters(): List<ASTNode> {
        val delimiters = setOf(MarkdownTokenTypes.EMPH)
        return dropWhile { it.type in delimiters }.dropLastWhile { it.type in delimiters }
    }

    private fun List<ASTNode>.withoutBrackets(): List<ASTNode> =
        filterNot { it.type == MarkdownTokenTypes.LBRACKET || it.type == MarkdownTokenTypes.RBRACKET }
}

private fun headingLevel(type: IElementType): Int = when (type) {
    MarkdownElementTypes.ATX_1 -> 1
    MarkdownElementTypes.ATX_2 -> 2
    MarkdownElementTypes.ATX_3 -> 3
    MarkdownElementTypes.ATX_4 -> 4
    MarkdownElementTypes.ATX_5 -> 5
    else -> 6
}

private val BackslashEscape = Regex("""\\([!-/:-@\[-`{-~])""")

private fun String.trimSingleSpace(): String =
    if (length >= 2 && startsWith(' ') && endsWith(' ') && isNotBlank()) substring(1, length - 1) else this

/** Joins neighbouring text runs. */
private fun List<MarkdownInline>.mergeText(): List<MarkdownInline> {
    val out = mutableListOf<MarkdownInline>()
    for (inline in this) {
        val last = out.lastOrNull()
        if (inline is MarkdownInline.Text && last is MarkdownInline.Text) {
            out[out.lastIndex] = MarkdownInline.Text(last.text + inline.text)
        } else {
            out += inline
        }
    }
    return out
}

private fun List<MarkdownInline>.trimmed(): List<MarkdownInline> {
    if (isEmpty()) return this
    val out = toMutableList()
    (out.first() as? MarkdownInline.Text)?.let { out[0] = MarkdownInline.Text(it.text.trimStart()) }
    (out.last() as? MarkdownInline.Text)?.let { out[out.lastIndex] = MarkdownInline.Text(it.text.trimEnd()) }
    return out.filterNot { it is MarkdownInline.Text && it.text.isEmpty() }
}

/** The visible text of [this], without formatting. */
fun List<MarkdownInline>.plainText(): String = buildString {
    fun walk(items: List<MarkdownInline>) {
        for (item in items) when (item) {
            is MarkdownInline.Text -> append(item.text)
            is MarkdownInline.Bold -> walk(item.children)
            is MarkdownInline.Italic -> walk(item.children)
            is MarkdownInline.Strikethrough -> walk(item.children)
            is MarkdownInline.Code -> append(item.code)
            is MarkdownInline.Link -> walk(item.children)
            MarkdownInline.LineBreak -> append('\n')
        }
    }
    walk(this@plainText)
}
