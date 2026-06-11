package io.jadie

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform