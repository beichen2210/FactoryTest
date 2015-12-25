LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := libbacklightJni
LOCAL_SRC_FILES := libbacklightJni.so
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE_TAGS:= optional
include $(BUILD_PREBUILT)






