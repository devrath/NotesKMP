package com.istudio.noteskmp.core.testing

import com.istudio.noteskmp.core.domain.DataError
import com.istudio.noteskmp.core.domain.EmptyResult
import com.istudio.noteskmp.core.domain.Result
import com.istudio.noteskmp.core.domain.note.NewNote
import com.istudio.noteskmp.core.domain.note.Note
import com.istudio.noteskmp.core.domain.note.NoteRepository
import com.istudio.noteskmp.core.domain.note.NoteSearchHit
import com.istudio.noteskmp.core.domain.note.NoteSummary
import com.istudio.noteskmp.core.domain.note.noteTitle
import com.istudio.noteskmp.core.domain.note.stripMarkdown
import com.istudio.noteskmp.core.domain.note.summarizeNoteText
import com.istudio.noteskmp.core.domain.search.matchesAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

/**
 * Notes in memory with the same ordering and edit-time rules as the real repository.
 * Set [error] to make every call fail with it.
 */
class FakeNoteRepository(private val clock: Clock = FakeClock()) : NoteRepository {

    private val notes = MutableStateFlow<Map<Long, Note>>(emptyMap())
    private var nextId = 1L
    private var samplesCreated = false

    var error: DataError.Local? = null

    /** Every write, in order, for asserting what was saved. */
    val writes = mutableListOf<String>()

    val all: List<Note> get() = notes.value.values.sortedBy { it.id }

    fun note(id: Long): Note? = notes.value[id]

    fun add(title: String, body: String, isPinned: Boolean = false): Long {
        val id = nextId++
        val now = clock.now()
        notes.value += id to Note(id, title, body, now, now, isPinned)
        return id
    }

    override fun observeNotes(): Flow<List<NoteSummary>> = notes.map { map ->
        map.values
            .sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.editedAt }.thenByDescending { it.id })
            .map { note ->
                val summary = summarizeNoteText(note.title, note.body)
                NoteSummary(note.id, noteTitle(note.title, summary.fallbackTitle), summary.preview, note.editedAt, note.isPinned)
            }
    }

    override suspend fun getNote(id: Long): Result<Note, DataError.Local> {
        error?.let { return Result.Error(it) }
        return notes.value[id]?.let { Result.Success(it) } ?: Result.Error(DataError.Local.NOT_FOUND)
    }

    override suspend fun createNote(note: NewNote): Result<Long, DataError.Local> {
        error?.let { return Result.Error(it) }
        writes += "create:${note.title}|${note.body}"
        return Result.Success(add(note.title, note.body, note.isPinned))
    }

    override suspend fun updateNote(id: Long, title: String, body: String): EmptyResult<DataError.Local> {
        error?.let { return Result.Error(it) }
        val note = notes.value[id] ?: return Result.Error(DataError.Local.NOT_FOUND)
        writes += "update:$id:$title|$body"
        notes.value += id to note.copy(title = title, body = body, editedAt = clock.now())
        return Result.Success(Unit)
    }

    override suspend fun setPinned(id: Long, isPinned: Boolean): EmptyResult<DataError.Local> {
        error?.let { return Result.Error(it) }
        val note = notes.value[id] ?: return Result.Error(DataError.Local.NOT_FOUND)
        writes += "pin:$id:$isPinned"
        notes.value += id to note.copy(isPinned = isPinned)
        return Result.Success(Unit)
    }

    override suspend fun deleteNote(id: Long): EmptyResult<DataError.Local> {
        error?.let { return Result.Error(it) }
        if (id !in notes.value) return Result.Error(DataError.Local.NOT_FOUND)
        writes += "delete:$id"
        notes.value -= id
        return Result.Success(Unit)
    }

    override suspend fun search(terms: List<String>): Result<List<NoteSearchHit>, DataError.Local> {
        error?.let { return Result.Error(it) }
        val hits = notes.value.values
            .filter { matchesAll(it.title + "\n" + stripMarkdown(it.body), terms) }
            .sortedByDescending { it.editedAt }
            .map { note ->
                val summary = summarizeNoteText(note.title, note.body)
                NoteSearchHit(note.id, noteTitle(note.title, summary.fallbackTitle), stripMarkdown(note.body), note.editedAt, note.isPinned)
            }
        return Result.Success(hits)
    }

    override suspend fun createSampleNotesOnFirstLaunch(samples: suspend () -> List<NewNote>): EmptyResult<DataError.Local> {
        error?.let { return Result.Error(it) }
        if (!samplesCreated) {
            samplesCreated = true
            samples().forEach { add(it.title, it.body, it.isPinned) }
        }
        return Result.Success(Unit)
    }
}
