package fr.acinq.tor


internal data class TorControlRequest(val command: String, val arguments: List<String> = emptyList(), val data: String? = null)

internal fun TorControlRequest.toProtocolString(): String {
    val line = command + " " + arguments.joinToString(" ")
    return if (data == null) "${line}\r\n"
    else "+${line}\r\n${data}\r\n.\r\n"
}

internal data class TorControlResponse(val status: Int, val replies: List<Reply>) {
    data class Reply(val reply: String, val data: String?)
}

internal val TorControlResponse.isSuccess get() = status in 200..299
internal val TorControlResponse.isError get() = status in 400..599
internal val TorControlResponse.isAsync get() = status in 600..699

internal fun TorControlResponse.Reply.parseCommandKeyValues(): Pair<String, Map<String, String>> {
    val (command, keyValues) = reply.split(" ", limit = 2)
    val map = keyValues
        .split(" ")
        .map { it.split("=", limit = 2) }
        .map { it[0] to it[1] }
        .toMap()
    return command to map
}

public enum class TorState { STARTING, RUNNING, STOPPED }