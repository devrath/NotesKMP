package com.istudio.noteskmp.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.istudio.noteskmp.core.designsystem.components.NotesConfirmationDialog
import com.istudio.noteskmp.core.designsystem.components.NotesMenuItem
import com.istudio.noteskmp.core.designsystem.components.NotesMenuSeparator
import com.istudio.noteskmp.core.designsystem.icon.NotesIcons
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme
import com.istudio.noteskmp.core.domain.note.MARKDOWN_EXTENSION
import com.istudio.noteskmp.core.domain.note.exportContent
import com.istudio.noteskmp.core.domain.note.exportFileName
import com.istudio.noteskmp.core.domain.note.noteTitle
import com.istudio.noteskmp.core.domain.note.summarizeNoteText
import com.istudio.noteskmp.core.presentation.generated.resources.Res
import com.istudio.noteskmp.core.presentation.generated.resources.action_cancel
import com.istudio.noteskmp.core.presentation.generated.resources.action_delete
import com.istudio.noteskmp.core.presentation.generated.resources.action_export
import com.istudio.noteskmp.core.presentation.generated.resources.action_pin
import com.istudio.noteskmp.core.presentation.generated.resources.action_unpin
import com.istudio.noteskmp.core.presentation.generated.resources.delete_dialog_message
import com.istudio.noteskmp.core.presentation.generated.resources.delete_dialog_title
import com.istudio.noteskmp.core.presentation.generated.resources.export_failed
import com.istudio.noteskmp.core.presentation.generated.resources.export_succeeded
import com.istudio.noteskmp.core.presentation.file.ExportResult
import org.jetbrains.compose.resources.stringResource

/**
 * Pin or Unpin, Export as Markdown, and Delete, for a note's overflow menu or a list entry's
 * long-press menu. Place inside a `NotesDropdownMenu`. Delete is set apart and destructive.
 */
@Composable
fun NoteActionsMenuItems(
    isPinned: Boolean,
    onTogglePinClick: () -> Unit,
    onExportClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    NotesMenuItem(
        text = stringResource(if (isPinned) Res.string.action_unpin else Res.string.action_pin),
        icon = if (isPinned) NotesIcons.PinOff else NotesIcons.Pin,
        onClick = onTogglePinClick,
    )
    NotesMenuItem(
        text = stringResource(Res.string.action_export),
        icon = NotesIcons.Export,
        onClick = onExportClick,
    )
    NotesMenuSeparator()
    NotesMenuItem(
        text = stringResource(Res.string.action_delete),
        icon = NotesIcons.Trash,
        onClick = onDeleteClick,
        isDestructive = true,
    )
}

/** "Delete “title”?" with Cancel (default focus) and a destructive Delete. */
@Composable
fun DeleteNoteDialog(
    noteTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    NotesConfirmationDialog(
        title = stringResource(Res.string.delete_dialog_title, noteTitle),
        text = stringResource(Res.string.delete_dialog_message),
        confirmLabel = stringResource(Res.string.action_delete),
        dismissLabel = stringResource(Res.string.action_cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

/** A note ready to hand to the exporter. */
data class NoteExport(val fileName: String, val content: String)

/** File name from [title] (or the fallback title) and [body] as it is exported. */
suspend fun noteExport(title: String, body: String): NoteExport {
    val shownTitle = noteTitle(title, summarizeNoteText(title, body).fallbackTitle).asStringAsync()
    // Only look up "Untitled" when the title leaves nothing usable.
    val fileName = exportFileName(shownTitle, untitled = "").takeUnless { it == MARKDOWN_EXTENSION }
        ?: exportFileName(untitledText(), untitled = "")
    return NoteExport(fileName = fileName, content = exportContent(body))
}

/**
 * The snackbar for an export outcome, or null when there's nothing to say: the user
 * cancelled, or the share sheet confirms the outcome itself.
 */
fun ExportResult.toUiText(fileName: String): UiText? = when (this) {
    ExportResult.Saved -> UiText.Resource(Res.string.export_succeeded, arrayOf(fileName))
    ExportResult.Failed -> UiText.Resource(Res.string.export_failed)
    ExportResult.Shared, ExportResult.Cancelled -> null
}

@Preview
@Composable
private fun DeleteNoteDialogPreview() {
    NotesTheme {
        DeleteNoteDialog(noteTitle = "Weekly plan", onConfirm = {}, onDismiss = {})
    }
}
