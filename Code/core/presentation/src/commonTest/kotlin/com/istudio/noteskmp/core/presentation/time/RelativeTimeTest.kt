package com.istudio.noteskmp.core.presentation.time

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class RelativeTimeTest {

    private val utc = TimeZone.UTC
    private val now = Instant.parse("2026-03-10T12:00:00Z")

    private fun ago(edited: Instant) = relativeTime(edited, now, utc)

    @Test
    fun `under a minute is just now - including the same instant`() {
        assertThat(ago(now)).isEqualTo(RelativeTime.JustNow)
        assertThat(ago(now - 59.seconds)).isEqualTo(RelativeTime.JustNow)
    }

    @Test
    fun `a clock that moved backwards reads just now`() {
        assertThat(ago(now + 5.minutes)).isEqualTo(RelativeTime.JustNow)
    }

    @Test
    fun `minutes then hours on the same day`() {
        assertThat(ago(now - 1.minutes)).isEqualTo(RelativeTime.MinutesAgo(1))
        assertThat(ago(now - 59.minutes)).isEqualTo(RelativeTime.MinutesAgo(59))
        assertThat(ago(now - 60.minutes)).isEqualTo(RelativeTime.HoursAgo(1))
        assertThat(ago(now - 12.hours)).isEqualTo(RelativeTime.HoursAgo(12))
    }

    @Test
    fun `the previous calendar day is yesterday even if under 24 hours`() {
        val justAfterMidnight = Instant.parse("2026-03-10T00:30:00Z")
        assertThat(relativeTime(Instant.parse("2026-03-09T23:00:00Z"), justAfterMidnight, utc)).isEqualTo(RelativeTime.Yesterday)
        assertThat(ago(now - 13.hours)).isEqualTo(RelativeTime.Yesterday)
    }

    @Test
    fun `older dates in this year omit the year - earlier years include it`() {
        assertThat(ago(now - 6.days)).isEqualTo(RelativeTime.OnDate(LocalDate(2026, 3, 4), includeYear = false))
        assertThat(ago(Instant.parse("2025-12-12T10:00:00Z"))).isEqualTo(RelativeTime.OnDate(LocalDate(2025, 12, 12), includeYear = true))
    }

    @Test
    fun `days follow the device time zone`() {
        val tokyo = TimeZone.of("Asia/Tokyo")
        // 01:00 on the 11th in Tokyo. An edit two hours earlier was 23:00 on the 10th there:
        // the same day in UTC, but yesterday on this device.
        val now = Instant.parse("2026-03-10T16:00:00Z")
        val edited = Instant.parse("2026-03-10T14:00:00Z")
        assertThat(relativeTime(edited, now, TimeZone.UTC)).isEqualTo(RelativeTime.HoursAgo(2))
        assertThat(relativeTime(edited, now, tokyo)).isEqualTo(RelativeTime.Yesterday)
    }
}
