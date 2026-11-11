package com.istudio.noteskmp.core.presentation

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class UiTextTest {

    @Test
    fun `dynamic string resolves to its value unchanged`() = runTest {
        val value = "Café 📝 مرحبا  \n"
        assertThat(UiText.DynamicString(value).asStringAsync()).isEqualTo(value)
    }

    @Test
    fun `empty dynamic string stays empty`() = runTest {
        assertThat(UiText.DynamicString("").asStringAsync()).isEqualTo("")
    }
}
