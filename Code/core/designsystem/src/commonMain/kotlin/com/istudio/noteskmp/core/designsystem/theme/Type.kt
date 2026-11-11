package com.istudio.noteskmp.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle

/**
 * Semantic type roles from the design system. Each platform uses its system face and its
 * own scale (Roboto in sp on Android, SF Pro Dynamic Type sizes on iOS), so the values
 * come from [platformNotesTypography].
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
)

internal expect fun platformNotesTypography(): NotesTypography

internal fun NotesTypography.toMaterialTypography(): Typography = Typography(
    headlineLarge = appTitle,
    headlineMedium = noteTitle,
    headlineSmall = emptyHeadline,
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
