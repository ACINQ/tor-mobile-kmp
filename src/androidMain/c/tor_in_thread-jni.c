#include "tor_in_thread.h"

#include <jni.h>
#include <stdlib.h>

JNIEXPORT void JNICALL Java_fr_acinq_tor_TorInThreadNative_start
  (JNIEnv *env, jclass clazz, jobjectArray argArray)
{
    jsize argc = (*env)->GetArrayLength(env, argArray);

    const char **argv = (const char **) malloc(argc * sizeof(char *));
    for (int i = 0; i < argc; ++i) {
        jstring arg = (jstring) (*env)->GetObjectArrayElement(env, argArray, i);
        argv[i] = (*env)->GetStringUTFChars(env, arg, NULL);
    }

    tor_in_thread_start(argc, argv);

    for (int i = 0; i < argc; ++i) {
        jstring arg = (jstring) (*env)->GetObjectArrayElement(env, argArray, i);
        (*env)->ReleaseStringUTFChars(env, arg, argv[i]);
    }

    free(argv);
}

JNIEXPORT jboolean JNICALL Java_fr_acinq_tor_TorInThreadNative_isRunning
  (JNIEnv *env, jclass clazz)
{
    return tor_in_thread_get_is_running();
}
