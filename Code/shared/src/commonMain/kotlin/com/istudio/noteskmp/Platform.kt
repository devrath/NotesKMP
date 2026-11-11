package com.istudio.noteskmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform