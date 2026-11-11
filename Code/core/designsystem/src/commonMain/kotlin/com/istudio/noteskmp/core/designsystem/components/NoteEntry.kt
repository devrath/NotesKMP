package com.istudio.noteskmp.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.istudio.noteskmp.core.designsystem.generated.resources.Res
import com.istudio.noteskmp.core.designsystem.generated.resources.note_entry_description
import com.istudio.noteskmp.core.designsystem.generated.resources.note_entry_description_pinned
import com.istudio.noteskmp.core.designsystem.generated.resources.note_entry_long_click_label
import com.istudio.noteskmp.core.designsystem.icon.NotesIcons
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesRadius
import com.istudio.noteskmp.core.designsystem.theme.NotesSpacing
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme
import org.jetbrains.compose.resources.stringResource

/** Where an entry sits in its group. iOS draws inset grouped rows from it; Android ignores it. */
enum class NoteEntryPosition { Single, First, Middle, Last }

/** From this font scale on, titles wrap instead of truncating (the 200% text check). */
private const val WRAP_TITLE_FONT_SCALE = 1.5f
private val PinIconSize = 16.dp

/**
 * One note in the list: one-line title, two-line preview, edit time and a pin icon.
 * Android draws an outlined card; iOS draws a row of an inset grouped list, so it reads
 * [position] to round the group's outer corners and draw the inset divider.
 *
 * @param isFallbackTitle the title was derived from the body, so it uses a lighter weight.
 * @param isSelected the long-press menu is open for this entry (Android).
 * @param isRecentlyImported briefly outlines a note that was just imported.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteEntry(
    title: String,
    preview: String,
    editedTime: String,
    isPinned: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFallbackTitle: Boolean = false,
    isSelected: Boolean = false,
    isRecentlyImported: Boolean = false,
    position: NoteEntryPosition = NoteEntryPosition.Single,
) {
    val isIos = NotesTheme.platform == NotesPlatform.Ios
    val colors = NotesTheme.colors
    val shape = if (isIos) position.shape() else RoundedCornerShape(NotesRadius.md)
    val description = stringResource(
        if (isPinned) Res.string.note_entry_description_pinned else Res.string.note_entry_description,
        title,
        editedTime,
        preview,
    )
    val longClickLabel = stringResource(Res.string.note_entry_long_click_label)

    Surface(
        shape = shape,
        color = if (isSelected && !isIos) colors.surface3 else colors.surface,
        border = when {
            isIos -> null
            isSelected -> BorderStroke(1.dp, colors.outlineStrong)
            else -> BorderStroke(1.dp, colors.outline)
        },
        modifier = modifier
            .fillMaxWidth()
            .then(if (isIos) Modifier.groupedRowOutline(position, colors.outline, colors.outlineStrong) else Modifier)
            .then(if (isRecentlyImported) Modifier.border(2.dp, colors.primary, shape) else Modifier)
            .clip(shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onLongClickLabel = longClickLabel,
            )
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
                onClick { onClick(); true }
                onLongClick(label = longClickLabel) { onLongClick(); true }
            },
    ) {
        Column(
            modifier = Modifier.padding(
                start = NotesSpacing.space4,
                end = NotesSpacing.space4,
                top = if (isIos) 11.dp else 14.dp,
                bottom = if (isIos) 11.dp else NotesSpacing.space3,
            ),
        ) {
            NoteEntryHead(title = title, isFallbackTitle = isFallbackTitle, isPinned = isPinned)
            Text(
                text = preview,
                style = NotesTheme.typography.label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = if (isIos) 3.dp else 2.dp),
            )
            Text(
                text = editedTime,
                style = NotesTheme.typography.meta,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = if (isIos) 6.dp else NotesSpacing.space2),
            )
        }
    }
}

@Composable
private fun NoteEntryHead(title: String, isFallbackTitle: Boolean, isPinned: Boolean) {
    val wrapTitle = LocalDensity.current.fontScale >= WRAP_TITLE_FONT_SCALE
    Row(
        horizontalArrangement = Arrangement.spacedBy(NotesSpacing.space2),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = title,
            style = NotesTheme.typography.listTitle.let {
                if (isFallbackTitle) it.copy(fontWeight = FontWeight.Medium) else it
            },
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (wrapTitle) Int.MAX_VALUE else 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (isPinned) {
            Icon(
                painter = NotesIcons.Pin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = NotesSpacing.space1).size(PinIconSize),
            )
        }
    }
}

/** Section label above a group of entries, such as "Pinned" and "Notes". */
@Composable
fun NoteGroupLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    val isIos = NotesTheme.platform == NotesPlatform.Ios
    Text(
        text = text,
        style = NotesTheme.typography.label.copy(
            fontWeight = if (isIos) FontWeight.SemiBold else FontWeight.Medium,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(
            start = NotesSpacing.space1,
            end = NotesSpacing.space1,
            top = if (isIos) 14.dp else NotesSpacing.space3,
            bottom = if (isIos) NotesSpacing.space2 else 2.dp,
        ),
    )
}

