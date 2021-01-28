package fr.acinq.tor


internal object TorInThreadNative {
    @JvmStatic external fun start(args: Array<String>)
    @JvmStatic external fun isRunning(): Boolean
}

private var loaded = false
private fun load() {
    if (loaded) return
    System.loadLibrary("tor_in_thread-jni")
}

internal actual fun startTorInThread(args: Array<String>) {
    load()
    TorInThreadNative.start(args)
}

internal actual fun isTorInThreadRunning(): Boolean {
    load()
    return TorInThreadNative.isRunning()
}
