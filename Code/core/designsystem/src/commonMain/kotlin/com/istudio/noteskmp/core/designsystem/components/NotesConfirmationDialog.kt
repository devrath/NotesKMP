package com.istudio.noteskmp.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesRadius
import com.istudio.noteskmp.core.designsystem.theme.NotesSpacing
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme

private val IosAlertWidth = 270.dp
private val IosAlertRadius = 16.dp
private val IosAlertButtonHeight = 44.dp

/**
 * Confirmation such as "Delete “Weekly plan”?". Android: Material 3 basic dialog. iOS: alert
 * with side-by-side actions and the dismiss action in bold. The dismiss action has default
 * focus, and dismissing the dialog any other way calls [onDismiss] too.
 */
@Composable
fun NotesConfirmationDialog(
    title: String,
    text: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = true,
) {
    if (NotesTheme.platform == NotesPlatform.Ios) {
        IosAlert(title, text, confirmLabel, dismissLabel, onConfirm, onDismiss, isDestructive, modifier)
    } else {
        AndroidDialog(title, text, confirmLabel, dismissLabel, onConfirm, onDismiss, isDestructive, modifier)
    }
}

@Composable
private fun AndroidDialog(
    title: String,
    text: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean,
    modifier: Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(NotesRadius.xl),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(text = title, style = NotesTheme.typography.dialogTitle) },
        text = {
            Text(
                text = text,
                style = NotesTheme.typography.label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        dismissButton = {
            NotesTextButton(text = dismissLabel, onClick = onDismiss, modifier = Modifier.defaultFocus())
        },
        confirmButton = {
            NotesTextButton(text = confirmLabel, onClick = onConfirm, isDestructive = isDestructive)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IosAlert(
    title: String,
    text: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean,
    modifier: Modifier,
) {
    BasicAlertDialog(onDismissRequest = onDismiss, modifier = modifier) {
        IosAlertContent(title, text, confirmLabel, dismissLabel, onConfirm, onDismiss, isDestructive, Modifier.defaultFocus())
    }
}

@Composable
private fun IosAlertContent(
    title: String,
    text: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean,
    dismissModifier: Modifier = Modifier,
) {
    val divider = NotesTheme.colors.outlineStrong
    Surface(
        shape = RoundedCornerShape(IosAlertRadius),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.width(IosAlertWidth),
    ) {
        Column {
            Column(modifier = Modifier.padding(start = NotesSpacing.space4, end = NotesSpacing.space4, top = NotesSpacing.space5, bottom = 18.dp)) {
                Text(
                    text = title,
                    style = NotesTheme.typography.dialogTitle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = text,
                    style = NotesTheme.typography.meta,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = NotesSpacing.space1),
                )
            }
            HorizontalDivider(thickness = 0.5.dp, color = divider)
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                IosAlertButton(
                    text = dismissLabel,
                    onClick = onDismiss,
                    isDefault = true,
                    isDestructive = false,
                    modifier = dismissModifier.weight(1f),
                )
                VerticalDivider(thickness = 0.5.dp, color = divider, modifier = Modifier.fillMaxHeight())
                IosAlertButton(
                    text = confirmLabel,
                    onClick = onConfirm,
                    isDefault = false,
                    isDestructive = isDestructive,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun IosAlertButton(
    text: String,
    onClick: () -> Unit,
    isDefault: Boolean,
    isDestructive: Boolean,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        shape = RectangleShape,
        modifier = modifier.heightIn(min = IosAlertButtonHeight),
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        ),
    ) {
        Text(
            text = text,
            style = NotesTheme.typography.body.copy(fontWeight = if (isDefault) FontWeight.SemiBold else FontWeight.Normal),
        )
    }
}

/** Requests focus once the dialog's content is composed, so the safe action is the default. */
@Composable
private fun Modifier.defaultFocus(): Modifier {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
    return focusRequester(focusRequester)
}

// Dialogs open in their own window, which previews don't render, so these show the content.

@Preview(name = "Android")
@Composable
private fun NotesConfirmationDialogPreview() {
    NotesPreview {
        NotesConfirmationDialog(
            title = "Delete “Weekly plan”?",
            text = "This note will be deleted permanently. This can’t be undone.",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "Android dark")
@Composable
private fun NotesConfirmationDialogDarkPreview() {
    NotesPreview(darkTheme = true) {
        NotesConfirmationDialog(
            title = "Delete “Weekly plan”?",
            text = "This note will be deleted permanently. This can’t be undone.",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "iOS alert")
@Composable
private fun NotesConfirmationDialogIosPreview() {
    NotesPreview(platform = NotesPlatform.Ios) {
        IosAlertContent(
            title = "Delete “Weekly plan”?",
            text = "This note will be deleted permanently. This can’t be undone.",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            onConfirm = {},
            onDismiss = {},
            isDestructive = true,
        )
    }
}

@Preview(name = "iOS alert dark")
@Composable
private fun NotesConfirmationDialogIosDarkPreview() {
    NotesPreview(darkTheme = true, platform = NotesPlatform.Ios) {
        IosAlertContent(
            title = "Delete “Weekly plan”?",
            text = "This note will be deleted permanently. This can’t be undone.",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            onConfirm = {},
            onDismiss = {},
            isDestructive = true,
        )
    }
}