private fun NoteEntryPosition.shape(): Shape {
    val r = NotesRadius.md
    return when (this) {
        NoteEntryPosition.Single -> RoundedCornerShape(r)
        NoteEntryPosition.First -> RoundedCornerShape(topStart = r, topEnd = r)
        NoteEntryPosition.Middle -> RoundedCornerShape(0.dp)
        NoteEntryPosition.Last -> RoundedCornerShape(bottomStart = r, bottomEnd = r)
    }
}

/**
 * Draws this row's share of the group outline (outer edges only, so rows never double up a
 * line) plus the 0.5 pt inset divider above every row but the first.
 */
private fun Modifier.groupedRowOutline(
    position: NoteEntryPosition,
    outline: Color,
    divider: Color,
): Modifier = drawWithContent {
    drawContent()
    val stroke = 1.dp.toPx()
    val half = stroke / 2
    val r = NotesRadius.md.toPx()
    val w = size.width
    val h = size.height
    val roundTop = position == NoteEntryPosition.Single || position == NoteEntryPosition.First
    val roundBottom = position == NoteEntryPosition.Single || position == NoteEntryPosition.Last
    val path = Path().apply {
        // Left edge, bottom to top.
        moveTo(half, if (roundBottom) h - r else h)
        if (roundTop) {
            lineTo(half, r)
            arcTo(Rect(half, half, 2 * r - half, 2 * r - half), 180f, 90f, false)
            lineTo(w - r, half)
            arcTo(Rect(w - 2 * r + half, half, w - half, 2 * r - half), 270f, 90f, false)
        } else {
            lineTo(half, 0f)
            moveTo(w - half, 0f)
        }
        // Right edge, top to bottom.
        lineTo(w - half, if (roundBottom) h - r else h)
        if (roundBottom) {
            arcTo(Rect(w - 2 * r + half, h - 2 * r + half, w - half, h - half), 0f, 90f, false)
            lineTo(r, h - half)
            arcTo(Rect(half, h - 2 * r + half, 2 * r - half, h - half), 90f, 90f, false)
        }
    }
    drawPath(path, outline, style = Stroke(width = stroke))
    if (!roundTop) {
        drawLine(
            color = divider.copy(alpha = 0.45f),
            start = Offset(NotesSpacing.space4.toPx(), 0f),
            end = Offset(w, 0f),
            strokeWidth = 0.5.dp.toPx(),
        )
    }
}

