package com.istudio.noteskmp.feature.note.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.istudio.noteskmp.core.presentation.UiText
import com.istudio.noteskmp.core.presentation.file.ExportResult
import com.istudio.noteskmp.feature.note.domain.MarkdownBlock
import com.istudio.noteskmp.feature.note.domain.MarkdownFormat

enum class NoteMode { Edit, Preview }

data class NoteState(
    val isLoading: Boolean = true,
    val title: String = "",
    val body: TextFieldValue = TextFieldValue(""),
    val mode: NoteMode = NoteMode.Edit,
    val isPinned: Boolean = false,
    /** Shown quietly in the top bar after a save; hidden while there are unsaved changes. */
    val isSaved: Boolean = false,
    /** The parsed body, for preview mode. */
    val blocks: List<MarkdownBlock> = emptyList(),
    /**
     * Where in the Markdown the user is looking, as a character offset. Carried between
     * modes so both show the same part of the note, and set to the first search match.
     */
    val scrollAnchor: Int = 0,
    /** Bumped whenever the screen should scroll to [scrollAnchor]. */
    val scrollRequest: Int = 0,
    /** The body should take focus, for a new note or a double-tap into edit mode. */
    val focusBodyRequest: Boolean = false,
    val isMenuOpen: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
    /** Long-press menu for a link: which preview block it's in, and its address. */
    val linkMenu: LinkMenuUi? = null,
) {
    /** Title shown in the delete dialog. */
    val hasText: Boolean get() = title.isNotBlank() || body.text.isNotBlank()
}

data class LinkMenuUi(val blockIndex: Int, val url: String)

sealed interface NoteAction {
    data class OnTitleChange(val title: String) : NoteAction
    data class OnBodyChange(val value: TextFieldValue) : NoteAction
    data class OnFormatClick(val format: MarkdownFormat, val clipboardText: String?) : NoteAction

    /** [anchor] is the character offset at the top of the current mode's view. */
    data class OnToggleModeClick(val anchor: Int) : NoteAction

    /** Double-tap in preview: edit with the cursor at [offset]. */
    data class OnPreviewDoubleTap(val offset: Int) : NoteAction
    data class OnChecklistToggle(val checkboxOffset: Int) : NoteAction
    data object OnBodyFocused : NoteAction
    data object OnMenuClick : NoteAction
    data object OnMenuDismiss : NoteAction
    data object OnTogglePinClick : NoteAction
    data object OnExportClick : NoteAction
    data class OnExportFinished(val result: ExportResult) : NoteAction
    data object OnDeleteClick : NoteAction
    data object OnDeleteConfirm : NoteAction
    data object OnDeleteDismiss : NoteAction
    data class OnLinkLongPress(val blockIndex: Int, val url: String) : NoteAction
    data object OnLinkMenuDismiss : NoteAction

    /** The screen left the foreground: save now. */
    data object OnLeaveForeground : NoteAction
}

sealed interface NoteEvent {
    data class ShowSnackbar(val message: UiText) : NoteEvent
    data class Export(val fileName: String, val content: String) : NoteEvent

    /** The note is gone (deleted or missing); leave the screen. */
    data object Close : NoteEvent
}
