LOCAL_PATH := $(call my-dir)
 
include $(CLEAR_VARS)
 
LOCAL_MODULE:= libspeex
LOCAL_CFLAGS = -DFIXED_POINT -DUSE_KISS_FFT -DEXPORT="" -UHAVE_CONFIG_H
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include \
$(LOCAL_PATH)/lib \

 
LOCAL_SRC_FILES :=\
speex_jni.cpp \
lib/Log.cpp \
data_core/base/HashTable.cpp \
data_core/base/List.cpp \
data_core/base/Queue.cpp \
data_core/base/MemoryManagement.cpp \
data_core/base/JSObject.cpp \
test.cpp \

LOCAL_LDLIBS := -landroid -llog 
LOCAL_CPPFLAGS := -pthread                    \
       --std=c++11                                       \
       -D__GXX_EXPERIMENTAL_CXX0X__     \
       -D_GLIBCXX_HAS_GTHREADS             \
       -fpermissive
       
LOCAL_DISABLE_FORMAT_STRING_CHECKS := true
include $(BUILD_SHARED_LIBRARY)