# Tor Mobile KMP

**A Kotlin Multiplatform library for Android & iOS to start, connect to, and control a Tor proxy.**

This library only targets Android & iOS, it cannot be used in desktop JVMs, nor can it be used on other Kotlin/Native targets.

- Uses Tor as a Library and starts in an application thread.
- Controls Tor through the Tor Control port (with cookie file authentication).
- Provides a Socks5 handshake implementation that's easy to plug in any network socket system (provides a simple usage for Ktor-Sockets).