package fr.acinq.tor

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking


internal actual fun runSuspendBlocking(block: suspend CoroutineScope.() -> Unit) = runBlocking { block() }

internal actual val cachesDirectoryPath: String get() =
    InstrumentationRegistry.getInstrumentation().context.cacheDir.absolutePath
