package fr.acinq.tor

import platform.Foundation.*


internal actual val cachesDirectoryPath: String get() =
    NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)[0] as String
