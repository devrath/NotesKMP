package com.istudio.noteskmp.core.data.di

import com.istudio.noteskmp.core.data.RoomNoteRepository
import com.istudio.noteskmp.core.database.NotesDatabase
import com.istudio.noteskmp.core.domain.AppDispatchers
import com.istudio.noteskmp.core.domain.ApplicationScope
import com.istudio.noteskmp.core.domain.note.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.time.Clock

/** Provides the [NotesDatabase] built for the platform. */
expect val coreDataPlatformModule: Module

val coreDataModule = module {
    includes(coreDataPlatformModule)
    single { get<NotesDatabase>().noteDao }
    single<Clock> { Clock.System }
    single { ApplicationScope(CoroutineScope(SupervisorJob() + Dispatchers.Default)) }
    single { AppDispatchers(default = Dispatchers.Default) }
    single<NoteRepository> { RoomNoteRepository(get(), get(), get<AppDispatchers>().default) }
}
