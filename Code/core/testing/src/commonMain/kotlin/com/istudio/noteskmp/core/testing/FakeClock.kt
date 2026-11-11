package com.istudio.noteskmp.core.testing

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/** A clock that only moves when a test moves it. */
class FakeClock(var now: Instant = Instant.parse("2026-03-10T12:00:00Z")) : Clock {
    override fun now(): Instant = now

    fun advance(by: Duration) {
        now += by
    }
}
