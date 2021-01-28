package fr.acinq.tor

import kotlinx.coroutines.CoroutineScope


internal expect fun runSuspendBlocking(block: suspend CoroutineScope.() -> Unit)

internal expect val cachesDirectoryPath: String
