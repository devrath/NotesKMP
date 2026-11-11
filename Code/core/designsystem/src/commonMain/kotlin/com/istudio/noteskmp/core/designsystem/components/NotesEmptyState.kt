package com.istudio.noteskmp.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.istudio.noteskmp.core.designsystem.icon.NotesIcons
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesSpacing
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme

private val GlyphSize = 72.dp
private val GlyphRadius = 22.dp
private val GlyphIconSize = 32.dp
private val ActionsMaxWidth = 240.dp
private val MessageMaxWidth = 280.dp

/**
 * Empty screen: a glyph on a marker swipe, a headline, one sentence of guidance and the
 * actions that resolve it (for example [NotesAccentButton] and [NotesOutlinedButton]).
 */
@Composable
fun NotesEmptyState(
    icon: Painter,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actions: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyGlyph(icon = icon)
        Text(
            text = title,
            style = NotesTheme.typography.emptyHeadline,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = NotesSpacing.space5).semantics { heading() },
        )
        Text(
            text = message,
            style = NotesTheme.typography.label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = NotesSpacing.space2).widthIn(max = MessageMaxWidth),
        )
        if (actions != null) {
            Column(
                modifier = Modifier
                    .padding(top = 28.dp)
                    .widthIn(max = ActionsMaxWidth)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = actions,
            )
        }
    }
}

@Composable
private fun EmptyGlyph(icon: Painter) {
    val marker = NotesTheme.colors.accent
    Surface(
        shape = RoundedCornerShape(GlyphRadius),
        color = NotesTheme.colors.surface2,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.size(GlyphSize),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            // .empty .glyph::after: a tilted marker swipe behind the icon.
            modifier = Modifier.drawBehind {
                val inset = 14.dp.toPx()
                val swipeHeight = 10.dp.toPx()
                val top = size.height - 18.dp.toPx() - swipeHeight
                rotate(degrees = -3f) {
                    drawRoundRect(
                        color = marker.copy(alpha = 0.9f),
                        topLeft = Offset(inset, top),
                        size = Size(size.width - 2 * inset, swipeHeight),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                    )
                }
            },
        ) {
            Icon(painter = icon, contentDescription = null, modifier = Modifier.size(GlyphIconSize))
        }
    }
}

@Composable
private fun NoNotesSample() {
    NotesEmptyState(
        icon = NotesIcons.Note,
        title = "No notes yet",
        message = "Write your first note, or import a Markdown file.",
    ) {
        NotesAccentButton(text = "New note", icon = NotesIcons.Plus, onClick = {}, modifier = Modifier.fillMaxWidth())
        NotesOutlinedButton(text = "Import file", icon = NotesIcons.Import, onClick = {}, modifier = Modifier.fillMaxWidth())
    }
}

@Preview(name = "No notes", widthDp = 360)
@Composable
private fun NotesEmptyStatePreview() {
    NotesPreview { NoNotesSample() }
}

@Preview(name = "No notes dark", widthDp = 360)
@Composable
private fun NotesEmptyStateDarkPreview() {
    NotesPreview(darkTheme = true) { NoNotesSample() }
}

@Preview(name = "No notes iOS", widthDp = 393)
@Composable
private fun NotesEmptyStateIosPreview() {
    NotesPreview(platform = NotesPlatform.Ios) { NoNotesSample() }
}

@Preview(name = "Search, nothing typed", widthDp = 360)
@Composable
private fun NotesEmptyStateSearchPreview() {
    NotesPreview {
        NotesEmptyState(icon = NotesIcons.Search, title = "Search your notes", message = "Find any word in titles and text.")
    }
}

@Preview(name = "Search, no results (long)", widthDp = 320)
@Composable
private fun NotesEmptyStateNoResultsPreview() {
    NotesPreview(darkTheme = true) {
        NotesEmptyState(
            icon = NotesIcons.Search,
            title = "No results for “quarterly budget review for the design team”",
            message = "Check the spelling or try fewer words.",
        )
    }
}
