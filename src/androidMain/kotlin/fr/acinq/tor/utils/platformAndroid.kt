package fr.acinq.tor.utils


internal actual suspend fun fileContent(path: String): ByteArray? = TODO()

internal actual suspend fun deleteFile(path: String) { TODO() }

internal actual fun hmacSha256(key: ByteArray, message: ByteArray): ByteArray = TODO()
