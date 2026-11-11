package com.istudio.noteskmp.feature.note.domain

/** A styled range in the raw editor. [end] is exclusive. The text itself never changes. */
data class SyntaxSpan(val start: Int, val end: Int, val kind: SyntaxKind)

enum class SyntaxKind {
    /** `#`, `**`, `- [ ]`, `>`, link brackets: shown muted. */
    Marker,
    Heading1,
    Heading2,
    Heading3,
    Bold,
    Italic,
    Strikethrough,
    Code,
    LinkText,
    LinkUrl,
}

/** Above this length the editor shows plain text so typing stays within a frame. */
const val SYNTAX_HIGHLIGHT_MAX_LENGTH = 100_000

private val HeadingLine = Regex("""^(\s{0,3})(#{1,6})(\s+|$)""")
private val BlockMarker = Regex("""^(\s*)((?:>\s?)+)?(\s*)([-*+] \[[ xX]]|[-*+]|\d{1,9}[.)])?(?=\s|$)""")
private val InlineCodeSpan = Regex("""(`+)(.+?)\1""")
private val StrongSpan = Regex("""(\*\*|__)(\S(?:.*?\S)?)\1""")
private val EmphasisSpan = Regex("""(?<![*\w])([*_])(?![*\s])(.+?)(?<![*\s])\1(?![*\w])""")
private val StrikeSpan = Regex("""(~~)(\S(?:.*?\S)?)~~""")
private val LinkSpan = Regex("""(!?\[)([^\]]*)(]\()([^)\s]*)(\))""")
private val BareUrl = Regex("""\bhttps?://[^\s<>()]+""")

/**
 * Light highlighting for the raw editor, computed line by line: muted markers, bold
 * headings, emphasis, monospace code (fenced blocks included) and links.
 */
fun markdownSyntaxSpans(text: String): List<SyntaxSpan> {
    if (text.length > SYNTAX_HIGHLIGHT_MAX_LENGTH) return emptyList()
    val spans = mutableListOf<SyntaxSpan>()
    var lineStart = 0
    var inFence = false
    while (lineStart <= text.length) {
        val lineEnd = text.lineEndAt(lineStart)
        val line = text.substring(lineStart, lineEnd)
        val trimmed = line.trimStart()
        when {
            trimmed.startsWith("```") || trimmed.startsWith("~~~") -> {
                spans += SyntaxSpan(lineStart, lineEnd, SyntaxKind.Code)
                spans += SyntaxSpan(lineStart + line.length - trimmed.length, lineEnd, SyntaxKind.Marker)
                inFence = !inFence
            }
            inFence -> if (lineEnd > lineStart) spans += SyntaxSpan(lineStart, lineEnd, SyntaxKind.Code)
            else -> lineSpans(line, lineStart, spans)
        }
        if (lineEnd >= text.length) break
        lineStart = lineEnd + 1
    }
    return spans
}

private fun lineSpans(line: String, offset: Int, spans: MutableList<SyntaxSpan>) {
    var contentStart = 0
    val heading = HeadingLine.find(line)
    if (heading != null) {
        // MatchGroup.range isn't available in common code, so positions come from group lengths.
        val hashesStart = heading.groupValues[1].length
        val hashes = heading.groupValues[2]
        val kind = when (hashes.length) {
            1 -> SyntaxKind.Heading1
            2 -> SyntaxKind.Heading2
            else -> SyntaxKind.Heading3
        }
        spans += SyntaxSpan(offset, offset + line.length, kind)
        spans += SyntaxSpan(offset + hashesStart, offset + hashesStart + hashes.length, SyntaxKind.Marker)
        contentStart = heading.range.last + 1
    } else {
        BlockMarker.find(line)?.let { m ->
            val (indent, quote, gap, marker) = m.destructured
            val quoteStart = offset + indent.length
            if (quote.isNotEmpty()) spans += SyntaxSpan(quoteStart, quoteStart + quote.length, SyntaxKind.Marker)
            val markerStart = quoteStart + quote.length + gap.length
            if (marker.isNotEmpty()) spans += SyntaxSpan(markerStart, markerStart + marker.length, SyntaxKind.Marker)
            contentStart = m.range.last + 1
        }
    }
    inlineSpans(line, offset, contentStart, spans)
}

private fun inlineSpans(line: String, offset: Int, from: Int, spans: MutableList<SyntaxSpan>) {
    val code = mutableListOf<IntRange>()
    InlineCodeSpan.findAll(line, from).forEach { m ->
        code += m.range
        spans += SyntaxSpan(offset + m.range.first, offset + m.range.last + 1, SyntaxKind.Code)
        val ticks = m.groups[1]!!.value.length
        spans += SyntaxSpan(offset + m.range.first, offset + m.range.first + ticks, SyntaxKind.Marker)
        spans += SyntaxSpan(offset + m.range.last + 1 - ticks, offset + m.range.last + 1, SyntaxKind.Marker)
    }
    fun outsideCode(range: IntRange) = code.none { it.first <= range.first && range.last <= it.last }

    fun wrapped(regex: Regex, kind: SyntaxKind) {
        regex.findAll(line, from).filter { outsideCode(it.range) }.forEach { m ->
            val marker = m.groups[1]!!.value.length
            val start = offset + m.range.first
            val end = offset + m.range.last + 1
            spans += SyntaxSpan(start, end, kind)
            spans += SyntaxSpan(start, start + marker, SyntaxKind.Marker)
            spans += SyntaxSpan(end - marker, end, SyntaxKind.Marker)
        }
    }
    wrapped(StrongSpan, SyntaxKind.Bold)
    wrapped(EmphasisSpan, SyntaxKind.Italic)
    wrapped(StrikeSpan, SyntaxKind.Strikethrough)

    val links = mutableListOf<IntRange>()
    LinkSpan.findAll(line, from).filter { outsideCode(it.range) }.forEach { m ->
        links += m.range
        // The five groups are contiguous, so each starts where the previous one ends.
        var start = offset + m.range.first
        listOf(SyntaxKind.Marker, SyntaxKind.LinkText, SyntaxKind.Marker, SyntaxKind.LinkUrl, SyntaxKind.Marker)
            .forEachIndexed { index, kind ->
                val length = m.groupValues[index + 1].length
                if (length > 0) spans += SyntaxSpan(start, start + length, kind)
                start += length
            }
    }
    BareUrl.findAll(line, from)
        .filter { m -> outsideCode(m.range) && links.none { it.first <= m.range.first && m.range.last <= it.last } }
        .forEach { spans += SyntaxSpan(offset + it.range.first, offset + it.range.last + 1, SyntaxKind.LinkText) }
}
