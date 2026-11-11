package com.istudio.noteskmp.feature.search.domain

import com.istudio.noteskmp.core.domain.search.findMatches

/** About two lines of result text. */
const val SNIPPET_LENGTH = 160

/** Text shown before the first match, so it reads in context. */
private const val LEADING_CONTEXT = 40

private const val ELLIPSIS = "…"

/** A result snippet and where the terms match inside it. */
data class SearchSnippet(
    val text: String,
    val matches: List<IntRange>,
)

/**
 * A snippet of [strippedBody] centred on the first match of [terms], on word boundaries,
 * with `…` where text was cut. When the body has no match (only the title matched), the
 * snippet is the start of the body.
 */
fun buildSnippet(strippedBody: String, terms: List<String>, length: Int = SNIPPET_LENGTH): SearchSnippet {
    val flat = strippedBody.replace('\n', ' ').trim()
    if (flat.isEmpty()) return SearchSnippet("", emptyList())
    val firstMatch = findMatches(flat, terms).firstOrNull()?.first ?: 0

    var start = (firstMatch - LEADING_CONTEXT).coerceAtLeast(0)
    if (start > 0) start = flat.wordStartAtOrAfter(start).coerceAtMost(firstMatch)
    var end = (start + length).coerceAtMost(flat.length)
    if (end < flat.length) end = flat.wordEndAtOrBefore(end).let { if (it <= firstMatch) (start + length).coerceAtMost(flat.length) else it }
    end = end.avoidSplitSurrogate(flat)
    start = start.avoidSplitSurrogate(flat)

    val text = buildString {
        if (start > 0) append(ELLIPSIS)
        append(flat, start, end)
        if (end < flat.length) append(ELLIPSIS)
    }
    return SearchSnippet(text, findMatches(text, terms))
}

private fun String.wordStartAtOrAfter(index: Int): Int {
    var i = index
    while (i < length && !this[i].isWhitespace() && i > 0 && !this[i - 1].isWhitespace()) i++
    while (i < length && this[i].isWhitespace()) i++
    return i
}

private fun String.wordEndAtOrBefore(index: Int): Int {
    var i = index
    while (i > 0 && !this[i - 1].isWhitespace() && i < length && !this[i].isWhitespace()) i--
    while (i > 0 && this[i - 1].isWhitespace()) i--
    return i
}

private fun Int.avoidSplitSurrogate(text: String): Int =
    if (this in 1 until text.length && text[this].isLowSurrogate() && text[this - 1].isHighSurrogate()) this - 1 else this
