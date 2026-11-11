package com.istudio.noteskmp.feature.notes.presentation

import com.istudio.noteskmp.core.domain.note.NoteTitle
import com.istudio.noteskmp.core.presentation.UiText
import com.istudio.noteskmp.core.presentation.asStringAsync
import com.istudio.noteskmp.feature.notes.domain.ImportError
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.Res
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.import_error_not_text
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.import_error_too_large
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.import_error_unreadable
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.import_succeeded

fun ImportError.toUiText(): UiText = UiText.Resource(
    when (this) {
        ImportError.TOO_LARGE -> Res.string.import_error_too_large
        ImportError.NOT_TEXT -> Res.string.import_error_not_text
        ImportError.UNREADABLE -> Res.string.import_error_unreadable
    },
)

/** "Imported “Ideas”". */
internal suspend fun importedMessage(title: NoteTitle): UiText =
    UiText.Resource(Res.string.import_succeeded, arrayOf(title.asStringAsync()))
