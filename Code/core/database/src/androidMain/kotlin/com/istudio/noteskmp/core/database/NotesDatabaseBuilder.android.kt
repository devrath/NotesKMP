package com.istudio.noteskmp.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/** The database lives in the app's database folder, which Android Auto Backup includes. */
fun notesDatabaseBuilder(context: Context): RoomDatabase.Builder<NotesDatabase> {
    val appContext = context.applicationContext
    val file = appContext.getDatabasePath(DATABASE_FILE_NAME)
    return Room.databaseBuilder<NotesDatabase>(context = appContext, name = file.absolutePath)
}
