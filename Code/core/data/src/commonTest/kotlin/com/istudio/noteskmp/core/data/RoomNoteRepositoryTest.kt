package com.istudio.noteskmp.core.data

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import com.istudio.noteskmp.core.database.MetaEntity
import com.istudio.noteskmp.core.database.NoteDao
import com.istudio.noteskmp.core.database.NoteEntity
import com.istudio.noteskmp.core.database.NoteFtsEntity
import com.istudio.noteskmp.core.database.NoteSearchRow
import com.istudio.noteskmp.core.database.NoteSummaryRow
import com.istudio.noteskmp.core.domain.DataError
import com.istudio.noteskmp.core.domain.Result
import com.istudio.noteskmp.core.domain.note.NewNote
import com.istudio.noteskmp.core.domain.note.NoteTitle
import com.istudio.noteskmp.core.testing.FakeClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

class RoomNoteRepositoryTest {

    private val clock = FakeClock()
    private val dao = FakeNoteDao()
    private val repository = RoomNoteRepository(dao, clock, UnconfinedTestDispatcher())

    @Test
    fun `create stores the text exactly and derives list columns and the index`() = runTest {
        val body = "# Groceries\r\n**oat milk**  \n"
        val id = (repository.createNote(NewNote("", body)) as Result.Success).data

        val stored = dao.notes.value.getValue(id)
        assertThat(stored.body).isEqualTo(body)
        assertThat(stored.fallbackTitle).isEqualTo("Groceries")
        assertThat(stored.preview).isEqualTo("oat milk")
        assertThat(stored.createdAt).isEqualTo(clock.now.toEpochMilliseconds())
        assertThat(dao.fts.getValue(id)).isEqualTo(NoteFtsEntity(id, "", "Groceries\noat milk\n"))
    }

    @Test
    fun `update changes the edit time but pinning does not`() = runTest {
        val id = (repository.createNote(NewNote("T", "a")) as Result.Success).data
        val created = clock.now.toEpochMilliseconds()
        clock.advance(5.minutes)

        repository.setPinned(id, true)
        assertThat(dao.notes.value.getValue(id).editedAt).isEqualTo(created)

        repository.updateNote(id, "T", "b")
        assertThat(dao.notes.value.getValue(id).editedAt).isEqualTo(clock.now.toEpochMilliseconds())
        assertThat(dao.notes.value.getValue(id).isPinned).isEqualTo(true)
        assertThat(dao.fts.getValue(id).body).isEqualTo("b")
    }

    @Test
    fun `missing notes report not found`() = runTest {
        assertThat(repository.getNote(9)).isEqualTo(Result.Error(DataError.Local.NOT_FOUND))
        assertThat(repository.updateNote(9, "", "")).isEqualTo(Result.Error(DataError.Local.NOT_FOUND))
        assertThat(repository.setPinned(9, true)).isEqualTo(Result.Error(DataError.Local.NOT_FOUND))
        assertThat(repository.deleteNote(9)).isEqualTo(Result.Error(DataError.Local.NOT_FOUND))
    }

    @Test
    fun `database exceptions become typed errors`() = runTest {
        dao.failure = IllegalStateException("database or disk is full")
        assertThat(repository.getNote(1)).isEqualTo(Result.Error(DataError.Local.DISK_FULL))

        dao.failure = IllegalStateException("boom")
        assertThat(repository.createNote(NewNote("a", "b"))).isEqualTo(Result.Error(DataError.Local.UNKNOWN))
    }

    @Test
    fun `delete removes the note and its index row`() = runTest {
        val id = (repository.createNote(NewNote("T", "a")) as Result.Success).data
        repository.deleteNote(id)

        assertThat(dao.notes.value[id]).isNull()
        assertThat(dao.fts[id]).isNull()
    }

    @Test
    fun `list maps titles with fallback and untitled`() = runTest {
        repository.createNote(NewNote("", ""))
        clock.advance(1.minutes)
        repository.createNote(NewNote("", "First line"))
        clock.advance(1.minutes)
        repository.createNote(NewNote("Typed", "x"))

        repository.observeNotes().test {
            assertThat(awaitItem().map { it.title }).containsExactly(
                NoteTitle.Typed("Typed"),
                NoteTitle.Fallback("First line"),
                NoteTitle.Untitled,
            )
        }
    }

    @Test
    fun `search requires every term as a prefix and ranks title matches first`() = runTest {
        val bodyOnly = (repository.createNote(NewNote("Groceries", "plan the week")) as Result.Success).data
        clock.advance(1.minutes)
        val newestBody = (repository.createNote(NewNote("Misc", "plans")) as Result.Success).data
        clock.advance(1.minutes)
        val titled = (repository.createNote(NewNote("Weekly plan", "")) as Result.Success).data
        clock.advance(1.minutes)
        // Newer, but only a body match, so it ranks below the title match.
        repository.updateNote(newestBody, "Misc", "plans")

        val hits = (repository.search(listOf("plan")) as Result.Success).data

        assertThat(dao.lastMatch).isEqualTo("plan*")
        assertThat(hits.map { it.id }).containsExactly(titled, newestBody, bodyOnly)
    }

