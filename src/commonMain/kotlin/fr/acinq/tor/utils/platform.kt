package fr.acinq.tor.utils


internal expect suspend fun fileContent(path: String): ByteArray?
internal expect suspend fun deleteFile(path: String)

internal expect fun hmacSha256(key: ByteArray, message: ByteArray): ByteArray
