package com.istudio.noteskmp.core.data

import com.istudio.noteskmp.core.database.NoteEntity
import com.istudio.noteskmp.core.database.NoteSearchRow
import com.istudio.noteskmp.core.database.NoteSummaryRow
import com.istudio.noteskmp.core.domain.note.Note
import com.istudio.noteskmp.core.domain.note.NoteSearchHit
import com.istudio.noteskmp.core.domain.note.NoteSummary
import com.istudio.noteskmp.core.domain.note.noteTitle
import kotlin.time.Instant

fun NoteEntity.toNote(): Note = Note(
    id = id,
    title = title,
    body = body,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    editedAt = Instant.fromEpochMilliseconds(editedAt),
    isPinned = isPinned,
)

fun NoteSummaryRow.toNoteSummary(): NoteSummary = NoteSummary(
    id = id,
    title = noteTitle(title, fallbackTitle),
    preview = preview,
    editedAt = Instant.fromEpochMilliseconds(editedAt),
    isPinned = isPinned,
)

fun NoteSearchRow.toNoteSearchHit(): NoteSearchHit = NoteSearchHit(
    id = id,
    title = noteTitle(title, fallbackTitle),
    strippedBody = strippedBody,
    editedAt = Instant.fromEpochMilliseconds(editedAt),
    isPinned = isPinned,
)
