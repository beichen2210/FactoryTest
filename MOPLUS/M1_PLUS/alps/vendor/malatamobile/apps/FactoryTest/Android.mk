# Copyright 2007-2008 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4

LOCAL_PACKAGE_NAME := FactoryTest

#LOCAL_DEX_PREOPT := false

#LOCAL_JNI_SHARED_LIBRARIES :=	\
#	libBatteryJni	\
#	libmacAddrJni	\
#	libbacklightJni \
#	libfmjni
	
LOCAL_CERTIFICATE := platform

#反编译指令
LOCAL_PROGUARD_ENABLED := custom
LOCAL_PROGUARD_FLAG_FILES := proguard.flags

#使用MTK 的私有类 必须加入
#LOCAL_PRIVILEGED_MODULE := true
LOCAL_JAVA_LIBRARIES +=mediatek-framework telephony-common

include $(BUILD_PACKAGE)

# This finds and builds the test apk as well, so a single make does both.
include $(call all-makefiles-under,$(LOCAL_PATH))
