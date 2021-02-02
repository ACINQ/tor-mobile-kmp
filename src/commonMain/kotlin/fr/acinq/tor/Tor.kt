package fr.acinq.tor

import fr.acinq.tor.utils.*
import fr.acinq.tor.utils.Hex
import fr.acinq.tor.utils.fileContent
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.onReceiveOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.selects.select
import kotlin.random.Random


@OptIn(ExperimentalCoroutinesApi::class, InternalAPI::class)
public class Tor(
    private val dataDirectoryPath: String,
    private val log: (LogLevel, String) -> Unit = { _, _ -> }
) {

    public enum class LogLevel { DEBUG, NOTICE, WARN, ERR }

    private val requestChannel = Channel<Pair<TorControlRequest, CompletableDeferred<TorControlResponse>>>()

    private val controlParser = TorControlParser()

    private val subscribedEvents = listOf("STATUS_CLIENT", "NETWORK_LIVENESS", "NOTICE", "WARN", "ERR")

    private val torState = MutableStateFlow(TorState.STOPPED)
    public val state: StateFlow<TorState> = torState

    public data class TorInfo(val version: String = "unknown", val networkLiveness: String = "unknown")
    private val _info = MutableStateFlow<TorInfo?>(null)
    public val info: StateFlow<TorInfo?> = _info

    private suspend fun tryConnect(selectorManager: SelectorManager, address: String, port: Int, tries: Int): Socket =
        try {
            log(LogLevel.DEBUG, "Connecting to Tor Control on $address:$port")
            aSocket(selectorManager).tcp().connect(address, port)
        } catch (ex: IOException) {
            if (tries == 0) {
                selectorManager.close()
                throw ex
            }
            log(LogLevel.DEBUG, "Tor control is not ready yet")
            delay(200)
            tryConnect(selectorManager, address, port, tries - 1)
        }

    private suspend fun tryRead(file: String): ByteArray {
        var timeWait = 0

        while (true) {
            val content = fileContent(file)
            if (content != null) return content
            if (timeWait >= 5000) break
            delay(100)
            timeWait += 100
        }
        error("$file was not created in 5 seconds")
    }

    public suspend fun start(scope: CoroutineScope) {
        if (isTorInThreadRunning()) {
            log(LogLevel.ERR, "Cannot start Tor as it is already running!")
            return
        }

        log(LogLevel.NOTICE, "Starting Tor thread")

        val portFile = "$dataDirectoryPath/tor-control.port"
        val dataDir = "$dataDirectoryPath/tor-data"

        deleteFile(portFile)

        startTorInThread(arrayOf(
            "tor",
            "__DisableSignalHandlers", "1",
            "SafeSocks", "1",
            "SocksPort", SOCKS_PORT.toString(),
            "NoExec", "1",
            "ControlPort", "auto",
            "ControlPortWriteToFile", portFile,
            "CookieAuthentication", "1",
            "DataDirectory", dataDir,
            "Log", "err file /dev/null" // Logs will be monitored by controller
        ))

        val portString = tryRead(portFile).decodeToString().trim()
        log(LogLevel.DEBUG, "Port control file content: $portString")

        deleteFile(portFile)

        if (!portString.startsWith("PORT=")) error("Invalid port file content: $portString")
        val (address, port) = portString.removePrefix("PORT=").split(":")

        val selectorManager = SelectorManager()

        val socket = tryConnect(selectorManager, address, port.toInt(), 15)
        log(LogLevel.NOTICE, "Connected to Tor Control Socket.")

        val socketRequestChannel = Channel<TorControlRequest>()
        val socketResponseChannel = Channel<TorControlResponse>()

        scope.launch {
            try {
                val readChannel = socket.openReadChannel()
                while (true) {
                    val line = readChannel.readUTF8Line() ?: break
                    controlParser.parseLine(line)?.let { socketResponseChannel.send(it) }
                }
            } catch (ex: IOException) {
                log(LogLevel.NOTICE, "Tor Control Socket disconnected!")
            } finally {
                socketRequestChannel.close()
                socketResponseChannel.close()
                selectorManager.close()
                controlParser.reset()
            }
        }

        scope.launch {
            socket.openWriteChannel(autoFlush = true).use {
                socketRequestChannel.consumeEach { command ->
                    val bytes = command.toProtocolString().encodeToByteArray()
                    this.writeFully(bytes, 0, bytes.size)
                }
            }
        }

        scope.launch {
            do {
                val loop = select<Boolean> {
                    (socketResponseChannel.onReceiveOrNull()) { reply ->
                        if (reply == null) false
                        else {
                            if (reply.isAsync) handleAsyncResponse(reply)
                            //else log(LogLevel.WARN, "Received a sync reply but did not expect one: $reply")
                            true
                        }
                    }
                    requestChannel.onReceive { (cmd, def) ->
                        socketRequestChannel.send(cmd)
                        while (true) {
                            val reply = socketResponseChannel.receive()
                            if (reply.isAsync) handleAsyncResponse(reply)
                            else {
                                def.complete(reply)
                                break
                            }
                        }
                        true
                    }
                }
            } while (loop)
        }

        try {
            val cookie = tryRead("$dataDir/control_auth_cookie")
            log(LogLevel.DEBUG, "Cookie file contains ${cookie.size} bytes")

            val clientNonce = Random.nextBytes(1)
            val clientNonceHex = Hex.encode(clientNonce).toUpperCase()
            log(LogLevel.DEBUG, "Auth challenge client nonce: $clientNonceHex")
            val acResponse = requireCommand(TorControlRequest("AUTHCHALLENGE", listOf("SAFECOOKIE", clientNonceHex)))
            val (acCommand, acValues) = acResponse.replies[0].parseCommandKeyValues()
            if (acCommand != "AUTHCHALLENGE") error("Bad auth challenge reply: $acCommand")
            val serverHash = acValues["SERVERHASH"]?.let { Hex.decode(it) }
                ?: error("Auth challenge reply has no SERVERHASH: $acResponse")
            val serverNonce = acValues["SERVERNONCE"]?.let { Hex.decode(it) }
                ?: error("Auth challenge reply has no SERVERNONCE: $acResponse")

            val authMessage = cookie + clientNonce + serverNonce
            if (!serverHash.contentEquals(hmacSha256(SAFECOOKIE_SERVER_KEY, authMessage))) error("Bad auth server hash!")
            log(LogLevel.DEBUG, "Auth server hash is valid")

            val clientHash = hmacSha256(SAFECOOKIE_CLIENT_KEY, authMessage)
            requireCommand(TorControlRequest("AUTHENTICATE", listOf(Hex.encode(clientHash).toUpperCase())))
            requireCommand(TorControlRequest("TAKEOWNERSHIP"))
            requireCommand(TorControlRequest("SETEVENTS", subscribedEvents))
            getVersion()
        } catch (ex: Throwable) {
            stop()
            throw ex
        }
    }

    public suspend fun getVersion() {
        val response = requireCommand(TorControlRequest("GETINFO", listOf("version")))
        if (response.isSuccess) {
            val version = response.replies[0].reply.removePrefix("version=")
            log(LogLevel.DEBUG, "TOR version: $version")
            _info.value = _info.value?.copy(version = version) ?: TorInfo(version = version)
        }
    }

    private fun handleAsyncResponse(response: TorControlResponse) {
        val (event, firstLine) = response.replies[0].reply.split(' ', limit = 2)
        when (event) {
            "NOTICE" -> log(LogLevel.NOTICE, "TOR: $firstLine")
            "WARN" -> log(LogLevel.WARN, "TOR: $firstLine")
            "ERR" -> log(LogLevel.ERR, "TOR: $firstLine")
            "STATUS_CLIENT" -> {
                val (severity: String, action: String) = firstLine.split(' ')

                val newState = when(action) {
                    "BOOTSTRAP", "ENOUGH_DIR_INFO" -> TorState.STARTING
                    "CIRCUIT_ESTABLISHED" -> TorState.RUNNING
                    else -> TorState.STOPPED
                }

                if (torState.value != newState) {
                    log(LogLevel.valueOf(severity), "TOR: state changed=$newState")
                    torState.value = newState
                }

            }
            "NETWORK_LIVENESS" -> {
                log(LogLevel.DEBUG, "TOR: $event $firstLine")
                _info.value = _info.value?.copy(networkLiveness = firstLine) ?: TorInfo(networkLiveness = firstLine)
            }
            else -> log(LogLevel.WARN, "Received unknown event $event (${response.replies})")
        }
    }

    private suspend fun sendCommand(request: TorControlRequest): TorControlResponse {
        val reply = CompletableDeferred<TorControlResponse>()
        requestChannel.send(request to reply)
        return reply.await()
    }

    private suspend fun requireCommand(request: TorControlRequest): TorControlResponse {
        val response = sendCommand(request)
        if (response.isError) error("${request.command.toLowerCase().capitalize()} error: ${response.status} - ${response.replies[0].reply}")
        log(LogLevel.DEBUG, "${request.command.toLowerCase().capitalize()} success!")
        return response
    }

    public suspend fun stop() {
        if (!isTorInThreadRunning()) {
            log(LogLevel.WARN, "Cannot stop Tor as it is not running!")
            return
        }

        requireCommand(TorControlRequest("SIGNAL", listOf("SHUTDOWN")))
        while (true) {
            if (!isTorInThreadRunning()) break
            delay(20)
        }
        // Fallback if the circuit did not notified us already
        torState.value = TorState.STOPPED
        _info.value = null
    }

    public companion object {
        public const val SOCKS_ADDRESS: String = "127.0.0.1"
        public const val SOCKS_PORT: Int = 34781 // CRC-16 of "Phoenix"!

        private val SAFECOOKIE_SERVER_KEY = "Tor safe cookie authentication server-to-controller hash".encodeToByteArray()
        private val SAFECOOKIE_CLIENT_KEY = "Tor safe cookie authentication controller-to-server hash".encodeToByteArray()
    }
}
