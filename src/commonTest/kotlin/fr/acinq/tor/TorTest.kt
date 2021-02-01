package fr.acinq.tor

import fr.acinq.tor.socks.socks5Handshake
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.minutes
import kotlin.time.seconds

@OptIn(ExperimentalTime::class, InternalAPI::class)
class TorTest {

    @Test fun bootstrap() = runSuspendTest {
        val tor = Tor(
            dataDirectoryPath = cachesDirectoryPath,
            log = { level, message -> println("${level.name}: $message") }
        )

        assertEquals(TorState.STOPPED, tor.state.value)

        tor.start(this)

        tor.state.first { it == TorState.STARTING }
        tor.state.first { it == TorState.RUNNING }

        tor.stop()

        tor.state.first { it == TorState.STOPPED }
    }

    @Test fun simpleHttpRequest() = runSuspendTest {
        val tor = Tor(
            dataDirectoryPath = cachesDirectoryPath,
            log = { level, message -> println("${level.name}: $message") }
        )

        tor.start(this)

        try {
            SelectorManager().use { selectorManager ->
                aSocket(selectorManager).tcp().connect(Tor.SOCKS_ADDRESS, Tor.SOCKS_PORT).use { socket ->
                    val connection = socket.connection()

                    connection.socks5Handshake("www.google.com", 80)
                    connection.output.writeFully("GET / HTTP/1.0\nhost: www.google.com\n\n".encodeToByteArray())
                    connection.output.flush()

                    val line = connection.input.readUTF8Line()
                    assertEquals("HTTP/1.0 200 OK", line)
                }
            }
        } finally {
            tor.stop()
        }
    }

}
