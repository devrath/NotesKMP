package com.istudio.noteskmp.feature.note.domain

/** Editor text and its selection. A collapsed selection is the cursor. */
data class EditorValue(
    val text: String,
    val selectionStart: Int = text.length,
    val selectionEnd: Int = selectionStart,
) {
    val min: Int get() = minOf(selectionStart, selectionEnd).coerceIn(0, text.length)
    val max: Int get() = maxOf(selectionStart, selectionEnd).coerceIn(0, text.length)
    val isCollapsed: Boolean get() = min == max
}

internal fun String.lineStartAt(index: Int): Int = lastIndexOf('\n', index - 1) + 1

internal fun String.lineEndAt(index: Int): Int = indexOf('\n', index).let { if (it < 0) length else it }

/** Whether [index] sits inside a fenced code block (an odd number of fences above it). */
internal fun String.isInsideCodeFence(index: Int): Boolean {
    var inside = false
    var lineStart = 0
    while (lineStart < index) {
        val lineEnd = lineEndAt(lineStart)
        if (lineEnd >= index) break
        if (substring(lineStart, lineEnd).trimStart().let { it.startsWith("```") || it.startsWith("~~~") }) {
            inside = !inside
        }
        lineStart = lineEnd + 1
    }
    return inside
}
