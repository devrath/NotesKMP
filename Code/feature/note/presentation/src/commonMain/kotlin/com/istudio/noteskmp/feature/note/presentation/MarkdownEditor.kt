package com.istudio.noteskmp.feature.note.presentation

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.istudio.noteskmp.core.designsystem.theme.NotesColors
import com.istudio.noteskmp.core.designsystem.theme.NotesSpacing
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme
import com.istudio.noteskmp.feature.note.domain.SyntaxKind
import com.istudio.noteskmp.feature.note.domain.SyntaxSpan
import com.istudio.noteskmp.feature.note.domain.markdownSyntaxSpans
import com.istudio.noteskmp.feature.note.presentation.generated.resources.Res
import com.istudio.noteskmp.feature.note.presentation.generated.resources.body_placeholder
import com.istudio.noteskmp.feature.note.presentation.generated.resources.title_placeholder
import org.jetbrains.compose.resources.stringResource

/** The raw Markdown editor: a single-line title above the body. */
@Composable
internal fun MarkdownEditor(
    title: String,
    body: TextFieldValue,
    onTitleChange: (String) -> Unit,
    onBodyChange: (TextFieldValue) -> Unit,
    scrollState: ScrollState,
    bodyFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val colors = NotesTheme.colors
    val typography = NotesTheme.typography
    val textColor = MaterialTheme.colorScheme.onBackground
    // Highlighting never changes the text; it is a style layer over it.
    val spans = remember(body.text) { markdownSyntaxSpans(body.text) }
    val highlighting = remember(spans, colors) { MarkdownHighlighting(spans, SyntaxStyles(colors)) }

    Column(modifier = modifier.fillMaxSize().verticalScroll(scrollState)) {
        PlaceholderBox(isEmpty = title.isEmpty(), placeholder = stringResource(Res.string.title_placeholder), isTitle = true) {
            BasicTextField(
                value = title,
                onValueChange = onTitleChange,
                singleLine = true,
                textStyle = typography.noteTitle.copy(color = textColor),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                // Enter in the title moves on to the body.
                keyboardActions = KeyboardActions(onNext = { bodyFocusRequester.requestFocus() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        PlaceholderBox(isEmpty = body.text.isEmpty(), placeholder = stringResource(Res.string.body_placeholder), isTitle = false) {
            BasicTextField(
                value = body,
                onValueChange = onBodyChange,
                textStyle = typography.body.copy(color = textColor),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = highlighting,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth().focusRequester(bodyFocusRequester),
            )
        }
    }
}

@Composable
private fun PlaceholderBox(
    isEmpty: Boolean,
    placeholder: String,
    isTitle: Boolean,
    field: @Composable () -> Unit,
) {
    val typography = NotesTheme.typography
    Box(
        modifier = Modifier.padding(
            start = NotesSpacing.space5,
            end = NotesSpacing.space5,
            top = if (isTitle) NotesSpacing.space1 else 6.dp,
            bottom = if (isTitle) 6.dp else NotesSpacing.space5,
        ),
    ) {
        if (isEmpty) {
            Text(
                text = placeholder,
                style = if (isTitle) typography.noteTitle.copy(fontWeight = FontWeight.SemiBold) else typography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        field()
    }
}

/** Styles for each kind of syntax, from the theme. */
@Immutable
internal class SyntaxStyles(colors: NotesColors) {
    val marker = SpanStyle(color = colors.markdownMarker, fontWeight = FontWeight.Normal)
    val heading1 = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 1.2.em)
    val heading2 = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 1.12.em)
    val heading3 = SpanStyle(fontWeight = FontWeight.Bold)
    val bold = SpanStyle(fontWeight = FontWeight.Bold)
    val italic = SpanStyle(fontStyle = FontStyle.Italic)
    val strike = SpanStyle(textDecoration = TextDecoration.LineThrough)
    val code = SpanStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 0.88.em,
        background = colors.codeBackground,
        color = colors.codeForeground,
    )
    val linkText = SpanStyle(color = colors.primary)
    val linkUrl = SpanStyle(color = colors.markdownMarker)

    fun of(kind: SyntaxKind): SpanStyle = when (kind) {
        SyntaxKind.Marker -> marker
        SyntaxKind.Heading1 -> heading1
        SyntaxKind.Heading2 -> heading2
        SyntaxKind.Heading3 -> heading3
        SyntaxKind.Bold -> bold
        SyntaxKind.Italic -> italic
        SyntaxKind.Strikethrough -> strike
        SyntaxKind.Code -> code
        SyntaxKind.LinkText -> linkText
        SyntaxKind.LinkUrl -> linkUrl
    }
}

/** Applies precomputed [spans] as styles. Offsets map one to one, since no text changes. */
internal class MarkdownHighlighting(
    private val spans: List<SyntaxSpan>,
    private val styles: SyntaxStyles,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        if (spans.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val styled = AnnotatedString.Builder(text).apply {
            for (span in spans) {
                // Spans can trail the text by a keystroke; skip any that no longer fit.
                if (span.end <= text.length && span.start < span.end) addStyle(styles.of(span.kind), span.start, span.end)
            }
        }.toAnnotatedString()
        return TransformedText(styled, OffsetMapping.Identity)
    }

    override fun equals(other: Any?): Boolean =
        other is MarkdownHighlighting && other.spans == spans && other.styles === styles
    override fun hashCode(): Int = 31 * spans.hashCode() + styles.hashCode()
}
