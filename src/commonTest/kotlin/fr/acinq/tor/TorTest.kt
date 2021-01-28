package fr.acinq.tor

import fr.acinq.tor.socks.socks5Handshake
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TorTest {

    @InternalAPI
    @Test fun simpleHttpRequest() {
        runSuspendBlocking {
            val tor = Tor(
                dataDirectoryPath = cachesDirectoryPath,
                log = { level, message -> println("${level.name}: $message") }
            )

            tor.start(this)

            val selectorManager = SelectorManager()
            val connection = aSocket(selectorManager)
                .tcp().connect(Tor.SOCKS_ADDRESS, Tor.SOCKS_PORT)
//                .tcp().connect("52.222.230.187", 80) // neverssl.com
                .connection()

            // http://jsonplaceholder.typicode.com/posts/1
            connection.socks5Handshake("172.64.100.5", 80) // neverssl.com
            connection.output.writeFully("GET /posts/1 HTTP/1.0\nhost: jsonplaceholder.typicode.com\n\n".encodeToByteArray())
            connection.output.flush()

            val res = ByteArray(64 * 1024)

            val read = connection.input.readAvailable(res, 0, res.size)
            assertTrue(read > 0)
            assertTrue(res.decodeToString(0, read).length > 50)
            println()

            connection.socket.close()
            selectorManager.close()

            tor.stop()
        }

    }

}
