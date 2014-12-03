APP_ABI := armeabi  armeabi-v7a
APP_CPPFLAGS := --std=c++11
APP_CFLAGS += -Wno-error=format-security
NDK_TOOLCHAIN_VERSION := 4.8
APP_OPTIM := debug
APP_STL := gnustl_static
APP_PLATFORM := android-14