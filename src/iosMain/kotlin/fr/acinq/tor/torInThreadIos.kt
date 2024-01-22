package fr.acinq.tor

import fr.acinq.tor.in_thread.tor_in_thread_get_is_running
import fr.acinq.tor.in_thread.tor_in_thread_start
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCStringArray


@OptIn(ExperimentalForeignApi::class)
internal actual fun startTorInThread(args: Array<String>) {
    memScoped {
        tor_in_thread_start(args.size, args.toCStringArray(this))
    }
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun isTorInThreadRunning(): Boolean =
    tor_in_thread_get_is_running() != 0
