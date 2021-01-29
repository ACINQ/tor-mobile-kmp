package fr.acinq.tor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds


internal expect val cachesDirectoryPath: String

internal expect fun runSuspendBlocking(block: suspend CoroutineScope.() -> Unit)

@OptIn(ExperimentalTime::class)
fun runSuspendTest(timeout: Duration = 30.seconds, test: suspend CoroutineScope.() -> Unit) {
    runSuspendBlocking {
        withTimeout(timeout) {
            launch {
                test()
                cancel()
            }.join()
        }
    }
}
