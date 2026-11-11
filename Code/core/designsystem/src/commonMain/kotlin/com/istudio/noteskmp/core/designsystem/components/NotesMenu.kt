package com.istudio.noteskmp.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.istudio.noteskmp.core.designsystem.icon.NotesIcons
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesRadius
import com.istudio.noteskmp.core.designsystem.theme.NotesSpacing
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme

private val AndroidMenuMinWidth = 200.dp
private val IosMenuWidth = 254.dp
private val IosMenuRadius = 14.dp
private val IosMenuIconSize = 20.dp

/**
 * Long-press and overflow menu. Android: Material dropdown. iOS: pull-down styling with
 * labels first and icons trailing.
 */
@Composable
fun NotesDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val isIos = NotesTheme.platform == NotesPlatform.Ios
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = offset,
        shape = RoundedCornerShape(if (isIos) IosMenuRadius else NotesRadius.xs),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.widthIn(min = if (isIos) IosMenuWidth else AndroidMenuMinWidth),
        content = content,
    )
}

/**
 * Menu action. [isDestructive] uses the danger color; destructive items also carry their
 * icon and sit after a [NotesMenuSeparator], so meaning never relies on color alone.
 */
@Composable
fun NotesMenuItem(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
) {
    val isIos = NotesTheme.platform == NotesPlatform.Ios
    val content = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val iconColor = when {
        isDestructive -> MaterialTheme.colorScheme.error
        isIos -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val iconSlot: @Composable () -> Unit = {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = if (isIos) Modifier.size(IosMenuIconSize) else Modifier,
        )
    }
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = if (isIos) NotesTheme.typography.body else NotesTheme.typography.label,
            )
        },
        onClick = onClick,
        modifier = modifier,
        leadingIcon = if (isIos) null else iconSlot,
        trailingIcon = if (isIos) iconSlot else null,
        colors = MenuDefaults.itemColors(
            textColor = content,
            leadingIconColor = iconColor,
            trailingIconColor = iconColor,
        ),
        contentPadding = if (isIos) {
            PaddingValues(horizontal = NotesSpacing.space4)
        } else {
            PaddingValues(start = NotesSpacing.space3, end = NotesSpacing.space4)
        },
    )
}

/** Sets the destructive action apart: a hairline on Android, a wide gap band on iOS. */
@Composable
fun NotesMenuSeparator(modifier: Modifier = Modifier) {
    if (NotesTheme.platform == NotesPlatform.Ios) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(NotesSpacing.space2)
                .background(NotesTheme.colors.outline.copy(alpha = 0.7f)),
        )
    } else {
        HorizontalDivider(
            color = NotesTheme.colors.outline,
            modifier = modifier.padding(vertical = NotesSpacing.space2),
        )
    }
}

/** Non-interactive header line, such as the link address above Copy link and Open. */
@Composable
fun NotesMenuHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = NotesTheme.typography.meta,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.padding(horizontal = NotesSpacing.space4, vertical = NotesSpacing.space3),
    )
}

@Composable
private fun MenuSample() {
    Box {
        NotesDropdownMenu(expanded = true, onDismissRequest = {}) {
            NotesMenuItem(text = "Pin", icon = NotesIcons.Pin, onClick = {})
            NotesMenuItem(text = "Export as Markdown", icon = NotesIcons.Export, onClick = {})
            NotesMenuSeparator()
            NotesMenuItem(text = "Delete", icon = NotesIcons.Trash, onClick = {}, isDestructive = true)
        }
    }
}

@Composable
private fun LinkMenuSample() {
    Box {
        NotesDropdownMenu(expanded = true, onDismissRequest = {}) {
            NotesMenuHeader(text = "https://spec.commonmark.org")
            NotesMenuItem(text = "Copy link", icon = NotesIcons.Copy, onClick = {})
            NotesMenuItem(text = "Open", icon = NotesIcons.OpenExternal, onClick = {})
        }
    }
}

@Preview(name = "Android")
@Composable
private fun NotesMenuPreview() {
    NotesPreview { MenuSample() }
}

@Preview(name = "Android dark")
@Composable
private fun NotesMenuDarkPreview() {
    NotesPreview(darkTheme = true) { MenuSample() }
}

@Preview(name = "Android link menu")
@Composable
private fun NotesLinkMenuPreview() {
    NotesPreview { LinkMenuSample() }
}

@Preview(name = "iOS")
@Composable
private fun NotesMenuIosPreview() {
    NotesPreview(platform = NotesPlatform.Ios) { MenuSample() }
}

@Preview(name = "iOS dark")
@Composable
private fun NotesMenuIosDarkPreview() {
    NotesPreview(darkTheme = true, platform = NotesPlatform.Ios) { MenuSample() }
}
