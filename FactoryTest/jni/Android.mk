LOCAL_PATH := $(call my-dir)
#include $(CLEAR_VARS)
#LOCAL_SHARED_LIBRARIES := libhwm      
#LOCAL_MODULE    := libfactorysensor_jni
#LOCAL_SRC_FILES := com_malata_factorymode_nativesensor.cpp
#LOCAL_CERTIFICATE := platform
#LOCAL_C_INCLUDES += \
#    mediatek/external/sensor-tools \
#    frameworks/base/core/jni \
#    $(PV_INCLUDES) \
#    $(JNI_H_INCLUDE) \
#    $(call include-path-for, corecg graphics)
#include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := libbacklightJni
LOCAL_SRC_FILES := backlightJni.cpp
LOCAL_PACKAGE_NAME := com.malata.factorytest.item.thread
LOCAL_CERTIFICATE := platform
include $(BUILD_SHARED_LIBRARY)


#chb's Battery c file  about JNI
include $(CLEAR_VARS)
LOCAL_MODULE    := libBatteryJni
LOCAL_SRC_FILES := battery.cpp
LOCAL_PACKAGE_NAME := com.malata.factorytest.item
LOCAL_CERTIFICATE := platform
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_C_INCLUDES += $(LOCAL_PATH) 
include $(BUILD_SHARED_LIBRARY)



#chb's wifiMAC  c file  about JNI
include $(CLEAR_VARS)
LOCAL_MODULE    := libmacAddrJni
LOCAL_SRC_FILES := macAddr.cpp
LOCAL_C_INCLUDES := macAddr.h
LOCAL_PACKAGE_NAME := com.malata.factorytest.item
LOCAL_CERTIFICATE := platform
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
include $(BUILD_SHARED_LIBRARY)