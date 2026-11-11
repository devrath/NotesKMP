package com.istudio.noteskmp.core.presentation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEqualTo
import com.istudio.noteskmp.core.domain.DataError
import com.istudio.noteskmp.core.presentation.generated.resources.Res
import com.istudio.noteskmp.core.presentation.generated.resources.error_disk_full
import com.istudio.noteskmp.core.presentation.generated.resources.error_not_found
import com.istudio.noteskmp.core.presentation.generated.resources.error_unknown
import kotlin.test.Test

class DataErrorToUiTextTest {

    private fun DataError.resourceId() =
        (toUiText() as UiText.Resource).id

    @Test
    fun `every local error maps to a string resource`() {
        DataError.Local.entries.forEach { error ->
            assertThat(error.toUiText()).isInstanceOf<UiText.Resource>()
        }
    }

    @Test
    fun `each local error maps to its own message`() {
        assertThat(DataError.Local.DISK_FULL.resourceId()).isEqualTo(Res.string.error_disk_full)
        assertThat(DataError.Local.NOT_FOUND.resourceId()).isEqualTo(Res.string.error_not_found)
        assertThat(DataError.Local.UNKNOWN.resourceId()).isEqualTo(Res.string.error_unknown)
    }

    @Test
    fun `specific errors never fall back to the generic message`() {
        assertThat(DataError.Local.DISK_FULL.resourceId()).isNotEqualTo(Res.string.error_unknown)
        assertThat(DataError.Local.NOT_FOUND.resourceId()).isNotEqualTo(Res.string.error_unknown)
    }
}
