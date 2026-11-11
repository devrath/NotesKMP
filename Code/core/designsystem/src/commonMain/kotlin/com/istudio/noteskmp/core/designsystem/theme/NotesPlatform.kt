package com.istudio.noteskmp.core.designsystem.theme

import androidx.compose.runtime.Composable

/**
 * Which platform's conventions a component follows where the spec asks for native behavior
 * (create button, top bars, menus, alerts). Everything else shares one look.
 */
enum class NotesPlatform { Android, Ios }

internal expect val currentNotesPlatform: NotesPlatform

/** Whether the system asks for reduced motion. Animations then become instant. */
@Composable
internal expect fun isSystemReduceMotionEnabled(): Boolean
