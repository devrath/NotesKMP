package com.istudio.noteskmp.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query(
        """
        SELECT id, title, fallbackTitle, preview, editedAt, isPinned FROM notes
        ORDER BY isPinned DESC, editedAt DESC, id DESC
        """,
    )
    fun observeSummaries(): Flow<List<NoteSummaryRow>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    /** Inserts the note and its index row in one transaction and returns the new id. */
    @Transaction
    suspend fun insertNote(note: NoteEntity, strippedBody: String): Long {
        val id = insert(note)
        insertFts(NoteFtsEntity(rowId = id, title = note.title, body = strippedBody))
        return id
    }

    /** Updates the text and the index together. Returns false if the note doesn't exist. */
    @Transaction
    suspend fun updateText(
        id: Long,
        title: String,
        body: String,
        fallbackTitle: String?,
        preview: String,
        strippedBody: String,
        editedAt: Long,
    ): Boolean {
        if (updateNoteText(id, title, body, fallbackTitle, preview, editedAt) == 0) return false
        updateFts(id, title, strippedBody)
        return true
    }

    @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean): Int

    @Transaction
    suspend fun deleteNote(id: Long): Boolean {
        val deleted = delete(id)
        deleteFts(id)
        return deleted > 0
    }

    @Query(
        """
        SELECT notes.id, notes.title, notes.fallbackTitle, notes_fts.body AS strippedBody,
            notes.editedAt, notes.isPinned
        FROM notes_fts JOIN notes ON notes.id = notes_fts.rowid
        WHERE notes_fts MATCH :match
        """,
    )
    suspend fun search(match: String): List<NoteSearchRow>

    /** Inserts [notes] and records [flagKey] in one transaction, unless the flag is already set. */
    @Transaction
    suspend fun insertOnce(flagKey: String, notes: List<Pair<NoteEntity, String>>): Boolean {
        if (getMeta(flagKey) != null) return false
        notes.forEach { (note, stripped) -> insertNote(note, stripped) }
        insertMeta(MetaEntity(flagKey, "1"))
        return true
    }

    @Query("SELECT value FROM app_meta WHERE `key` = :key")
    suspend fun getMeta(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeta(meta: MetaEntity)

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Insert
    suspend fun insertFts(row: NoteFtsEntity)

    @Query(
        """
        UPDATE notes SET title = :title, body = :body, fallbackTitle = :fallbackTitle,
            preview = :preview, editedAt = :editedAt
        WHERE id = :id
        """,
    )
    suspend fun updateNoteText(
        id: Long,
        title: String,
        body: String,
        fallbackTitle: String?,
        preview: String,
        editedAt: Long,
    ): Int

    @Query("UPDATE notes_fts SET title = :title, body = :body WHERE rowid = :id")
    suspend fun updateFts(id: Long, title: String, body: String)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM notes_fts WHERE rowid = :id")
    suspend fun deleteFts(id: Long)
}