    @Test
    fun `empty terms search nothing`() = runTest {
        assertThat(repository.search(emptyList())).isEqualTo(Result.Success(emptyList()))
        assertThat(dao.lastMatch).isNull()
    }

    @Test
    fun `fts query ands every term as a prefix`() {
        assertThat(ftsMatchQuery(listOf("cafe", "meet"))).isEqualTo("cafe* meet*")
    }

    @Test
    fun `samples are created once - first sample on top - and never again`() = runTest {
        val samples = listOf(NewNote("Welcome", "w", isPinned = true), NewNote("Cheat", "c"), NewNote("Tips", "t"))
        repository.createSampleNotesOnFirstLaunch { samples }
        dao.notes.value = emptyMap()
        var askedAgain = false
        repository.createSampleNotesOnFirstLaunch { askedAgain = true; samples }

        assertThat(askedAgain).isEqualTo(false)
        assertThat(dao.notes.value).isEqualTo(emptyMap())
    }

    @Test
    fun `samples keep their order in the list`() = runTest {
        repository.createSampleNotesOnFirstLaunch {
            listOf(NewNote("Welcome", "w", isPinned = true), NewNote("Cheat", "c"), NewNote("Tips", "t"))
        }

        repository.observeNotes().test {
            assertThat(awaitItem().map { (it.title as NoteTitle.Typed).text }).containsExactly("Welcome", "Cheat", "Tips")
        }
    }

    @Test
    fun `get maps the entity`() = runTest {
        val id = (repository.createNote(NewNote("T", "b")) as Result.Success).data

        assertThat(repository.getNote(id)).isInstanceOf<Result.Success<*>>()
    }
}

/** In-memory DAO. The transactional defaults of [NoteDao] run on top of these. */
private class FakeNoteDao : NoteDao {
    val notes = MutableStateFlow<Map<Long, NoteEntity>>(emptyMap())
    val fts = mutableMapOf<Long, NoteFtsEntity>()
    private val meta = mutableMapOf<String, String>()
    private var nextId = 1L
    var failure: Exception? = null
    var lastMatch: String? = null

    private fun check() {
        failure?.let { throw it }
    }

    override fun observeSummaries(): Flow<List<NoteSummaryRow>> = notes.map { map ->
        map.values.sortedWith(compareByDescending<NoteEntity> { it.isPinned }.thenByDescending { it.editedAt }.thenByDescending { it.id })
            .map { NoteSummaryRow(it.id, it.title, it.fallbackTitle, it.preview, it.editedAt, it.isPinned) }
    }

    override suspend fun getById(id: Long): NoteEntity? {
        check()
        return notes.value[id]
    }

    override suspend fun setPinned(id: Long, isPinned: Boolean): Int {
        check()
        val note = notes.value[id] ?: return 0
        notes.value += id to note.copy(isPinned = isPinned)
        return 1
    }

    /** Matches when every `term*` is a word prefix in the title or indexed body. */
    override suspend fun search(match: String): List<NoteSearchRow> {
        check()
        lastMatch = match
        val terms = match.split(' ').map { it.removeSuffix("*") }
        return fts.values.filter { row ->
            val words = (row.title + " " + row.body).lowercase().split(Regex("[^\\p{L}\\p{N}]+"))
            terms.all { term -> words.any { it.startsWith(term) } }
        }.map { row ->
            val note = notes.value.getValue(row.rowId)
            NoteSearchRow(note.id, note.title, note.fallbackTitle, row.body, note.editedAt, note.isPinned)
        }
    }

    override suspend fun getMeta(key: String): String? = meta[key]

    override suspend fun insertMeta(meta: MetaEntity) {
        this.meta[meta.key] = meta.value
    }

    override suspend fun insert(note: NoteEntity): Long {
        check()
        val id = nextId++
        notes.value += id to note.copy(id = id)
        return id
    }

    override suspend fun insertFts(row: NoteFtsEntity) {
        fts[row.rowId] = row
    }

    override suspend fun updateNoteText(
        id: Long,
        title: String,
        body: String,
        fallbackTitle: String?,
        preview: String,
        editedAt: Long,
    ): Int {
        check()
        val note = notes.value[id] ?: return 0
        notes.value += id to note.copy(title = title, body = body, fallbackTitle = fallbackTitle, preview = preview, editedAt = editedAt)
        return 1
    }

    override suspend fun updateFts(id: Long, title: String, body: String) {
        fts[id] = NoteFtsEntity(id, title, body)
    }

    override suspend fun delete(id: Long): Int {
        check()
        val existed = id in notes.value
        notes.value -= id
        return if (existed) 1 else 0
    }

    override suspend fun deleteFts(id: Long) {
        fts.remove(id)
    }
}
