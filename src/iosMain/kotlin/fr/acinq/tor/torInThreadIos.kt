package fr.acinq.tor

import fr.acinq.tor.in_thread.tor_in_thread_get_is_running
import fr.acinq.tor.in_thread.tor_in_thread_start
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCStringArray


internal actual fun startTorInThread(args: Array<String>) {
    memScoped {
        tor_in_thread_start(args.size, args.toCStringArray(this))
    }
}

internal actual fun isTorInThreadRunning(): Boolean =
    tor_in_thread_get_is_running() != 0
