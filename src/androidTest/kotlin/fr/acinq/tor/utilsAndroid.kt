package fr.acinq.tor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking


internal actual fun runSuspendBlocking(block: suspend CoroutineScope.() -> Unit) = runBlocking { block() }

internal actual val cachesDirectoryPath: String get() =
    TODO()
