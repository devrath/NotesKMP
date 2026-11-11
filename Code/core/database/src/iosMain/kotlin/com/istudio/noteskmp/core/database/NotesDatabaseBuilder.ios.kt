package com.istudio.noteskmp.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/** The database lives in Documents, which the iCloud device backup includes. */
@OptIn(ExperimentalForeignApi::class)
fun notesDatabaseBuilder(): RoomDatabase.Builder<NotesDatabase> {
    val documents = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val path = requireNotNull(documents?.path) { "No documents directory" } + "/$DATABASE_FILE_NAME"
    return Room.databaseBuilder<NotesDatabase>(name = path)
}
