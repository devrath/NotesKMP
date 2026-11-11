package com.istudio.noteskmp.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.istudio.noteskmp.core.designsystem.icon.NotesIcons
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesRadius
import com.istudio.noteskmp.core.designsystem.theme.NotesSpacing
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme

private val ButtonIconSize = 20.dp
private val IosButtonRadius = 14.dp
private val IosButtonHeight = 50.dp
private val AndroidButtonHeight = 48.dp
private val ComposeButtonSize = 50.dp

/** Android create button: marker-yellow extended FAB, 56 dp, with a plus icon. */
@Composable
fun NotesExtendedFab(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(NotesRadius.lg),
        containerColor = NotesTheme.colors.accent,
        contentColor = NotesTheme.colors.onAccent,
        icon = { Icon(painter = NotesIcons.Plus, contentDescription = null) },
        text = { Text(text = text, style = NotesTheme.typography.button) },
    )
}

/** iOS create button: a 50 pt marker-yellow circle with the compose icon, for the bottom toolbar. */
@Composable
fun NotesComposeButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier
            .shadow(elevation = 1.dp, shape = CircleShape)
            .size(ComposeButtonSize),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = NotesTheme.colors.accent,
            contentColor = NotesTheme.colors.onAccent,
        ),
    ) {
        Icon(painter = NotesIcons.Compose, contentDescription = contentDescription)
    }
}

/** Primary filled button in marker yellow, used by empty states. */
@Composable
fun NotesAccentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = buttonHeight()),
        shape = buttonShape(),
        colors = ButtonDefaults.buttonColors(
            containerColor = NotesTheme.colors.accent,
            contentColor = NotesTheme.colors.onAccent,
        ),
        contentPadding = ButtonPadding,
    ) {
        ButtonContent(text = text, icon = icon)
    }
}

/** Secondary button with a strong outline, used by empty states. */
@Composable
fun NotesOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = buttonHeight()),
        shape = buttonShape(),
        border = BorderStroke(1.dp, NotesTheme.colors.outlineStrong),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
        contentPadding = ButtonPadding,
    ) {
        ButtonContent(text = text, icon = icon)
    }
}

/** Dialog and snackbar-style text button. Destructive actions use the danger color. */
@Composable
fun NotesTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge.copy(fontWeight = NotesTheme.typography.button.fontWeight))
    }
}

private val ButtonPadding = PaddingValues(horizontal = NotesSpacing.space5)

@Composable
private fun buttonHeight() = if (NotesTheme.platform == NotesPlatform.Ios) IosButtonHeight else AndroidButtonHeight

@Composable
private fun buttonShape(): Shape =
    if (NotesTheme.platform == NotesPlatform.Ios) RoundedCornerShape(IosButtonRadius) else CircleShape

@Composable
private fun RowScope.ButtonContent(text: String, icon: Painter?) {
    if (icon != null) {
        Icon(painter = icon, contentDescription = null, modifier = Modifier.size(ButtonIconSize))
        Spacer(modifier = Modifier.width(NotesSpacing.space2))
    }
    Text(text = text, style = NotesTheme.typography.button)
}

@Composable
private fun ButtonsSample() {
    Column(verticalArrangement = Arrangement.spacedBy(NotesSpacing.space3)) {
        NotesExtendedFab(text = "New note", onClick = {})
        NotesComposeButton(contentDescription = "New note", onClick = {})
        Column(
            modifier = Modifier.widthIn(max = 240.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            NotesAccentButton(text = "New note", icon = NotesIcons.Plus, onClick = {}, modifier = Modifier.fillMaxWidth())
            NotesOutlinedButton(text = "Import file", icon = NotesIcons.Import, onClick = {}, modifier = Modifier.fillMaxWidth())
        }
        Row {
            NotesTextButton(text = "Cancel", onClick = {})
            NotesTextButton(text = "Delete", onClick = {}, isDestructive = true)
        }
    }
}

@Preview(name = "Android")
@Composable
private fun NotesButtonsPreview() {
    NotesPreview { ButtonsSample() }
}

@Preview(name = "Android dark")
@Composable
private fun NotesButtonsDarkPreview() {
    NotesPreview(darkTheme = true) { ButtonsSample() }
}

@Preview(name = "iOS")
@Composable
private fun NotesButtonsIosPreview() {
    NotesPreview(platform = NotesPlatform.Ios) { ButtonsSample() }
}

@Preview(name = "iOS dark")
@Composable
private fun NotesButtonsIosDarkPreview() {
    NotesPreview(darkTheme = true, platform = NotesPlatform.Ios) { ButtonsSample() }
}
