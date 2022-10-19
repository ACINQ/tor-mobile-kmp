package fr.acinq.tor.socks

import fr.acinq.secp256k1.Hex
import io.ktor.network.sockets.*
import io.ktor.utils.io.*


public suspend fun socks5Handshake(
    destinationHost: String, destinationPort: Int,
    receive: suspend (into: ByteArray) -> Unit,
    send: suspend (bytes: ByteArray) -> Unit
): Pair<String, Int> {
    send(
        byteArrayOf(
            0x05, // Socks version
            0x01, // One auth method supported
            0x00 // Auth method: no authentication
        ),
    )

    val handshake = ByteArray(2)
    receive(handshake)
    check(handshake[0].toInt() == 0x05) { "Server responded by version: 0x${handshake[0].toString(16)}" }

    when (handshake[1].toInt()) {
        0x00 -> {}
        else -> throw SocksException("Unsupported authentication method: 0x${handshake[1].toString(16)}")
    }

    val destinationHostBytes = destinationHost.encodeToByteArray()
    val destinationPortBytes = ByteArray(Short.SIZE_BYTES).also {
        it[0] = (destinationPort ushr 8 and 0xFF).toByte()
        it[1] = (destinationPort and 0xFF).toByte()
    }

    send(
        byteArrayOf(
            0x05, // Socks Version
            0x01, // Command: connect
            0x00, // Reserved
            0x03, // Address type: domain name
            destinationHostBytes.size.toByte()
        ) +
        destinationHostBytes +
        destinationPortBytes,
    )

    val response = ByteArray(4)
    receive(response)
    check(response[0].toInt() == 0x05) { "Server responded by version: 0x${response[0].toString(16)}" }

    when (response[1].toInt()) {
        0x00 -> {}
        0x01 -> throw SocksServerFailure()
        0x02 -> throw SocksConnectionNotAllowed()
        0x03 -> throw SocksNetworkUnreachable()
        0x04 -> throw SocksHostUnreachable()
        0x05 -> throw SocksConnectionRefused()
        0x06 -> throw SocksException("TTL expired")
        0x07 -> throw SocksException("Command not supported")
        0x08 -> throw SocksException("Address type not supported")
        else -> throw SocksException("Unknown Socks5 error: 0x${response[1].toString(16)}")
    }

    // response[2] is reserved and ignored.

    val connectedHost = when (response[3].toInt()) {
        0x01 -> {
            val ip = ByteArray(4)
            receive(ip)
            "${ip[0]}.${ip[1]}.${ip[2]}.${ip[3]}"
        }
        0x03 -> {
            val length = ByteArray(1)
            receive(length)
            val domain = ByteArray(length[0].toInt())
            receive(domain)
            domain.decodeToString()
        }
        0x04 -> {
            val ip = ByteArray(16)
            receive(ip)
            Hex.encode(ip).chunked(2).joinToString(":")
        }
        else -> error("Unsupported address type: 0x${response[3].toString(16)}")
    }

    val connectedPortBytes = ByteArray(2)
    receive(connectedPortBytes)
    val connectedPort = (
            (connectedPortBytes[0].toInt() and 0xFF shl 8) or
            (connectedPortBytes[1].toInt() and 0xFF)
    )

    return connectedHost to connectedPort
}

public suspend fun Connection.socks5Handshake(destinationHost: String, destinationPort: Int): Pair<String, Int> =
    socks5Handshake(
        destinationHost, destinationPort,
        receive = { input.readFully(it) },
        send = { output.writeFully(it) ; output.flush() }
    )
