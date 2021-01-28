cmake_minimum_required(VERSION 3.10.0)

add_library( tor_in_thread-jni SHARED
    ${CMAKE_CURRENT_LIST_DIR}/tor_in_thread-jni.c
)

target_include_directories( tor_in_thread-jni
    PUBLIC ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/include
)

target_link_libraries( tor_in_thread-jni
    z
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor_in_thread.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-app.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-compress.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-evloop.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-tls.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-crypt-ops.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libkeccak-tiny.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libed25519_ref10.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-pubsub.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-dispatch.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libcurve25519_donna.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libed25519_donna.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-geoip.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-process.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-llharden.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-buf.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-time.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-fs.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-encoding.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-sandbox.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-container.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-net.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-thread.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-memarea.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-confmgt.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-math.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-meminfo.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-osinfo.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-log.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-lock.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-fdio.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-string.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-term.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-smartlist-core.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-wallclock.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-malloc.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-err.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-version.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-intmath.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-ctime.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libor-trunnel.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libtor-trace.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libssl.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libcrypto.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libevent.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libevent_core.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libevent_extra.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/libevent_pthreads.a
    ${CMAKE_CURRENT_LIST_DIR}/../../../native/build/android/${ANDROID_ABI}/lib/liblzma.a
)
