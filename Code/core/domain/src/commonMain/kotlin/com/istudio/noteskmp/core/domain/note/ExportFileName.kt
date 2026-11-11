package com.istudio.noteskmp.core.domain.note

const val EXPORT_FILE_NAME_MAX_LENGTH = 100
const val MARKDOWN_EXTENSION = ".md"

private const val ILLEGAL_FILE_NAME_CHARS = "/\\:*?\"<>|"

/**
 * File name for exporting a note titled [title] (its title or fallback title): characters
 * that file systems reject become `-`, the name is trimmed to 100 characters, and `.md`
 * is added. [untitled] is used when nothing usable is left.
 */
fun exportFileName(title: String, untitled: String): String {
    val sanitized = buildString {
        for (c in title) {
            append(if (c in ILLEGAL_FILE_NAME_CHARS || c.isISOControl()) '-' else c)
        }
    }.trim().safeTake(EXPORT_FILE_NAME_MAX_LENGTH).trim()
    // A name of only dots means "this folder" or "parent folder" on most systems.
    val base = if (sanitized.isEmpty() || sanitized.all { it == '.' }) untitled else sanitized
    return base + MARKDOWN_EXTENSION
}

/** The exported file holds exactly the body, with `\n` line endings. The title isn't added. */
fun exportContent(body: String): String =
    if ('\r' !in body) body else body.replace("\r\n", "\n").replace('\r', '\n')