@Composable
private fun EntriesSample() {
    Column(verticalArrangement = Arrangement.spacedBy(NotesSpacing.space2)) {
        NoteGroupLabel(text = "Pinned")
        NoteEntry(
            title = "Weekly plan",
            preview = "This week: focus on search and import errors. Ship the notes list, review import error copy, finish the search screen.",
            editedTime = "Yesterday",
            isPinned = true,
            onClick = {},
            onLongClick = {},
        )
        NoteGroupLabel(text = "Notes")
        NoteEntry(
            title = "Café ideas",
            preview = "Oat flat white with cardamom. Ask about the long table by the window for Saturday mornings.",
            editedTime = "Just now",
            isPinned = false,
            isSelected = true,
            onClick = {},
            onLongClick = {},
        )
        NoteEntry(
            title = "Groceries for the weekend",
            preview = "Lemons, oat milk, sourdough, basil, the good olive oil.",
            editedTime = "2 h ago",
            isPinned = false,
            isFallbackTitle = true,
            onClick = {},
            onLongClick = {},
        )
        NoteEntry(
            title = "Ideas",
            preview = "A notes app that keeps only the core: write, read, find. Maybe a highlighter as the logo?",
            editedTime = "Just now",
            isPinned = false,
            isRecentlyImported = true,
            onClick = {},
            onLongClick = {},
        )
    }
}

@Composable
private fun IosEntriesSample() {
    Column {
        NoteGroupLabel(text = "Pinned")
        NoteEntry(
            title = "Welcome to Notes",
            preview = "Write in Markdown, read it formatted, and find any note in seconds. Everything stays on this device.",
            editedTime = "Mar 4",
            isPinned = true,
            position = NoteEntryPosition.First,
            onClick = {},
            onLongClick = {},
        )
        NoteEntry(
            title = "Weekly plan",
            preview = "This week: focus on search and import errors. Ship the notes list, review import error copy.",
            editedTime = "Yesterday",
            isPinned = true,
            position = NoteEntryPosition.Last,
            onClick = {},
            onLongClick = {},
        )
        NoteGroupLabel(text = "Notes")
        NoteEntry(
            title = "Groceries for the weekend",
            preview = "Lemons, oat milk, sourdough, basil, the good olive oil.",
            editedTime = "2 h ago",
            isPinned = false,
            isFallbackTitle = true,
            position = NoteEntryPosition.Single,
            onClick = {},
            onLongClick = {},
        )
    }
}

@Preview(name = "Android", widthDp = 360)
@Composable
private fun NoteEntryPreview() {
    NotesPreview { EntriesSample() }
}

@Preview(name = "Android dark", widthDp = 360)
@Composable
private fun NoteEntryDarkPreview() {
    NotesPreview(darkTheme = true) { EntriesSample() }
}

@Preview(name = "Android, long title", widthDp = 320)
@Composable
private fun NoteEntryLongTitlePreview() {
    NotesPreview {
        NoteEntry(
            title = "Meeting notes: Q4 roadmap with the whole product and design org",
            preview = "We agreed the plan keeps export per note. Search has to stay under 100 ms with ten thousand notes.",
            editedTime = "5 min ago",
            isPinned = true,
            onClick = {},
            onLongClick = {},
        )
    }
}

@Preview(name = "Android, 200% text", widthDp = 360)
@Composable
private fun NoteEntryLargeFontPreview() {
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, fontScale = 2f),
    ) {
        NotesPreview {
            NoteEntry(
                title = "Meeting notes: Q4 roadmap",
                preview = "We agreed the plan keeps export per note. Search has to stay under 100 ms with ten thousand notes.",
                editedTime = "5 min ago",
                isPinned = true,
                onClick = {},
                onLongClick = {},
            )
        }
    }
}

@Preview(name = "iOS", widthDp = 393)
@Composable
private fun NoteEntryIosPreview() {
    NotesPreview(platform = NotesPlatform.Ios) { IosEntriesSample() }
}

@Preview(name = "iOS dark", widthDp = 393)
@Composable
private fun NoteEntryIosDarkPreview() {
    NotesPreview(darkTheme = true, platform = NotesPlatform.Ios) { IosEntriesSample() }
}
