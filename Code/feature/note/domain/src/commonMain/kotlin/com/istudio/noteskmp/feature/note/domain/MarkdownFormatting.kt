package com.istudio.noteskmp.feature.note.domain

/** What the formatting toolbar can insert. Each produces the Markdown a user could type. */
enum class MarkdownFormat {
    Heading,
    Bold,
    Italic,
    BulletedList,
    NumberedList,
    Checklist,
    Quote,
    InlineCode,
    CodeBlock,
    Link,
}

/**
 * Applies [format] to [value] as one edit.
 *
 * - Bold, italic, inline code: wrap the selection, unwrap it if it's already wrapped, or
 *   insert an empty pair with the cursor between.
 * - Lists, checklist, quote: toggle the marker on every line the selection touches.
 * - Heading: cycle `#` → `##` → `###` → none on those lines.
 * - Code block: fence the selected lines, or insert an empty block with the cursor inside.
 * - Link: `[selection](url)` with the cursor in the URL. A URL in [clipboardText] becomes
 *   the target.
 */
fun applyFormat(value: EditorValue, format: MarkdownFormat, clipboardText: String? = null): EditorValue =
    when (format) {
        MarkdownFormat.Bold -> toggleInline(value, '*', 2)
        MarkdownFormat.Italic -> toggleInline(value, '*', 1)
        MarkdownFormat.InlineCode -> toggleInline(value, '`', 1)
        MarkdownFormat.Heading -> cycleHeading(value)
        MarkdownFormat.BulletedList -> toggleLineMarker(value, LineMarker.Bullet)
        MarkdownFormat.NumberedList -> toggleLineMarker(value, LineMarker.Numbered)
        MarkdownFormat.Checklist -> toggleLineMarker(value, LineMarker.Checklist)
        MarkdownFormat.Quote -> toggleQuote(value)
        MarkdownFormat.CodeBlock -> insertCodeBlock(value)
        MarkdownFormat.Link -> insertLink(value, clipboardText?.trim()?.takeIf(::isLinkTarget))
    }

// ---- Inline styles ----

private fun toggleInline(value: EditorValue, markerChar: Char, size: Int): EditorValue {
    val text = value.text
    val start = value.min
    val end = value.max
    val marker = markerChar.toString().repeat(size)

    // The selection sits between markers: **|text|**
    val before = text.runBefore(start, markerChar)
    val after = text.runAfter(end, markerChar)
    if (hasMarker(minOf(before, after), markerChar, size)) {
        val newText = text.substring(0, start - size) + text.substring(start, end) + text.substring(end + size)
        return EditorValue(newText, start - size, end - size)
    }

    // The selection includes the markers: |**text**|
    val selected = text.substring(start, end)
    val innerBefore = selected.takeWhile { it == markerChar }.length
    val innerAfter = selected.takeLastWhile { it == markerChar }.length
    if (selected.length >= 2 * size && innerBefore < selected.length &&
        hasMarker(minOf(innerBefore, innerAfter), markerChar, size)
    ) {
        val inner = selected.substring(size, selected.length - size)
        val newText = text.substring(0, start) + inner + text.substring(end)
        return EditorValue(newText, start, start + inner.length)
    }

    val newText = text.substring(0, start) + marker + selected + marker + text.substring(end)
    return EditorValue(newText, start + size, end + size)
}

/**
 * Whether a run of [run] marker chars contains a marker of [size]. For `*`, a run of 1 is
 * italic, 2 is bold and 3 is both; inline code only uses single backticks here.
 */
private fun hasMarker(run: Int, markerChar: Char, size: Int): Boolean = when {
    markerChar != '*' -> run >= size
    size == 1 -> run == 1 || run == 3
    else -> run == 2 || run == 3
}

private fun String.runBefore(index: Int, c: Char): Int {
    var i = index
    while (i > 0 && this[i - 1] == c) i--
    return index - i
}

private fun String.runAfter(index: Int, c: Char): Int {
    var i = index
    while (i < length && this[i] == c) i++
    return i - index
}

// ---- Line styles ----

private enum class LineMarker { Bullet, Numbered, Checklist }

private val ListPrefix = Regex("""^(\s*)(?:[-*+] \[[ xX]] |[-*+] |\d{1,9}[.)] )""")
private val ChecklistPrefix = Regex("""^\s*[-*+] \[[ xX]] """)
private val BulletPrefix = Regex("""^\s*[-*+] (?!\[[ xX]] )""")
private val NumberedPrefix = Regex("""^\s*\d{1,9}[.)] """)
private val QuotePrefix = Regex("""^> ?""")
private val HeadingPrefix = Regex("""^(#{1,6}) """)

private fun toggleLineMarker(value: EditorValue, marker: LineMarker): EditorValue {
    val lines = value.touchedLines()
    val existing = when (marker) {
        LineMarker.Bullet -> BulletPrefix
        LineMarker.Numbered -> NumberedPrefix
        LineMarker.Checklist -> ChecklistPrefix
    }
    val removing = lines.all { existing.containsMatchIn(value.text.substring(it.first, it.last)) }
    var number = 0
    return value.editLines(lines) { line ->
        val match = ListPrefix.find(line)
        val indent = match?.groupValues?.get(1) ?: line.takeWhile { it == ' ' || it == '\t' }
        val content = line.substring(match?.value?.length ?: indent.length)
        when {
            removing -> LineEdit(indent, content)
            else -> {
                number++
                val prefix = when (marker) {
                    LineMarker.Bullet -> "- "
                    LineMarker.Numbered -> "$number. "
                    LineMarker.Checklist -> "- [ ] "
                }
                LineEdit(indent + prefix, content)
            }
        }
    }
}

