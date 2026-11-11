package com.istudio.noteskmp.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.istudio.noteskmp.core.designsystem.generated.resources.Res
import com.istudio.noteskmp.core.designsystem.generated.resources.cd_pinned
import com.istudio.noteskmp.core.designsystem.icon.NotesIcons
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesSpacing
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme
import org.jetbrains.compose.resources.stringResource

private val ResultPinSize = 14.dp

/**
 * One search result: title and a two-line snippet with matches highlighted (build them with
 * [highlightMatches]), then the edit time and a pin icon. Screen readers read the text plainly;
 * the highlight is visual only.
 */
@Composable
fun SearchResultItem(
    title: AnnotatedString,
    snippet: AnnotatedString,
    editedTime: String,
    isPinned: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isIos = NotesTheme.platform == NotesPlatform.Ios
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {},
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = NotesSpacing.space5,
                vertical = if (isIos) NotesSpacing.space3 else 14.dp,
            ),
        ) {
            Text(
                text = title,
                style = NotesTheme.typography.listTitle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = snippet,
                style = NotesTheme.typography.label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
            Row(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = editedTime,
                    style = NotesTheme.typography.meta,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isPinned) {
                    Icon(
                        painter = NotesIcons.Pin,
                        contentDescription = stringResource(Res.string.cd_pinned),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(ResultPinSize),
                    )
                }
            }
        }
        // iOS insets the separator to the text; Android runs it edge to edge.
        HorizontalDivider(
            color = NotesTheme.colors.outline,
            modifier = if (isIos) Modifier.padding(start = NotesSpacing.space5) else Modifier,
        )
    }
}

@Composable
private fun ResultsSample() {
    val style = searchHighlightStyle
    Column {
        SearchResultItem(
            title = highlightMatches("Weekly plan", listOf(7..10), style),
            snippet = AnnotatedString("This week: focus on search and import errors. Ship the notes list, review import error copy."),
            editedTime = "Yesterday",
            isPinned = true,
            onClick = {},
        )
        SearchResultItem(
            title = AnnotatedString("Meeting notes: Q4 roadmap"),
            snippet = highlightMatches(
                "…We agreed the plan keeps export per note. Search has to stay under 100 ms…",
                listOf(15..18),
                style,
            ),
            editedTime = "5 min ago",
            isPinned = false,
            onClick = {},
        )
    }
}

@Preview(name = "Android", widthDp = 360)
@Composable
private fun SearchResultItemPreview() {
    NotesPreview { ResultsSample() }
}

@Preview(name = "Android dark", widthDp = 360)
@Composable
private fun SearchResultItemDarkPreview() {
    NotesPreview(darkTheme = true) { ResultsSample() }
}

@Preview(name = "iOS", widthDp = 393)
@Composable
private fun SearchResultItemIosPreview() {
    NotesPreview(platform = NotesPlatform.Ios) { ResultsSample() }
}

@Preview(name = "iOS dark", widthDp = 393)
@Composable
private fun SearchResultItemIosDarkPreview() {
    NotesPreview(darkTheme = true, platform = NotesPlatform.Ios) { ResultsSample() }
}
