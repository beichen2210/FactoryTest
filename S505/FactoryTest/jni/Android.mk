LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := libbacklightJni
LOCAL_SRC_FILES := backlightJni.cpp
LOCAL_PACKAGE_NAME := com.mlt.factorytest.item.thread
LOCAL_CERTIFICATE := platform
include $(BUILD_SHARED_LIBRARY)


#chb's Battery c file  about JNI
include $(CLEAR_VARS)
LOCAL_MODULE    := libBatteryJni
LOCAL_SRC_FILES := battery.cpp
LOCAL_PACKAGE_NAME := com.mlt.factorytest.item
LOCAL_CERTIFICATE := platform
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_C_INCLUDES += $(LOCAL_PATH) 
include $(BUILD_SHARED_LIBRARY)



#chb's wifiMAC  c file  about JNI
include $(CLEAR_VARS)
LOCAL_MODULE    := libmacAddrJni
LOCAL_SRC_FILES := macAddr.cpp
LOCAL_C_INCLUDES := macAddr.h
LOCAL_PACKAGE_NAME := com.mlt.factorytest.item
LOCAL_CERTIFICATE := platform
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
include $(BUILD_SHARED_LIBRARY)