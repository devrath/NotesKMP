package com.istudio.noteskmp.core.designsystem.theme

import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.ui.unit.sp
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test

class NotesThemeTokensTest {

    @Test
    fun `motion spec is a tween of the token duration`() {
        val spec = NotesMotion.spec<Float>(NotesMotion.DURATION_BASE_MILLIS, reduceMotion = false)

        assertThat(spec).isInstanceOf<TweenSpec<Float>>()
        assertThat((spec as TweenSpec<Float>).durationMillis).isEqualTo(200)
        assertThat(spec.easing).isEqualTo(NotesMotion.EasingStandard)
    }

    @Test
    fun `motion spec keeps a custom easing`() {
        val spec = NotesMotion.spec<Float>(
            NotesMotion.DURATION_SLOW_MILLIS,
            reduceMotion = false,
            easing = NotesMotion.EasingEmphasized,
        ) as TweenSpec<Float>

        assertThat(spec.easing).isEqualTo(NotesMotion.EasingEmphasized)
    }

    @Test
    fun `reduce motion makes every duration instant`() {
        listOf(
            NotesMotion.DURATION_FAST_MILLIS,
            NotesMotion.DURATION_BASE_MILLIS,
            NotesMotion.DURATION_SLOW_MILLIS,
        ).forEach { duration ->
            val spec = NotesMotion.spec<Float>(duration, reduceMotion = true)
            assertThat(spec).isInstanceOf<SnapSpec<Float>>()
            assertThat((spec as SnapSpec<Float>).delay).isEqualTo(0)
        }
    }

    @Test
    fun `each platform gets its own type scale`() {
        assertThat(notesTypography(NotesPlatform.Android).body.fontSize).isEqualTo(16.sp)
        assertThat(notesTypography(NotesPlatform.Ios).body.fontSize).isEqualTo(17.sp)
        assertThat(notesTypography(NotesPlatform.Android).appTitle.fontSize).isEqualTo(24.sp)
        assertThat(notesTypography(NotesPlatform.Ios).appTitle.fontSize).isEqualTo(34.sp)
    }
}
