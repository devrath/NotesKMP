package com.istudio.noteskmp.feature.notes.presentation

import com.istudio.noteskmp.core.domain.note.NewNote
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.Res
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.sample_cheat_sheet_body
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.sample_cheat_sheet_title
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.sample_tips_body
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.sample_tips_title
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.sample_welcome_body
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.sample_welcome_title
import org.jetbrains.compose.resources.getString

/** The notes created on first launch, in list order. */
fun interface SampleNotes {
    suspend fun create(): List<NewNote>
}

/** Sample notes in the device language, from string resources. */
class ResourceSampleNotes : SampleNotes {
    override suspend fun create(): List<NewNote> = listOf(
        NewNote(getString(Res.string.sample_welcome_title), getString(Res.string.sample_welcome_body), isPinned = true),
        NewNote(getString(Res.string.sample_cheat_sheet_title), getString(Res.string.sample_cheat_sheet_body)),
        NewNote(getString(Res.string.sample_tips_title), getString(Res.string.sample_tips_body)),
    )
}
