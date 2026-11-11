package com.istudio.noteskmp.core.presentation

import com.istudio.noteskmp.core.domain.DataError
import com.istudio.noteskmp.core.presentation.generated.resources.Res
import com.istudio.noteskmp.core.presentation.generated.resources.error_disk_full
import com.istudio.noteskmp.core.presentation.generated.resources.error_not_found
import com.istudio.noteskmp.core.presentation.generated.resources.error_unknown

fun DataError.toUiText(): UiText {
    val id = when (this) {
        DataError.Local.DISK_FULL -> Res.string.error_disk_full
        DataError.Local.NOT_FOUND -> Res.string.error_not_found
        DataError.Local.UNKNOWN -> Res.string.error_unknown
    }
    return UiText.Resource(id)
}
