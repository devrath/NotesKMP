package com.istudio.noteskmp.core.designsystem.theme

import androidx.compose.runtime.Composable
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

internal actual val currentNotesPlatform: NotesPlatform = NotesPlatform.Ios

@Composable
internal actual fun isSystemReduceMotionEnabled(): Boolean = UIAccessibilityIsReduceMotionEnabled()
