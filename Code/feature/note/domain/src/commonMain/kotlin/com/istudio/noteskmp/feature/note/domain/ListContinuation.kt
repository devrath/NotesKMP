package com.istudio.noteskmp.feature.note.domain

private val ChecklistLine = Regex("""^(\s*)([-*+]) \[[ xX]] (.*)$""")
private val BulletLine = Regex("""^(\s*)([-*+]) (.*)$""")
private val OrderedLine = Regex("""^(\s*)(\d{1,9})([.)]) (.*)$""")
private val QuoteLine = Regex("""^(\s*)((?:> ?)+)(.*)$""")

/**
 * Applies list continuation when the change from [before] to [after] is a single Enter
 * typed at the end of a list item, checklist item or quote line. Returns the replacement
 * value, or null to keep [after] as it is.
 *
 * - A non-empty item continues: `- `, `- [ ] `, the next number, or `> ` starts the new line.
 * - An empty item ends the list: its marker is removed and no new line is added.
 */
fun continueList(before: EditorValue, after: EditorValue): EditorValue? {
    if (!before.isCollapsed || !after.isCollapsed) return null
    val cursor = before.min
    if (after.text.length != before.text.length + 1 || after.min != cursor + 1) return null
    if (after.text[cursor] != '\n') return null
    if (!after.text.regionMatches(0, before.text, 0, cursor) ||
        !after.text.regionMatches(cursor + 1, before.text, cursor, before.text.length - cursor)
    ) {
        return null
    }

    val text = before.text
    val lineStart = text.lineStartAt(cursor)
    if (text.lineEndAt(cursor) != cursor) return null
    if (text.isInsideCodeFence(lineStart)) return null
    val line = text.substring(lineStart, cursor)

    val (continuation, content) = continuationFor(line) ?: return null
    if (content.isBlank()) {
        // Enter on an empty item: drop the marker and end the list.
        val newText = text.substring(0, lineStart) + text.substring(cursor)
        return EditorValue(newText, lineStart)
    }
    val insert = "\n" + continuation
    val newText = text.substring(0, cursor) + insert + text.substring(cursor)
    return EditorValue(newText, cursor + insert.length)
}

/** The marker to start the next line with, and the item's content. */
private fun continuationFor(line: String): Pair<String, String>? {
    ChecklistLine.matchEntire(line)?.let { m ->
        val (indent, bullet, content) = m.destructured
        return "$indent$bullet [ ] " to content
    }
    BulletLine.matchEntire(line)?.let { m ->
        val (indent, bullet, content) = m.destructured
        return "$indent$bullet " to content
    }
    OrderedLine.matchEntire(line)?.let { m ->
        val (indent, number, delimiter, content) = m.destructured
        val next = number.toLong() + 1
        return "$indent$next$delimiter " to content
    }
    QuoteLine.matchEntire(line)?.let { m ->
        val (indent, markers, content) = m.destructured
        val marker = if (markers.endsWith(' ')) markers else "$markers "
        return "$indent$marker" to content
    }
    return null
}
