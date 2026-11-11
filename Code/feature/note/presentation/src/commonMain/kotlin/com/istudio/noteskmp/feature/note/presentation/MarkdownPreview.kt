package com.istudio.noteskmp.feature.note.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.istudio.noteskmp.core.designsystem.components.ChecklistItem
import com.istudio.noteskmp.core.designsystem.components.NotesDropdownMenu
import com.istudio.noteskmp.core.designsystem.components.NotesMenuHeader
import com.istudio.noteskmp.core.designsystem.components.NotesMenuItem
import com.istudio.noteskmp.core.designsystem.icon.NotesIcons
import com.istudio.noteskmp.core.designsystem.theme.NotesColors
import com.istudio.noteskmp.core.designsystem.theme.NotesRadius
import com.istudio.noteskmp.core.designsystem.theme.NotesSpacing
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme
import com.istudio.noteskmp.feature.note.domain.ListMarker
import com.istudio.noteskmp.feature.note.domain.MarkdownBlock
import com.istudio.noteskmp.feature.note.domain.MarkdownInline
import com.istudio.noteskmp.feature.note.domain.isSafeLinkUrl
import com.istudio.noteskmp.feature.note.presentation.generated.resources.Res
import com.istudio.noteskmp.feature.note.presentation.generated.resources.link_copy
import com.istudio.noteskmp.feature.note.presentation.generated.resources.link_open
import org.jetbrains.compose.resources.stringResource

/** About 70 characters per line on wide screens. */
private val ReadingMaxWidth = 680.dp
private val QuoteRuleWidth = 3.dp
private val ListIndent = 22.dp

/**
 * The formatted note. Checklist items tick on tap, links open on tap (only `http`,
 * `https` and `mailto`), long-pressing a link offers Copy link and Open, and double-tapping
 * anywhere switches to editing at that spot.
 */
@Composable
internal fun MarkdownPreview(
    title: String,
    blocks: List<MarkdownBlock>,
    listState: LazyListState,
    linkMenu: LinkMenuUi?,
    onChecklistToggle: (Int) -> Unit,
    onDoubleTap: (Int) -> Unit,
    onLinkLongPress: (Int, String) -> Unit,
    onLinkMenuDismiss: () -> Unit,
    onCopyLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NotesTheme.colors
    val uriHandler = LocalUriHandler.current
    val openLink: (String) -> Unit = { url -> if (isSafeLinkUrl(url)) uriHandler.openUri(url) }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = NotesSpacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (title.isNotBlank()) {
            item(key = "title") {
                Text(
                    text = title,
                    style = NotesTheme.typography.noteTitle,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .readingWidth()
                        .padding(start = NotesSpacing.space5, end = NotesSpacing.space5, top = NotesSpacing.space1, bottom = 6.dp)
                        .semantics { heading() },
                )
            }
        }
        itemsIndexed(blocks, contentType = { _, block -> block::class }) { index, block ->
            Box(
                modifier = Modifier
                    .readingWidth()
                    .pointerInput(block.sourceStart) {
                        detectTapGestures(onDoubleTap = { onDoubleTap(block.sourceStart) })
                    }
                    .padding(horizontal = NotesSpacing.space5),
            ) {
                QuoteFrame(depth = block.quoteDepth) {
                    PreviewBlock(
                        block = block,
                        colors = colors,
                        isQuoted = block.quoteDepth > 0,
                        onChecklistToggle = onChecklistToggle,
                        onLinkClick = openLink,
                        onLinkLongPress = { onLinkLongPress(index, it) },
                    )
                }
                NotesDropdownMenu(expanded = linkMenu?.blockIndex == index, onDismissRequest = onLinkMenuDismiss) {
                    val url = linkMenu?.url.orEmpty()
                    NotesMenuHeader(text = url)
                    NotesMenuItem(
                        text = stringResource(Res.string.link_copy),
                        icon = NotesIcons.Copy,
                        onClick = { onCopyLink(url); onLinkMenuDismiss() },
                    )
                    NotesMenuItem(
                        text = stringResource(Res.string.link_open),
                        icon = NotesIcons.OpenExternal,
                        onClick = { openLink(url); onLinkMenuDismiss() },
                    )
                }
            }
        }
    }
}

private fun Modifier.readingWidth(): Modifier = widthIn(max = ReadingMaxWidth).fillMaxWidth()

