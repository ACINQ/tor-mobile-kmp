package fr.acinq.tor


internal expect fun startTorInThread(args: Array<String>)
internal expect fun isTorInThreadRunning(): Boolean
