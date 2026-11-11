package com.istudio.noteskmp.core.designsystem.icon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme
import androidx.compose.ui.graphics.painter.Painter
import com.istudio.noteskmp.core.designsystem.generated.resources.Res
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_back
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_bold
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_check
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_checklist
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_chevron_left
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_chevron_right
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_clear_filled
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_close
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_code
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_code_block
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_compose
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_copy
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_edit
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_export
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_folder
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_heading
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_import
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_italic
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_link
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_list_bullet
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_list_numbered
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_more_circle
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_more_vert
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_note
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_open_external
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_pin
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_pin_off
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_plus
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_preview
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_quote
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_search
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_trash
import com.istudio.noteskmp.core.designsystem.generated.resources.ic_warning
import org.jetbrains.compose.resources.painterResource

/**
 * The Notes icon set from `design/assets/icons.js`: 24 grid, 1.75 stroke, round caps and joins.
 * Draw at 24 dp on Android and 22 pt on iOS ([NotesIconSize]). Prefer these over Material icons.
 */
object NotesIcons {
    val Search: Painter @Composable get() = painterResource(Res.drawable.ic_search)
    val Import: Painter @Composable get() = painterResource(Res.drawable.ic_import)

    /** New note on Android. */
    val Plus: Painter @Composable get() = painterResource(Res.drawable.ic_plus)

    /** New note on iOS. */
    val Compose: Painter @Composable get() = painterResource(Res.drawable.ic_compose)

    /** Back on Android. */
    val Back: Painter @Composable get() = painterResource(Res.drawable.ic_back)

    /** Back on iOS. */
    val ChevronLeft: Painter @Composable get() = painterResource(Res.drawable.ic_chevron_left)
    val ChevronRight: Painter @Composable get() = painterResource(Res.drawable.ic_chevron_right)
    val Preview: Painter @Composable get() = painterResource(Res.drawable.ic_preview)
    val Edit: Painter @Composable get() = painterResource(Res.drawable.ic_edit)

    /** More on Android. */
    val MoreVert: Painter @Composable get() = painterResource(Res.drawable.ic_more_vert)

    /** More on iOS. */
    val MoreCircle: Painter @Composable get() = painterResource(Res.drawable.ic_more_circle)

    /** Pin action and the pinned indicator. */
    val Pin: Painter @Composable get() = painterResource(Res.drawable.ic_pin)
    val PinOff: Painter @Composable get() = painterResource(Res.drawable.ic_pin_off)
    val Trash: Painter @Composable get() = painterResource(Res.drawable.ic_trash)
    val Export: Painter @Composable get() = painterResource(Res.drawable.ic_export)

    /** Clear search on Android. */
    val Close: Painter @Composable get() = painterResource(Res.drawable.ic_close)

    /** Clear search on iOS. */
    val ClearFilled: Painter @Composable get() = painterResource(Res.drawable.ic_clear_filled)
    val Heading: Painter @Composable get() = painterResource(Res.drawable.ic_heading)
    val Bold: Painter @Composable get() = painterResource(Res.drawable.ic_bold)
    val Italic: Painter @Composable get() = painterResource(Res.drawable.ic_italic)
    val ListBullet: Painter @Composable get() = painterResource(Res.drawable.ic_list_bullet)
    val ListNumbered: Painter @Composable get() = painterResource(Res.drawable.ic_list_numbered)
    val Checklist: Painter @Composable get() = painterResource(Res.drawable.ic_checklist)
    val Quote: Painter @Composable get() = painterResource(Res.drawable.ic_quote)
    val Code: Painter @Composable get() = painterResource(Res.drawable.ic_code)
    val CodeBlock: Painter @Composable get() = painterResource(Res.drawable.ic_code_block)
    val Link: Painter @Composable get() = painterResource(Res.drawable.ic_link)
    val Check: Painter @Composable get() = painterResource(Res.drawable.ic_check)
    val Copy: Painter @Composable get() = painterResource(Res.drawable.ic_copy)
    val OpenExternal: Painter @Composable get() = painterResource(Res.drawable.ic_open_external)

    /** Empty notes glyph. */
    val Note: Painter @Composable get() = painterResource(Res.drawable.ic_note)

    /** Save to Files. */
    val Folder: Painter @Composable get() = painterResource(Res.drawable.ic_folder)
    val Warning: Painter @Composable get() = painterResource(Res.drawable.ic_warning)
}

/** Default icon size: 24 dp on Android, 22 pt on iOS. */
val NotesIconSize: Dp
    @Composable
    @ReadOnlyComposable
    get() = if (NotesTheme.platform == NotesPlatform.Ios) 22.dp else 24.dp
