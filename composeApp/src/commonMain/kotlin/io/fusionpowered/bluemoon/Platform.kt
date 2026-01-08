package io.fusionpowered.bluemoon

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform