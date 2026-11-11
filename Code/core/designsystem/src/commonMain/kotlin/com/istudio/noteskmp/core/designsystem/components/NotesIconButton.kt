package com.istudio.noteskmp.core.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import com.istudio.noteskmp.core.designsystem.icon.NotesIconSize
import com.istudio.noteskmp.core.designsystem.icon.NotesIcons
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme

/**
 * Icon-only button: 48 dp / 44 pt target, an accessible name, and a tooltip with that name on
 * long-press. Icons are ink on Android and tinted with the primary color on iOS, like the
 * platforms' own bar buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesIconButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isMuted: Boolean = false,
    shape: Shape = CircleShape,
) {
    val contentColor = when {
        isMuted -> MaterialTheme.colorScheme.onSurfaceVariant
        NotesTheme.platform == NotesPlatform.Ios -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onBackground
    }
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
        tooltip = { PlainTooltip { Text(contentDescription) } },
        state = rememberTooltipState(),
        modifier = modifier,
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent,
                contentColor = contentColor,
            ),
        ) {
            Icon(
                painter = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(NotesIconSize),
            )
        }
    }
}

@Preview(name = "Android")
@Composable
private fun NotesIconButtonPreview() {
    NotesPreview {
        Row {
            NotesIconButton(icon = NotesIcons.Search, contentDescription = "Search notes", onClick = {})
            NotesIconButton(icon = NotesIcons.Close, contentDescription = "Clear search", onClick = {}, isMuted = true)
            NotesIconButton(icon = NotesIcons.Edit, contentDescription = "Edit", onClick = {}, enabled = false)
        }
    }
}

@Preview(name = "Android dark")
@Composable
private fun NotesIconButtonDarkPreview() {
    NotesPreview(darkTheme = true) {
        Row {
            NotesIconButton(icon = NotesIcons.Search, contentDescription = "Search notes", onClick = {})
            NotesIconButton(icon = NotesIcons.Close, contentDescription = "Clear search", onClick = {}, isMuted = true)
            NotesIconButton(icon = NotesIcons.Edit, contentDescription = "Edit", onClick = {}, enabled = false)
        }
    }
}

@Preview(name = "iOS")
@Composable
private fun NotesIconButtonIosPreview() {
    NotesPreview(platform = NotesPlatform.Ios) {
        Row {
            NotesIconButton(icon = NotesIcons.Import, contentDescription = "Import Markdown file", onClick = {})
            NotesIconButton(icon = NotesIcons.MoreCircle, contentDescription = "More options", onClick = {})
        }
    }
}

@Preview(name = "iOS dark")
@Composable
private fun NotesIconButtonIosDarkPreview() {
    NotesPreview(darkTheme = true, platform = NotesPlatform.Ios) {
        Row {
            NotesIconButton(icon = NotesIcons.Import, contentDescription = "Import Markdown file", onClick = {})
            NotesIconButton(icon = NotesIcons.MoreCircle, contentDescription = "More options", onClick = {})
        }
    }
}