@Composable
private fun QuoteFrame(depth: Int, content: @Composable () -> Unit) {
    if (depth == 0) {
        content()
        return
    }
    Row(modifier = Modifier.height(IntrinsicSize.Min).padding(vertical = 2.dp)) {
        repeat(depth) {
            Box(
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .width(QuoteRuleWidth)
                    .fillMaxHeight()
                    .background(NotesTheme.colors.outlineStrong),
            )
            Spacer(modifier = Modifier.width(14.dp))
        }
        Box(modifier = Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun PreviewBlock(
    block: MarkdownBlock,
    colors: NotesColors,
    isQuoted: Boolean,
    onChecklistToggle: (Int) -> Unit,
    onLinkClick: (String) -> Unit,
    onLinkLongPress: (String) -> Unit,
) {
    val body = NotesTheme.typography.body.copy(
        lineHeight = 1.5.em,
        color = if (isQuoted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
    )
    when (block) {
        is MarkdownBlock.Heading -> {
            val (size, top) = when (block.level) {
                1 -> 1.5.em to NotesSpacing.space5
                2 -> 1.25.em to NotesSpacing.space5
                3 -> 1.06.em to NotesSpacing.space4
                else -> 1.em to NotesSpacing.space3
            }
            InlineText(
                content = block.content,
                style = body.copy(fontSize = body.fontSize * size.value, fontWeight = FontWeight.Bold, lineHeight = 1.3.em),
                colors = colors,
                onLinkClick = onLinkClick,
                onLinkLongPress = onLinkLongPress,
                modifier = Modifier.padding(top = top, bottom = 6.dp).semantics { heading() },
            )
        }

        is MarkdownBlock.Paragraph -> InlineText(
            content = block.content,
            style = body,
            colors = colors,
            onLinkClick = onLinkClick,
            onLinkLongPress = onLinkLongPress,
            modifier = Modifier.padding(bottom = NotesSpacing.space3),
        )

        is MarkdownBlock.ListItem -> ListItemBlock(block, body, colors, onChecklistToggle, onLinkClick, onLinkLongPress)

        is MarkdownBlock.CodeBlock -> Surface(
            color = colors.codeBackground,
            contentColor = colors.codeForeground,
            shape = RoundedCornerShape(NotesRadius.sm),
            modifier = Modifier.fillMaxWidth().padding(bottom = NotesSpacing.space3),
        ) {
            // Code scrolls sideways instead of wrapping.
            Text(
                text = block.code,
                style = NotesTheme.typography.code,
                softWrap = false,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = NotesSpacing.space3),
            )
        }

        is MarkdownBlock.ThematicBreak -> HorizontalDivider(
            color = colors.outline,
            modifier = Modifier.padding(vertical = NotesSpacing.space4),
        )
    }
}

@Composable
private fun ListItemBlock(
    block: MarkdownBlock.ListItem,
    style: TextStyle,
    colors: NotesColors,
    onChecklistToggle: (Int) -> Unit,
    onLinkClick: (String) -> Unit,
    onLinkLongPress: (String) -> Unit,
) {
    val indent = Modifier.padding(start = ListIndent * block.depth)
    when (val marker = block.marker) {
        is ListMarker.Task -> ChecklistItem(
            text = block.content.toAnnotatedString(colors, onLinkClick),
            isChecked = marker.isChecked,
            onCheckedChange = { onChecklistToggle(marker.checkboxOffset) },
            modifier = indent,
        )

        else -> Row(
            modifier = indent.padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(NotesSpacing.space2),
        ) {
            Text(
                text = if (marker is ListMarker.Number) marker.number else "•",
                style = style,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.widthIn(min = 14.dp),
            )
            InlineText(
                content = block.content,
                style = style,
                colors = colors,
                onLinkClick = onLinkClick,
                onLinkLongPress = onLinkLongPress,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Text with inline styles. Long-pressing a link reports its address. */
@Composable
private fun InlineText(
    content: List<MarkdownInline>,
    style: TextStyle,
    colors: NotesColors,
    onLinkClick: (String) -> Unit,
    onLinkLongPress: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val text = remember(content, colors) { content.toAnnotatedString(colors, onLinkClick) }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    Column(modifier = modifier) {
        Text(
            text = text,
            style = style,
            onTextLayout = { layout = it },
            modifier = Modifier.pointerInput(text) {
                detectTapGestures(
                    onLongPress = { position ->
                        val offset = layout?.getOffsetForPosition(position) ?: return@detectTapGestures
                        text.getLinkAnnotations(offset, offset + 1).firstOrNull()?.let { range ->
                            (range.item as? LinkAnnotation.Clickable)?.tag?.let(onLinkLongPress)
                        }
                    },
                )
            },
        )
    }
}

/** Builds styled text; links are clickable annotations so screen readers expose them. */
internal fun List<MarkdownInline>.toAnnotatedString(
    colors: NotesColors,
    onLinkClick: (String) -> Unit,
): AnnotatedString = buildAnnotatedString {
    val linkStyles = TextLinkStyles(
        style = SpanStyle(color = colors.primary, textDecoration = TextDecoration.Underline),
        pressedStyle = SpanStyle(color = colors.primary, background = colors.primarySoft, textDecoration = TextDecoration.Underline),
    )

    fun append(items: List<MarkdownInline>) {
        for (item in items) when (item) {
            is MarkdownInline.Text -> append(item.text)
            is MarkdownInline.Bold -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(item.children) }
            is MarkdownInline.Italic -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(item.children) }
            is MarkdownInline.Strikethrough -> withStyle(
                SpanStyle(textDecoration = TextDecoration.LineThrough, color = colors.onMuted),
            ) { append(item.children) }
            is MarkdownInline.Code -> withStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 0.86.em,
                    background = colors.codeBackground,
                    color = colors.codeForeground,
                ),
            ) { append(item.code) }
            is MarkdownInline.Link -> withLink(
                LinkAnnotation.Clickable(tag = item.url, styles = linkStyles) { onLinkClick(item.url) },
            ) { append(item.children) }
            MarkdownInline.LineBreak -> append('\n')
        }
    }
    append(this@toAnnotatedString)
}
