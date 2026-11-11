package com.istudio.noteskmp.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A note. [title] and [body] are stored exactly as typed. [fallbackTitle] and [preview] are
 * derived from them on every write so the list never has to read full bodies.
 */
@Entity(
    tableName = "notes",
    indices = [Index(value = ["isPinned", "editedAt"])],
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val fallbackTitle: String?,
    val preview: String,
    /** Epoch milliseconds. */
    val createdAt: Long,
    /** Epoch milliseconds. Changes only when the title or body text changes. */
    val editedAt: Long,
    val isPinned: Boolean,
)

/**
 * Full-text index over each note's title and its body with Markdown stripped, keyed by the
 * note's id. `unicode61` with `remove_diacritics=2` folds case and diacritics ("cafe" finds
 * "Café").
 */
@Fts4(
    // Room KSP fails to resolve the default when it is left out ("Cannot find external
    // content entity class"). Any means a regular FTS table that owns its content.
    contentEntity = Any::class,
    tokenizer = FtsOptions.TOKENIZER_UNICODE61,
    tokenizerArgs = ["remove_diacritics=2"],
)
@Entity(tableName = "notes_fts")
data class NoteFtsEntity(
    @PrimaryKey @ColumnInfo(name = "rowid") val rowId: Long,
    val title: String,
    val body: String,
)

/** Small app-wide flags, such as whether the sample notes were ever created. */
@Entity(tableName = "app_meta")
data class MetaEntity(
    @PrimaryKey val key: String,
    val value: String,
)

/** The list's columns: everything but the full body. */
data class NoteSummaryRow(
    val id: Long,
    val title: String,
    val fallbackTitle: String?,
    val preview: String,
    val editedAt: Long,
    val isPinned: Boolean,
)

data class NoteSearchRow(
    val id: Long,
    val title: String,
    val fallbackTitle: String?,
    val strippedBody: String,
    val editedAt: Long,
    val isPinned: Boolean,
)
