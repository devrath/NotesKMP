package com.istudio.noteskmp.core.domain

/** Errors from local storage. The app has no network layer, so there is no network variant. */
sealed interface DataError : Error {
    enum class Local : DataError {
        DISK_FULL,
        NOT_FOUND,
        UNKNOWN,
    }
}
