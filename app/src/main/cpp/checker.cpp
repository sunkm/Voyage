//
// Created by muchuan on 2022/3/1.
//

#include <jni.h>
#include "checker.h"

void LOGD(const char *string);

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_manchuan_tools_activity_SplashActivity_checkXposed(JNIEnv *env, jobject thiz) {
    // TODO: implement checkXposed()
    jclass classloaderClass = env->FindClass("java/lang/ClassLoader");
    jmethodID getSysLoaderMethod = env->GetStaticMethodID(classloaderClass, "getSystemClassLoader",
                                                          "()Ljava/lang/ClassLoader;");
    jobject classLoader = env->CallStaticObjectMethod(classloaderClass, getSysLoaderMethod);
    jclass dexLoaderClass = env->FindClass("dalvik/system/DexClassLoader");
    jmethodID loadClass = env->GetMethodID(dexLoaderClass, "loadClass",
                                           "(Ljava/lang/String;)Ljava/lang/Class;");
    jstring dir = env->NewStringUTF("de.robv.android.xposed.XposedBridge");
    auto targetClass = (jclass) env->CallObjectMethod(classLoader, loadClass, dir);
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear();
        return false;
    }

    if (targetClass != NULL) {
        jfieldID disableHooksFiled = env->GetStaticFieldID(targetClass, "disableHooks", "Z");
        env->SetStaticBooleanField(targetClass, disableHooksFiled, true);
        jfieldID runtimeFiled = env->GetStaticFieldID(targetClass, "runtime", "I");
        env->SetStaticIntField(targetClass, runtimeFiled, 2);
        return true;
    } else {
        return false;
    }
}