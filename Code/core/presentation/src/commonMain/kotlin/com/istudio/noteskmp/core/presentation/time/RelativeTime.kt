package com.istudio.noteskmp.core.presentation.time

import androidx.compose.runtime.Composable
import com.istudio.noteskmp.core.presentation.generated.resources.Res
import com.istudio.noteskmp.core.presentation.generated.resources.relative_hours_ago
import com.istudio.noteskmp.core.presentation.generated.resources.relative_just_now
import com.istudio.noteskmp.core.presentation.generated.resources.relative_minutes_ago
import com.istudio.noteskmp.core.presentation.generated.resources.relative_yesterday
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** How long ago a note was edited, before it's turned into text. */
sealed interface RelativeTime {
    data object JustNow : RelativeTime
    data class MinutesAgo(val minutes: Int) : RelativeTime
    data class HoursAgo(val hours: Int) : RelativeTime
    data object Yesterday : RelativeTime

    /** A short date, with the year only when it isn't the current year. */
    data class OnDate(val date: LocalDate, val includeYear: Boolean) : RelativeTime
}

/**
 * Relative for recent edits ("Just now", "5 min ago", "3 h ago" on the same day,
 * "Yesterday"), then a short date. Times in the future (a clock that moved back) read as
 * "Just now".
 */
fun relativeTime(editedAt: Instant, now: Instant, timeZone: TimeZone): RelativeTime {
    val elapsed = now - editedAt
    if (elapsed < 1.minutes) return RelativeTime.JustNow
    if (elapsed < 1.hours) return RelativeTime.MinutesAgo(elapsed.inWholeMinutes.toInt())
    val today = now.toLocalDateTime(timeZone).date
    val editedDay = editedAt.toLocalDateTime(timeZone).date
    return when (editedDay) {
        today -> RelativeTime.HoursAgo(elapsed.inWholeHours.toInt())
        today.minus(DatePeriod(days = 1)) -> RelativeTime.Yesterday
        else -> RelativeTime.OnDate(editedDay, includeYear = editedDay.year != today.year)
    }
}

@Composable
fun RelativeTime.asString(): String = when (this) {
    RelativeTime.JustNow -> stringResource(Res.string.relative_just_now)
    is RelativeTime.MinutesAgo -> pluralStringResource(Res.plurals.relative_minutes_ago, minutes, minutes)
    is RelativeTime.HoursAgo -> pluralStringResource(Res.plurals.relative_hours_ago, hours, hours)
    RelativeTime.Yesterday -> stringResource(Res.string.relative_yesterday)
    is RelativeTime.OnDate -> formatShortDate(date, includeYear)
}

/** A short, locale-formatted date such as "Mar 4" or "Dec 12, 2025". */
expect fun formatShortDate(date: LocalDate, includeYear: Boolean): String
