package fr.acinq.tor

import androidx.test.platform.app.InstrumentationRegistry

internal actual val cachesDirectoryPath: String get() =
    InstrumentationRegistry.getInstrumentation().context.cacheDir.absolutePath
