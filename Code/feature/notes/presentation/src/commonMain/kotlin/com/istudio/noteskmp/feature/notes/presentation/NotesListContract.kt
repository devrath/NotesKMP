package com.istudio.noteskmp.feature.notes.presentation

import com.istudio.noteskmp.core.domain.note.NoteTitle
import com.istudio.noteskmp.core.presentation.UiText
import com.istudio.noteskmp.core.presentation.file.ExportResult
import com.istudio.noteskmp.core.presentation.file.PickedFile
import kotlin.time.Instant

data class NotesListState(
    /** True until the first list arrives, so the empty state doesn't flash on launch. */
    val isLoading: Boolean = true,
    val pinned: List<NoteEntryUi> = emptyList(),
    val others: List<NoteEntryUi> = emptyList(),
    /** Edit times are shown relative to this; it advances every minute. */
    val now: Instant = Instant.fromEpochMilliseconds(0),
    /** The entry whose long-press menu is open. */
    val menuNoteId: Long? = null,
    val pendingDelete: PendingDeleteUi? = null,
    /** A note just imported: outlined briefly, and the list scrolls to it. */
    val recentlyImportedId: Long? = null,
    val scrollToNoteId: Long? = null,
) {
    val isEmpty: Boolean get() = !isLoading && pinned.isEmpty() && others.isEmpty()
    val noteCount: Int get() = pinned.size + others.size
}

data class NoteEntryUi(
    val id: Long,
    val title: NoteTitle,
    val preview: String,
    val editedAt: Instant,
    val isPinned: Boolean,
)

data class PendingDeleteUi(val noteId: Long, val title: NoteTitle)

sealed interface NotesListAction {
    data class OnNoteLongClick(val noteId: Long) : NotesListAction
    data object OnMenuDismiss : NotesListAction
    data class OnTogglePinClick(val noteId: Long) : NotesListAction
    data class OnExportClick(val noteId: Long) : NotesListAction
    data class OnExportFinished(val result: ExportResult) : NotesListAction
    data class OnDeleteClick(val noteId: Long) : NotesListAction
    data object OnDeleteConfirm : NotesListAction
    data object OnDeleteDismiss : NotesListAction
    data class OnFilePicked(val file: PickedFile?) : NotesListAction
    data object OnScrolledToNote : NotesListAction
}

sealed interface NotesListEvent {
    /** A snackbar, optionally with an Open action for [openNoteId]. */
    data class ShowSnackbar(val message: UiText, val openNoteId: Long? = null) : NotesListEvent

    /** Hand the note to the platform save/share flow. */
    data class Export(val fileName: String, val content: String) : NotesListEvent
}
