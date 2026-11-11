package com.istudio.noteskmp.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Semantic type roles from the design system. Each platform uses its system face and its
 * own scale (Roboto in sp on Android, SF Pro Dynamic Type sizes on iOS), so the values
 * come from [notesTypography].
 */
@Immutable
data class NotesTypography(
    val appTitle: TextStyle,
    val noteTitle: TextStyle,
    val emptyHeadline: TextStyle,
    val listTitle: TextStyle,
    /** Body, editor and preview text. */
    val body: TextStyle,
    /** Note previews and labels. */
    val label: TextStyle,
    /** Timestamps and other metadata. */
    val meta: TextStyle,
    val code: TextStyle,
    /** Filled, outlined and FAB labels. */
    val button: TextStyle,
    /** Delete confirmation title. */
    val dialogTitle: TextStyle,
)

// Android scale (sp) from design/design-system.html.
private val AndroidNotesTypography = NotesTypography(
    appTitle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.03).em),
    noteTitle = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.015).em),
    emptyHeadline = TextStyle(fontSize = 22.sp, lineHeight = 29.sp, fontWeight = FontWeight.SemiBold),
    listTitle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    body = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
    label = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    meta = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal),
    code = TextStyle(fontSize = 14.sp, lineHeight = 22.sp, fontFamily = FontFamily.Monospace),
    // .btn and .a-fab in design/assets/app.css; Material 3 basic dialog headline.
    button = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    dialogTitle = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Normal),
)

// iOS scale (pt, Dynamic Type styles) from design/design-system.html.
private val IosNotesTypography = NotesTypography(
    appTitle = TextStyle(fontSize = 34.sp, lineHeight = 41.sp, fontWeight = FontWeight.ExtraBold),
    noteTitle = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    // The iOS specimen defines no empty headline; this is the Title 2 Dynamic Type style.
    emptyHeadline = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    listTitle = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    body = TextStyle(fontSize = 17.sp, lineHeight = 25.5.sp, fontWeight = FontWeight.Normal),
    label = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    meta = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
    code = TextStyle(fontSize = 15.sp, lineHeight = 24.sp, fontFamily = FontFamily.Monospace),
    // .ios .btn and .i-alert h3 in design/assets/app.css.
    button = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    dialogTitle = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
)

internal fun notesTypography(platform: NotesPlatform): NotesTypography = when (platform) {
    NotesPlatform.Android -> AndroidNotesTypography
    NotesPlatform.Ios -> IosNotesTypography
}

internal fun NotesTypography.toMaterialTypography(): Typography = Typography(
    headlineLarge = appTitle,
    headlineMedium = noteTitle,
    headlineSmall = dialogTitle,
    titleLarge = noteTitle,
    titleMedium = listTitle,
    titleSmall = label,
    bodyLarge = body,
    bodyMedium = label,
    bodySmall = meta,
    labelLarge = label,
    labelMedium = meta,
    labelSmall = meta,
)
