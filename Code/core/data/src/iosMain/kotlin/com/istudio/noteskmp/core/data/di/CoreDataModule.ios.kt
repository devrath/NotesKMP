package com.istudio.noteskmp.core.data.di

import com.istudio.noteskmp.core.database.createNotesDatabase
import com.istudio.noteskmp.core.database.notesDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual val coreDataPlatformModule: Module = module {
    single { createNotesDatabase(notesDatabaseBuilder()) }
}
