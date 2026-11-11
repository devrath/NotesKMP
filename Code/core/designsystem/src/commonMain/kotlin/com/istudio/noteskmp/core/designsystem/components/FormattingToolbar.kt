package com.istudio.noteskmp.core.designsystem.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.istudio.noteskmp.core.designsystem.generated.resources.Res
import com.istudio.noteskmp.core.designsystem.generated.resources.format_bold
import com.istudio.noteskmp.core.designsystem.generated.resources.format_bulleted_list
import com.istudio.noteskmp.core.designsystem.generated.resources.format_checklist
import com.istudio.noteskmp.core.designsystem.generated.resources.format_code_block
import com.istudio.noteskmp.core.designsystem.generated.resources.format_heading
import com.istudio.noteskmp.core.designsystem.generated.resources.format_inline_code
import com.istudio.noteskmp.core.designsystem.generated.resources.format_italic
import com.istudio.noteskmp.core.designsystem.generated.resources.format_link
import com.istudio.noteskmp.core.designsystem.generated.resources.format_numbered_list
import com.istudio.noteskmp.core.designsystem.generated.resources.format_quote
import com.istudio.noteskmp.core.designsystem.generated.resources.formatting_toolbar
import com.istudio.noteskmp.core.designsystem.icon.NotesIcons
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesSpacing
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** The formatting toolbar's buttons, in the order the spec lists them. */
enum class FormattingAction {
    Heading,
    Bold,
    Italic,
    BulletedList,
    NumberedList,
    Checklist,
    Quote,
    InlineCode,
    CodeBlock,
    Link,
}

private val AndroidToolbarHeight = 48.dp
private val IosToolbarHeight = 44.dp
private val ToolbarButtonRadius = 10.dp
private val SeparatorHeight = 22.dp

/** Separators fall after these actions: text styles | blocks | code and links. */
private val GroupEnds = setOf(FormattingAction.Italic, FormattingAction.Quote)

/**
 * Toolbar that rides on the keyboard in edit mode: 48 dp on Android, a 44 pt keyboard
 * accessory on iOS. It scrolls sideways on narrow screens; no button hides in a menu.
 */
@Composable
fun FormattingToolbar(
    onActionClick: (FormattingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isIos = NotesTheme.platform == NotesPlatform.Ios
    val label = stringResource(Res.string.formatting_toolbar)
    Surface(
        color = if (isIos) NotesTheme.colors.surface2 else NotesTheme.colors.surface,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = label
                isTraversalGroup = true
            },
    ) {
        Column {
            if (!isIos) HorizontalDivider(color = NotesTheme.colors.outline)
            Row(
                modifier = Modifier
                    .heightIn(min = if (isIos) IosToolbarHeight else AndroidToolbarHeight)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = NotesSpacing.space1),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FormattingAction.entries.forEach { action ->
                    NotesIconButton(
                        icon = action.icon(),
                        contentDescription = stringResource(action.label()),
                        onClick = { onActionClick(action) },
                        shape = RoundedCornerShape(ToolbarButtonRadius),
                    )
                    if (action in GroupEnds) {
                        VerticalDivider(
                            color = NotesTheme.colors.outline,
                            modifier = Modifier.padding(horizontal = NotesSpacing.space1).height(SeparatorHeight),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormattingAction.icon(): Painter = when (this) {
    FormattingAction.Heading -> NotesIcons.Heading
    FormattingAction.Bold -> NotesIcons.Bold
    FormattingAction.Italic -> NotesIcons.Italic
    FormattingAction.BulletedList -> NotesIcons.ListBullet
    FormattingAction.NumberedList -> NotesIcons.ListNumbered
    FormattingAction.Checklist -> NotesIcons.Checklist
    FormattingAction.Quote -> NotesIcons.Quote
    FormattingAction.InlineCode -> NotesIcons.Code
    FormattingAction.CodeBlock -> NotesIcons.CodeBlock
    FormattingAction.Link -> NotesIcons.Link
}

private fun FormattingAction.label(): StringResource = when (this) {
    FormattingAction.Heading -> Res.string.format_heading
    FormattingAction.Bold -> Res.string.format_bold
    FormattingAction.Italic -> Res.string.format_italic
    FormattingAction.BulletedList -> Res.string.format_bulleted_list
    FormattingAction.NumberedList -> Res.string.format_numbered_list
    FormattingAction.Checklist -> Res.string.format_checklist
    FormattingAction.Quote -> Res.string.format_quote
    FormattingAction.InlineCode -> Res.string.format_inline_code
    FormattingAction.CodeBlock -> Res.string.format_code_block
    FormattingAction.Link -> Res.string.format_link
}

@Preview(name = "Android, scrolls", widthDp = 360)
@Composable
private fun FormattingToolbarPreview() {
    NotesPreview { FormattingToolbar(onActionClick = {}) }
}

@Preview(name = "Android, wide", widthDp = 840)
@Composable
private fun FormattingToolbarWidePreview() {
    NotesPreview { FormattingToolbar(onActionClick = {}) }
}

@Preview(name = "Android dark", widthDp = 360)
@Composable
private fun FormattingToolbarDarkPreview() {
    NotesPreview(darkTheme = true) { FormattingToolbar(onActionClick = {}) }
}

@Preview(name = "iOS", widthDp = 393)
@Composable
private fun FormattingToolbarIosPreview() {
    NotesPreview(platform = NotesPlatform.Ios) { FormattingToolbar(onActionClick = {}) }
}

@Preview(name = "iOS dark", widthDp = 393)
@Composable
private fun FormattingToolbarIosDarkPreview() {
    NotesPreview(darkTheme = true, platform = NotesPlatform.Ios) { FormattingToolbar(onActionClick = {}) }
}
