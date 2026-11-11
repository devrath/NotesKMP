package com.istudio.noteskmp.core.data

import com.istudio.noteskmp.core.database.NoteDao
import com.istudio.noteskmp.core.database.NoteEntity
import com.istudio.noteskmp.core.domain.DataError
import com.istudio.noteskmp.core.domain.EmptyResult
import com.istudio.noteskmp.core.domain.Result
import com.istudio.noteskmp.core.domain.note.NewNote
import com.istudio.noteskmp.core.domain.note.Note
import com.istudio.noteskmp.core.domain.note.NoteRepository
import com.istudio.noteskmp.core.domain.note.NoteSearchHit
import com.istudio.noteskmp.core.domain.note.NoteSummary
import com.istudio.noteskmp.core.domain.note.NoteTitle
import com.istudio.noteskmp.core.domain.note.stripMarkdown
import com.istudio.noteskmp.core.domain.note.summarizeNoteText
import com.istudio.noteskmp.core.domain.search.matchesAny
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

internal const val SAMPLE_NOTES_FLAG = "sample_notes_created"

/**
 * Notes in Room. Writes derive the fallback title, preview and stripped index text on
 * [computeDispatcher], since a body can be up to 5 MB.
 */
class RoomNoteRepository(
    private val dao: NoteDao,
    private val clock: Clock,
    private val computeDispatcher: CoroutineDispatcher,
) : NoteRepository {

    override fun observeNotes(): Flow<List<NoteSummary>> =
        dao.observeSummaries().map { rows -> rows.map { it.toNoteSummary() } }

    override suspend fun getNote(id: Long): Result<Note, DataError.Local> = safeCall {
        dao.getById(id)?.let { Result.Success(it.toNote()) } ?: Result.Error(DataError.Local.NOT_FOUND)
    }

    override suspend fun createNote(note: NewNote): Result<Long, DataError.Local> = safeCall {
        val (entity, stripped) = newEntity(note, clock.now().toEpochMilliseconds())
        Result.Success(dao.insertNote(entity, stripped))
    }

    override suspend fun updateNote(id: Long, title: String, body: String): EmptyResult<DataError.Local> = safeCall {
        val (summary, stripped) = withContext(computeDispatcher) {
            summarizeNoteText(title, body) to stripMarkdown(body)
        }
        val updated = dao.updateText(
            id = id,
            title = title,
            body = body,
            fallbackTitle = summary.fallbackTitle,
            preview = summary.preview,
            strippedBody = stripped,
            editedAt = clock.now().toEpochMilliseconds(),
        )
        if (updated) Result.Success(Unit) else Result.Error(DataError.Local.NOT_FOUND)
    }

    override suspend fun setPinned(id: Long, isPinned: Boolean): EmptyResult<DataError.Local> = safeCall {
        if (dao.setPinned(id, isPinned) > 0) Result.Success(Unit) else Result.Error(DataError.Local.NOT_FOUND)
    }

    override suspend fun deleteNote(id: Long): EmptyResult<DataError.Local> = safeCall {
        if (dao.deleteNote(id)) Result.Success(Unit) else Result.Error(DataError.Local.NOT_FOUND)
    }

    override suspend fun search(terms: List<String>): Result<List<NoteSearchHit>, DataError.Local> = safeCall {
        if (terms.isEmpty()) return@safeCall Result.Success(emptyList())
        val rows = dao.search(ftsMatchQuery(terms))
        val ranked = withContext(computeDispatcher) {
            rows.map { it.toNoteSearchHit() }
                .sortedWith(
                    compareByDescending<NoteSearchHit> { matchesAny(it.title.text(), terms) }
                        .thenByDescending { it.editedAt }
                        .thenByDescending { it.id },
                )
        }
        Result.Success(ranked)
    }

    override suspend fun createSampleNotesOnFirstLaunch(
        samples: suspend () -> List<NewNote>,
    ): EmptyResult<DataError.Local> = safeCall {
        if (dao.getMeta(SAMPLE_NOTES_FLAG) != null) return@safeCall Result.Success(Unit)
        val now = clock.now().toEpochMilliseconds()
        // Older samples first, so the first sample ends up on top of its group.
        val entities = samples().reversed().mapIndexed { index, note -> newEntity(note, now + index) }
        dao.insertOnce(SAMPLE_NOTES_FLAG, entities)
        Result.Success(Unit)
    }

    private suspend fun newEntity(note: NewNote, now: Long): Pair<NoteEntity, String> =
        withContext(computeDispatcher) {
            val summary = summarizeNoteText(note.title, note.body)
            NoteEntity(
                title = note.title,
                body = note.body,
                fallbackTitle = summary.fallbackTitle,
                preview = summary.preview,
                createdAt = now,
                editedAt = now,
                isPinned = note.isPinned,
            ) to stripMarkdown(note.body)
        }

    private inline fun <T> safeCall(block: () -> Result<T, DataError.Local>): Result<T, DataError.Local> =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toDataError())
        }
}

/**
 * FTS4 query that requires every term (AND is implicit) as a prefix. Terms from
 * `searchTerms` hold only letters, digits and combining marks, and are lowercase, so they can
 * never form FTS operators such as `OR` or `NEAR`.
 */
internal fun ftsMatchQuery(terms: List<String>): String = terms.joinToString(" ") { "$it*" }

private fun NoteTitle.text(): String = when (this) {
    is NoteTitle.Typed -> text
    is NoteTitle.Fallback -> text
    NoteTitle.Untitled -> ""
}

// SQLite reports a full disk as SQLITE_FULL, "database or disk is full".
private fun Exception.toDataError(): DataError.Local =
    if (message?.contains("full", ignoreCase = true) == true) DataError.Local.DISK_FULL else DataError.Local.UNKNOWN
