package com.istudio.noteskmp.feature.note.domain

/**
 * Ticks or unticks the checklist box whose `[` is at [checkboxOffset] in [body]: `[ ]`
 * becomes `[x]` and `[x]` / `[X]` becomes `[ ]`. Nothing else in the text changes.
 * Returns null when [checkboxOffset] doesn't point at a box, for example after the text
 * changed underneath.
 */
fun toggleChecklistItem(body: String, checkboxOffset: Int): String? {
    if (checkboxOffset < 0 || checkboxOffset + 2 >= body.length) return null
    if (body[checkboxOffset] != '[' || body[checkboxOffset + 2] != ']') return null
    val replacement = when (body[checkboxOffset + 1]) {
        ' ' -> 'x'
        'x', 'X' -> ' '
        else -> return null
    }
    return buildString(body.length) {
        append(body, 0, checkboxOffset + 1)
        append(replacement)
        append(body, checkboxOffset + 2, body.length)
    }
}
