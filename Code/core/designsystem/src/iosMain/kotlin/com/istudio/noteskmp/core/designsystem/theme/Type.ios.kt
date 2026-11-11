package com.istudio.noteskmp.core.designsystem.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// iOS scale (pt, Dynamic Type styles) from design/design-system.html.
internal actual fun platformNotesTypography(): NotesTypography = NotesTypography(
    appTitle = TextStyle(fontSize = 34.sp, lineHeight = 41.sp, fontWeight = FontWeight.ExtraBold),
    noteTitle = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    // The iOS specimen defines no empty headline; this is the Title 2 Dynamic Type style.
    emptyHeadline = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    listTitle = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    body = TextStyle(fontSize = 17.sp, lineHeight = 25.5.sp, fontWeight = FontWeight.Normal),
    label = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    meta = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
    code = TextStyle(fontSize = 15.sp, lineHeight = 24.sp, fontFamily = FontFamily.Monospace),
)
