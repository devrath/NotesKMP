package com.istudio.noteskmp.core.domain.note

/** Previews never need more than this; the list shows two lines of it. */
const val PREVIEW_MAX_LENGTH = 300

private val HeadingMarker = Regex("""^#{1,6}(\s+|$)""")
private val ClosingHashes = Regex("""\s+#+\s*$""")
private val QuoteMarker = Regex("""^(>\s?)+""")
private val ListMarker = Regex("""^([-*+]|\d{1,9}[.)])\s+""")
private val TaskMarker = Regex("""^\[[ xX]]\s+""")
private val ThematicBreak = Regex("""^([-*_])(\s*\1){2,}\s*$""")
private val Fence = Regex("""^(`{3,}|~{3,})""")
private val Image = Regex("""!\[([^\]]*)]\([^)]*\)""")
private val Link = Regex("""\[([^\]]*)]\([^)]*\)""")
private val Autolink = Regex("""<((?:https?|mailto):[^>\s]+)>""")
private val Strong = Regex("""(\*\*|__)(\S(?:.*?\S)?)\1""")
private val Strike = Regex("""~~(\S(?:.*?\S)?)~~""")
private val EmphasisStar = Regex("""\*(\S(?:.*?\S)?)\*""")
private val EmphasisUnderscore = Regex("""(?<![\p{L}\p{N}])_(\S(?:.*?\S)?)_(?![\p{L}\p{N}])""")
private val InlineCode = Regex("""(`+)(.+?)\1""")
/** Start of a private-use block that stands in for escaped ASCII characters while stripping. */
private const val ESCAPE_BASE = 0xE000
private val Escape = Regex("""\\([\\`*_{}\[\]()#+\-.!~>|])""")

/**
 * Turns one line of Markdown into the plain prose it reads as: no `#`, `>`, list or
 * checklist markers, emphasis, code ticks or link syntax. Text inside is kept.
 */
fun stripMarkdownLine(line: String): String {
    var text = line.trim()
    if (ThematicBreak.matches(text) || Fence.containsMatchIn(text)) return ""
    text = text.replace(QuoteMarker, "")
    text = if (HeadingMarker.containsMatchIn(text)) {
        text.replace(HeadingMarker, "").replace(ClosingHashes, "")
    } else {
        text.replace(ListMarker, "").replace(TaskMarker, "")
    }
    // Escaped characters are hidden first so they never act as markers below.
    text = text.replace(Escape) { (ESCAPE_BASE + it.groupValues[1][0].code).toChar().toString() }
    text = text
        .replace(InlineCode) { it.groupValues[2].trim() }
        .replace(Image) { it.groupValues[1] }
        .replace(Link) { it.groupValues[1] }
        .replace(Autolink) { it.groupValues[1] }
        .replace(Strong) { it.groupValues[2] }
        .replace(Strike) { it.groupValues[1] }
        .replace(EmphasisStar) { it.groupValues[1] }
        .replace(EmphasisUnderscore) { it.groupValues[1] }
    text = buildString(text.length) {
        for (c in text) append(if (c.code in ESCAPE_BASE..ESCAPE_BASE + 0x7F) (c.code - ESCAPE_BASE).toChar() else c)
    }
    return text.trim()
}

/** The whole body as plain prose, one stripped line per line. Used for the search index. */
fun stripMarkdown(body: String): String =
    body.lineSequence().map(::stripMarkdownLine).joinToString("\n")

/** Fallback title and preview, derived without reading more of the body than needed. */
data class NoteTextSummary(
    val fallbackTitle: String?,
    val preview: String,
)

/**
 * The fallback title (first non-empty stripped body line, only when [title] is blank) and
 * the preview (the following stripped lines joined into prose, cut at [PREVIEW_MAX_LENGTH]).
 */
fun summarizeNoteText(title: String, body: String): NoteTextSummary {
    var fallback: String? = null
    val needsFallback = title.isBlank()
    val preview = StringBuilder()
    for (line in body.lineSequence()) {
        val stripped = stripMarkdownLine(line)
        if (stripped.isEmpty()) continue
        if (needsFallback && fallback == null) {
            fallback = stripped
            continue
        }
        if (preview.isNotEmpty()) preview.append(' ')
        preview.append(stripped)
        if (preview.length >= PREVIEW_MAX_LENGTH) break
    }
    return NoteTextSummary(
        fallbackTitle = fallback,
        preview = preview.toString().safeTake(PREVIEW_MAX_LENGTH),
    )
}

/** The title to show for a note. */
fun noteTitle(title: String, fallbackTitle: String?): NoteTitle = when {
    title.isNotBlank() -> NoteTitle.Typed(title.trim())
    !fallbackTitle.isNullOrBlank() -> NoteTitle.Fallback(fallbackTitle)
    else -> NoteTitle.Untitled
}

/** Takes at most [n] chars without splitting a surrogate pair. */
internal fun String.safeTake(n: Int): String {
    if (length <= n) return this
    val end = if (n > 0 && this[n - 1].isHighSurrogate()) n - 1 else n
    return substring(0, end)
}
