#include <jni.h>



#ifndef _Included_com_malata_factorytest_item_NativeBatteryMethods
#define _Included_com_malata_factorytest_item_NativeBatteryMethods
#ifdef __cplusplus
extern "C" {
#endif

//获取充电电压
JNIEXPORT jint JNICALL Java_com_malata_factorytest_item_NativeBatteryMethods_getChargerVoltage(JNIEnv * env, jobject obj);

JNIEXPORT jint JNICALL Java_com_malata_factorytest_item_NativeBatteryMethods_getChargerVoltage1(JNIEnv * env, jobject obj);


#ifdef __cplusplus
}
#endif
#endif
