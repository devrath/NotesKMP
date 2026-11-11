package com.istudio.noteskmp.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.istudio.noteskmp.core.designsystem.generated.resources.Res
import com.istudio.noteskmp.core.designsystem.generated.resources.checklist_item_description
import com.istudio.noteskmp.core.designsystem.icon.NotesIcons
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesSpacing
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme
import org.jetbrains.compose.resources.stringResource

private val BoxSize = 22.dp
private val BoxRadius = 6.dp
private val CheckIconSize = 16.dp
private val AndroidRowMinHeight = 48.dp
private val IosRowMinHeight = 44.dp

/**
 * A `- [ ]` item in the Markdown preview. The whole row toggles (48 dp / 44 pt). Done items
 * show a filled box with a check and muted text, so the state never relies on color alone.
 * Announced as "Checklist item, Buy milk", plus the checked state.
 */
@Composable
fun ChecklistItem(
    text: AnnotatedString,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(Res.string.checklist_item_description, text.text)
    val minHeight = if (NotesTheme.platform == NotesPlatform.Ios) IosRowMinHeight else AndroidRowMinHeight
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .toggleable(value = isChecked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(vertical = NotesSpacing.space1),
        horizontalArrangement = Arrangement.spacedBy(NotesSpacing.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChecklistBox(isChecked = isChecked)
        Text(
            text = text,
            style = NotesTheme.typography.body,
            color = if (isChecked) NotesTheme.colors.checkDone else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ChecklistBox(isChecked: Boolean) {
    val colors = NotesTheme.colors
    Surface(
        shape = RoundedCornerShape(BoxRadius),
        color = if (isChecked) colors.primary else Color.Transparent,
        contentColor = colors.onPrimary,
        border = BorderStroke(2.dp, if (isChecked) colors.primary else colors.outlineStrong),
        modifier = Modifier.size(BoxSize),
    ) {
        if (isChecked) {
            Box(contentAlignment = Alignment.Center) {
                Icon(painter = NotesIcons.Check, contentDescription = null, modifier = Modifier.size(CheckIconSize))
            }
        }
    }
}

@Composable
private fun ChecklistSample() {
    Column {
        ChecklistItem(text = AnnotatedString("Ship the notes list"), isChecked = true, onCheckedChange = {})
        ChecklistItem(text = AnnotatedString("Finish the search screen"), isChecked = false, onCheckedChange = {})
        ChecklistItem(
            text = AnnotatedString("Book the design review for Thursday and send the agenda to everyone"),
            isChecked = false,
            onCheckedChange = {},
        )
    }
}

@Preview(name = "Android", widthDp = 360)
@Composable
private fun ChecklistItemPreview() {
    NotesPreview { ChecklistSample() }
}

@Preview(name = "Android dark", widthDp = 360)
@Composable
private fun ChecklistItemDarkPreview() {
    NotesPreview(darkTheme = true) { ChecklistSample() }
}

@Preview(name = "iOS", widthDp = 393)
@Composable
private fun ChecklistItemIosPreview() {
    NotesPreview(platform = NotesPlatform.Ios) { ChecklistSample() }
}

@Preview(name = "iOS dark", widthDp = 393)
@Composable
private fun ChecklistItemIosDarkPreview() {
    NotesPreview(darkTheme = true, platform = NotesPlatform.Ios) { ChecklistSample() }
}
