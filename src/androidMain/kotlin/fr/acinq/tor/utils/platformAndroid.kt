package fr.acinq.tor.utils

import java.io.File
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


internal actual suspend fun fileContent(path: String): ByteArray? =
    try {
        File(path).readBytes()
    } catch (_: Throwable) {
        null
    }

internal actual suspend fun deleteFile(path: String) {
    File(path).delete()
}

internal actual fun hmacSha256(key: ByteArray, message: ByteArray): ByteArray {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(key, "HmacSHA256"))
    return mac.doFinal(message)
}
