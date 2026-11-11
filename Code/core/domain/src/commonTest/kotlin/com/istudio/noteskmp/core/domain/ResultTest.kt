package com.istudio.noteskmp.core.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class ResultTest {

    private val success: Result<Int, DataError> = Result.Success(2)
    private val failure: Result<Int, DataError> = Result.Error(DataError.Local.DISK_FULL)

    @Test
    fun `map transforms success data`() {
        assertThat(success.map { it * 10 }).isEqualTo(Result.Success(20))
    }

    @Test
    fun `map keeps the error and never calls the transform`() {
        var called = false
        val mapped = failure.map { called = true; it * 10 }

        assertThat(mapped).isEqualTo(Result.Error(DataError.Local.DISK_FULL))
        assertThat(called).isEqualTo(false)
    }

    @Test
    fun `onSuccess runs only for success and returns the same result`() {
        var seen: Int? = null
        assertThat(success.onSuccess { seen = it }).isEqualTo(success)
        assertThat(seen).isEqualTo(2)

        seen = null
        assertThat(failure.onSuccess { seen = it }).isEqualTo(failure)
        assertThat(seen).isNull()
    }

    @Test
    fun `onFailure runs only for error and returns the same result`() {
        var seen: DataError? = null
        assertThat(failure.onFailure { seen = it }).isEqualTo(failure)
        assertThat(seen).isEqualTo(DataError.Local.DISK_FULL)

        seen = null
        assertThat(success.onFailure { seen = it }).isEqualTo(success)
        assertThat(seen).isNull()
    }

    @Test
    fun `chained callbacks fire exactly once each`() {
        val calls = mutableListOf<String>()
        success
            .onSuccess { calls += "success" }
            .onFailure { calls += "failure" }
        failure
            .onSuccess { calls += "success" }
            .onFailure { calls += "failure" }

        assertThat(calls).isEqualTo(listOf("success", "failure"))
    }

    @Test
    fun `asEmptyResult drops data but keeps the error`() {
        assertThat(success.asEmptyResult()).isEqualTo(Result.Success(Unit))
        assertThat(failure.asEmptyResult()).isEqualTo(Result.Error(DataError.Local.DISK_FULL))
    }

    @Test
    fun `success can carry null data`() {
        val nullable: Result<String?, DataError> = Result.Success(null)
        assertThat(nullable.map { it ?: "fallback" }).isEqualTo(Result.Success("fallback"))
    }
}
