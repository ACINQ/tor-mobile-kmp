package fr.acinq.tor.utils

import kotlinx.cinterop.*
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreCrypto.kCCHmacAlgSHA256
import platform.Foundation.*
import platform.posix.*


@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun fileContent(path: String): ByteArray? {
    val file = fopen(path, "r") ?: return null
    fseek(file, 0.convert(), SEEK_END)
    val size = ftell(file).toInt()
    fseek(file, 0.convert(), SEEK_SET)
    val bytes = ByteArray(size)
    fread(bytes.refTo(0), 1.convert(), size.convert(), file)
    fclose(file)
    return bytes
}

internal actual suspend fun deleteFile(path: String) {
    remove(path)
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun hmacSha256(key: ByteArray, message: ByteArray): ByteArray {
    val result = ByteArray(CC_SHA256_DIGEST_LENGTH)
    result.usePinned { pinnedResult ->
        key.usePinned { pinnedKey ->
            message.usePinned { pinnedMessage ->
                CCHmac(
                    kCCHmacAlgSHA256,
                    pinnedKey.addressOf(0), key.size.convert(),
                    pinnedMessage.addressOf(0), message.size.convert(),
                    pinnedResult.addressOf(0)
                )
            }
        }
    }
    return result
}
