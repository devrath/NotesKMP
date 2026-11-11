package com.istudio.noteskmp.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme
import kotlin.math.PI
import kotlin.math.tan

/**
 * Brand mark: ink letters on a marker-yellow swipe. It looks the same in both themes, so it
 * reads accent colors that don't change with the theme.
 */
@Composable
fun NotesWordmark(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = NotesTheme.typography.appTitle,
) {
    val marker = NotesTheme.colors.accent
    val horizontalPadding = with(LocalDensity.current) { style.fontSize.toDp() * 0.2f }
    Text(
        text = text,
        style = style,
        color = NotesTheme.colors.onAccent,
        modifier = modifier
            .drawBehind { drawMarkerSwipe(marker, style.fontSize.toPx()) }
            .padding(horizontal = horizontalPadding),
    )
}

// .wordmark::before in design/assets/app.css: inset 0.06em 0 0.02em, uneven corners,
// rotate(-1.5deg) skewX(-6deg).
private fun DrawScope.drawMarkerSwipe(color: Color, em: Float) {
    val top = 0.06f * em
    val bottom = size.height - 0.02f * em
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                left = 0f,
                top = top,
                right = size.width,
                bottom = bottom,
                topLeftCornerRadius = CornerRadius(0.12f * em),
                topRightCornerRadius = CornerRadius(0.3f * em),
                bottomRightCornerRadius = CornerRadius(0.14f * em),
                bottomLeftCornerRadius = CornerRadius(0.34f * em),
            ),
        )
    }
    val skew = Matrix().apply { this[1, 0] = tan(-6f * PI.toFloat() / 180f) }
    rotate(degrees = -1.5f) {
        withTransform({
            translate(center.x, center.y)
            transform(skew)
            translate(-center.x, -center.y)
        }) {
            drawPath(path, color)
        }
    }
}

@Preview(name = "Android")
@Composable
private fun NotesWordmarkPreview() {
    NotesPreview { NotesWordmark(text = "Notes") }
}

@Preview(name = "Android dark")
@Composable
private fun NotesWordmarkDarkPreview() {
    NotesPreview(darkTheme = true) { NotesWordmark(text = "Notes") }
}

@Preview(name = "iOS large title")
@Composable
private fun NotesWordmarkIosPreview() {
    NotesPreview(platform = NotesPlatform.Ios) {
        Column { NotesWordmark(text = "Notes") }
    }
}

@Preview(name = "iOS large title dark")
@Composable
private fun NotesWordmarkIosDarkPreview() {
    NotesPreview(darkTheme = true, platform = NotesPlatform.Ios) {
        Column { NotesWordmark(text = "Notes") }
    }
}
