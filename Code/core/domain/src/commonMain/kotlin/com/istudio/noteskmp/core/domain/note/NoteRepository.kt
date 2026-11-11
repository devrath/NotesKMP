package com.istudio.noteskmp.core.domain.note

import com.istudio.noteskmp.core.domain.DataError
import com.istudio.noteskmp.core.domain.EmptyResult
import com.istudio.noteskmp.core.domain.Result
import kotlinx.coroutines.flow.Flow

/** Notes on this device. The database is the single source of truth. */
interface NoteRepository {

    /** Pinned notes first, then the rest, each group newest edit first. */
    fun observeNotes(): Flow<List<NoteSummary>>

    suspend fun getNote(id: Long): Result<Note, DataError.Local>

    /** Creates a note created and edited now, and returns its ID. */
    suspend fun createNote(note: NewNote): Result<Long, DataError.Local>

    /** Replaces the text and marks the note edited now. Pin state is untouched. */
    suspend fun updateNote(id: Long, title: String, body: String): EmptyResult<DataError.Local>

    /** Pins or unpins without changing the edit time. */
    suspend fun setPinned(id: Long, isPinned: Boolean): EmptyResult<DataError.Local>

    suspend fun deleteNote(id: Long): EmptyResult<DataError.Local>

    /**
     * Notes whose title or body match every word of [terms] by prefix, ignoring case,
     * diacritics and Markdown markers. Ranked: title matches first, then newest edit.
     */
    suspend fun search(terms: List<String>): Result<List<NoteSearchHit>, DataError.Local>

    /**
     * Creates [samples] the first time the app ever runs, and never again, even after the
     * user deletes them. [samples] is only called on that first run.
     */
    suspend fun createSampleNotesOnFirstLaunch(samples: suspend () -> List<NewNote>): EmptyResult<DataError.Local>
}
