package com.istudio.noteskmp.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal const val DATABASE_FILE_NAME = "notes.db"

/**
 * The notes database. Bump [version] for every schema change, add the migration (prefer
 * `autoMigrations`), and cover it with a migration test. Schemas are exported to `schemas/`.
 */
@Database(
    entities = [NoteEntity::class, NoteFtsEntity::class, MetaEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(NotesDatabaseConstructor::class)
abstract class NotesDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao
}

// Room's KSP processor generates the actual implementations.
@Suppress("KotlinNoActualForExpect")
expect object NotesDatabaseConstructor : RoomDatabaseConstructor<NotesDatabase> {
    override fun initialize(): NotesDatabase
}

/** Builds the database on the bundled SQLite driver. Queries run off the main thread. */
fun createNotesDatabase(builder: RoomDatabase.Builder<NotesDatabase>): NotesDatabase = builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()
