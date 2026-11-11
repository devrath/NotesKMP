package com.istudio.noteskmp.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** 4-pt spacing grid (`--sp-*`). */
object NotesSpacing {
    val space1 = 4.dp
    val space2 = 8.dp
    val space3 = 12.dp
    val space4 = 16.dp
    val space5 = 20.dp
    val space6 = 24.dp
    val space8 = 32.dp
    val space10 = 40.dp
}

/** Corner radii (`--r-*`). Radius grows with the size of the surface. */
object NotesRadius {
    /** Inline code, highlight. */
    val xs = 4.dp
    /** Code blocks, menus (iOS). */
    val sm = 8.dp
    /** List cards, snackbars. */
    val md = 12.dp
    /** FAB, sheets. */
    val lg = 16.dp
    /** Dialogs (Android), search bar. */
    val xl = 28.dp
}

internal val NotesShapes = Shapes(
    extraSmall = RoundedCornerShape(NotesRadius.xs),
    small = RoundedCornerShape(NotesRadius.sm),
    medium = RoundedCornerShape(NotesRadius.md),
    large = RoundedCornerShape(NotesRadius.lg),
    extraLarge = RoundedCornerShape(NotesRadius.xl),
)

/** Motion tokens (`--dur-*`, `--ease-*`). Callers must still honor reduce-motion. */
object NotesMotion {
    const val DURATION_FAST_MILLIS = 120
    const val DURATION_BASE_MILLIS = 200
    const val DURATION_SLOW_MILLIS = 300
    val EasingStandard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EasingEmphasized = CubicBezierEasing(0.3f, 0f, 0f, 1f)
}
