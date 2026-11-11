package com.istudio.noteskmp.core.presentation

import androidx.compose.runtime.Composable
import com.istudio.noteskmp.core.domain.note.NoteTitle
import com.istudio.noteskmp.core.presentation.generated.resources.Res
import com.istudio.noteskmp.core.presentation.generated.resources.untitled
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/** The title as shown, with "Untitled" when the note has no text at all. */
@Composable
fun NoteTitle.asString(): String = when (this) {
    is NoteTitle.Typed -> text
    is NoteTitle.Fallback -> text
    NoteTitle.Untitled -> stringResource(Res.string.untitled)
}

suspend fun NoteTitle.asStringAsync(): String = when (this) {
    is NoteTitle.Typed -> text
    is NoteTitle.Fallback -> text
    NoteTitle.Untitled -> getString(Res.string.untitled)
}

/** Localized "Untitled", for places that need it outside composition. */
suspend fun untitledText(): String = getString(Res.string.untitled)
