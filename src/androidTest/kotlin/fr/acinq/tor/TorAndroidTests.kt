package fr.acinq.tor

import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.acinq.tor.socks.socks5Handshake
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import io.ktor.util.*
import io.ktor.utils.io.*
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@RunWith(AndroidJUnit4::class)
public class TorAndroidTests : TestCase() {

    @OptIn(ExperimentalTime::class, InternalAPI::class)
    @Test public fun sslTest(): Unit = runSuspendTest(Duration.minutes(1)) {
        val tor = Tor(
            dataDirectoryPath = cachesDirectoryPath,
            log = { level, message -> println("${level.name}: $message") }
        )

        tor.start(this)

        try {
            SelectorManager().use { selectorManager ->
                aSocket(selectorManager).tcp().connect(Tor.SOCKS_ADDRESS, Tor.SOCKS_PORT).use { socket ->
                    val connection = socket.connection()

                    connection.socks5Handshake("google.com", 443)
                    connection.tls(Dispatchers.IO).use { tlsSocket ->
                        val tlsConnection = tlsSocket.connection()

                        tlsConnection.output.writeFully("GET / HTTP/1.0\nhost: www.google.com\n\n".encodeToByteArray())
                        tlsConnection.output.flush()

                        val line = tlsConnection.input.readUTF8Line()
                        assertEquals("HTTP/1.0 200 OK", line)
                    }
                }
            }
        } finally {
            tor.stop()
        }
    }

}
