package fr.acinq.tor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import platform.Foundation.*


internal actual fun runSuspendBlocking(block: suspend CoroutineScope.() -> Unit) {
    var result: Result<Unit>? = null

    MainScope().launch {
        result = kotlin.runCatching { block() }
    }

    while (result == null) NSRunLoop.mainRunLoop.runUntilDate(NSDate.dateWithTimeIntervalSinceNow(0.01))

    result!!.getOrThrow()
}

internal actual val cachesDirectoryPath: String get() =
    NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)[0] as String