private fun toggleQuote(value: EditorValue): EditorValue {
    val lines = value.touchedLines()
    val removing = lines.all { QuotePrefix.containsMatchIn(value.text.substring(it.first, it.last)) }
    return value.editLines(lines) { line ->
        val existing = QuotePrefix.find(line)?.value.orEmpty()
        if (removing) LineEdit("", line.substring(existing.length)) else LineEdit("> ", line)
    }
}

private fun cycleHeading(value: EditorValue): EditorValue {
    val lines = value.touchedLines()
    val first = value.text.substring(lines.first().first, lines.first().last)
    val level = HeadingPrefix.find(first)?.groupValues?.get(1)?.length ?: 0
    val next = when (level) {
        0 -> "# "
        1 -> "## "
        2 -> "### "
        else -> ""
    }
    return value.editLines(lines) { line ->
        val existing = HeadingPrefix.find(line)?.value.orEmpty()
        LineEdit(next, line.substring(existing.length))
    }
}

/** A line rewritten as [prefix] + [content]; the original prefix length is derived. */
private class LineEdit(val prefix: String, val content: String)

/** Start and end (exclusive) of every line the selection touches. */
private fun EditorValue.touchedLines(): List<IntRange> {
    val start = min
    // A selection that ends right after a newline doesn't touch the next line.
    val end = if (max > start && text[max - 1] == '\n') max - 1 else max
    val lines = mutableListOf<IntRange>()
    var lineStart = text.lineStartAt(start)
    while (true) {
        val lineEnd = text.lineEndAt(lineStart)
        lines += lineStart..lineEnd
        if (lineEnd >= end || lineEnd >= text.length) break
        lineStart = lineEnd + 1
    }
    return lines
}

/**
 * Rewrites each line in [lines] (ranges are start..endExclusive) and maps the selection
 * through the change, so it keeps covering the same text.
 */
private fun EditorValue.editLines(lines: List<IntRange>, edit: (String) -> LineEdit): EditorValue {
    val out = StringBuilder()
    var copied = 0
    var shift = 0
    var newStart = min
    var newEnd = max
    for (range in lines) {
        val line = text.substring(range.first, range.last)
        val result = edit(line)
        val oldPrefixLength = line.length - result.content.length
        val delta = result.prefix.length - oldPrefixLength
        out.append(text, copied, range.first).append(result.prefix).append(result.content)
        copied = range.last
        fun map(p: Int): Int = when {
            p < range.first || p > range.last -> p
            p - range.first <= oldPrefixLength -> range.first + shift + result.prefix.length
            else -> p + shift + delta
        }
        if (min in range) newStart = map(min)
        if (max in range) newEnd = map(max)
        shift += delta
    }
    out.append(text, copied, text.length)
    if (max > lines.last().last) newEnd = max + shift
    return EditorValue(out.toString(), newStart, newEnd)
}

// ---- Blocks ----

private const val FENCE = "```"

private fun insertCodeBlock(value: EditorValue): EditorValue {
    val text = value.text
    if (value.isCollapsed) {
        val cursor = value.min
        val before = if (cursor > 0 && text[cursor - 1] != '\n') "\n" else ""
        val after = if (cursor < text.length && text[cursor] != '\n') "\n" else ""
        val opening = "$before$FENCE\n"
        val insert = "$opening\n$FENCE$after"
        return EditorValue(text.substring(0, cursor) + insert + text.substring(cursor), cursor + opening.length)
    }
    val lines = value.touchedLines()
    val blockStart = lines.first().first
    val blockEnd = lines.last().last
    val body = text.substring(blockStart, blockEnd)
    val opening = "$FENCE\n"
    val newText = text.substring(0, blockStart) + opening + body + "\n" + FENCE + text.substring(blockEnd)
    return EditorValue(newText, blockStart + opening.length, blockStart + opening.length + body.length)
}

private fun insertLink(value: EditorValue, url: String?): EditorValue {
    val text = value.text
    val start = value.min
    val end = value.max
    val label = text.substring(start, end)
    val target = url.orEmpty()
    val link = "[$label]($target)"
    val newText = text.substring(0, start) + link + text.substring(end)
    // With a label, the cursor goes to the URL. Without a label but with a pasted URL,
    // the label is what's left to type.
    val cursor = if (label.isEmpty() && target.isNotEmpty()) start + 1 else start + label.length + 3 + target.length
    return EditorValue(newText, cursor)
}

/** Whether [text] is a URL the app would open, and so worth using as a link target. */
fun isLinkTarget(text: String): Boolean =
    text.isNotEmpty() && text.none { it.isWhitespace() } && isSafeLinkUrl(text) && text.contains(':')

/** Only these schemes ever open. Anything else is shown as text. */
fun isSafeLinkUrl(url: String): Boolean {
    val scheme = url.substringBefore(':', missingDelimiterValue = "").lowercase()
    return scheme == "http" || scheme == "https" || scheme == "mailto"
}
