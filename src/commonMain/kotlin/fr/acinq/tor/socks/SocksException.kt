package fr.acinq.tor.socks


public open class SocksException(message: String) : Exception(message)

public class SocksServerFailure : SocksException("General SOCKS server failure")
public class SocksConnectionNotAllowed : SocksException("Connection not allowed by ruleset")
public class SocksNetworkUnreachable : SocksException("Network unreachable")
public class SocksHostUnreachable : SocksException("Host unreachable")
public class SocksConnectionRefused : SocksException("Connection refused")
