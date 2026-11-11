package com.istudio.noteskmp.core.domain.note

import kotlin.time.Instant

/** A note as stored. [body] is Markdown exactly as the user typed it. */
data class Note(
    val id: Long,
    val title: String,
    val body: String,
    val createdAt: Instant,
    val editedAt: Instant,
    val isPinned: Boolean,
)

/** What the list and search show as a note's title. */
sealed interface NoteTitle {
    /** The title the user typed. */
    data class Typed(val text: String) : NoteTitle

    /** No title, so the first non-empty body line with Markdown stripped. */
    data class Fallback(val text: String) : NoteTitle

    /** Neither a title nor any body text. */
    data object Untitled : NoteTitle
}

/** A list entry: the note without its full body. */
data class NoteSummary(
    val id: Long,
    val title: NoteTitle,
    /** Body text with Markdown stripped, possibly cut short; the UI limits it to two lines. */
    val preview: String,
    val editedAt: Instant,
    val isPinned: Boolean,
)

/** A note to create, such as a sample note or an imported file. */
data class NewNote(
    val title: String,
    val body: String,
    val isPinned: Boolean = false,
)

/** A search match, with the stripped body that snippets are cut from. */
data class NoteSearchHit(
    val id: Long,
    val title: NoteTitle,
    val strippedBody: String,
    val editedAt: Instant,
    val isPinned: Boolean,
)
