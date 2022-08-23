# Tor Mobile KMP

**A Kotlin Multiplatform library for Android & iOS to start, connect to, and control a Tor proxy.**

This library only targets Android & iOS, it cannot be used in desktop JVMs, nor can it be used on other Kotlin/Native targets.

- Uses Tor as a Library and starts in an application thread.
- Controls Tor through the Tor Control port (with cookie file authentication).
- Provides a Socks5 handshake implementation that's easy to plug in any network socket system (provides a simple usage for Ktor-Sockets).

### Build

This library contains submodules that must be pulled. To do so, after cloning the repository, run:

```shell
git submodule update --init
```

You then need to install `autoconf` `automake` `coreutils` `gettext` `libtool` `po4a` to build those native libraries.

```shell
brew install autoconf automake coreutils gettext libtool po4a
```

:warning: **autoconf 2.69 required!**

Some libraries this project depends on do not support latest autoconf versions. You need to use autoconf 2.69.

```shell
brew install autoconf@2.69
brew link --overwrite autoconf@2.69
```

You can then build the library proper:

```shell
./gradlew clean build
# to install on your local maven repository
./gradlew publishToMavenLocal
```
