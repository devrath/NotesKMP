package com.istudio.noteskmp.feature.notes.presentation.di

import com.istudio.noteskmp.feature.notes.presentation.NotesListViewModel
import com.istudio.noteskmp.feature.notes.presentation.ResourceSampleNotes
import com.istudio.noteskmp.feature.notes.presentation.SampleNotes
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val notesPresentationModule = module {
    singleOf(::ResourceSampleNotes) { bind<SampleNotes>() }
    viewModelOf(::NotesListViewModel)
}
