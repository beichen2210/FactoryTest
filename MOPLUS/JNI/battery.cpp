#include <jni.h>
#include <ctype.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <fcntl.h>
#include <pthread.h>
#include <sys/mount.h>
#include <sys/statfs.h>
#include <sys/ioctl.h>
#include <assert.h>
#include <malloc.h>
#include <android/log.h>

#define  LOG_TAG     "BatteryCharging"
//#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define FEATURE_FTM_CLEAREMMC
#define FEATURE_FTM_PMIC_632X
#define FEATURE_FTM_SWCHR_I_68mohm
#define FEATURE_FTM_BATTERY

/* ADC CHannel */
#define ADC_I_SEN 0
#define ADC_BAT_SEN 1
#define ADC_CHARGER 3

int ADC_COUNT = 5;

/* IOCTO */
#define ADC_CHANNEL_READ 		_IOW('k', 4, int)
#define BAT_STATUS_READ 		_IOW('k', 5, int)
#define Set_Charger_Current _IOW('k', 6, int)

/* variate  */
int chargingVoltage = 0;
int ChargingCurrent = 0;
int bat_voltage = 0;
int adc_vbat_current = 0;
int battery_fg_current = 0;
bool   is_charging = false;
bool   charger_exist= false;

#ifdef FEATURE_FTM_PMIC_632X
#define ADC_BAT_FG_CURRENT 66 // magic number
#endif


/*Input : ChannelNUm, Counts*/
/*Output : Sum, Result (0:success, 1:failed)*/
//��ȡ���ADC��Ϣ
 static int get_ADC_channel(int adc_channel, int adc_count)
{
	int fd = -1;
	int ret = 0;
	int adc_in_data[2] = {1,1};

	#ifdef FEATURE_FTM_PMIC_632X
	fd = open("/dev/MT_pmic_adc_cali",O_RDONLY, 0);
	if (fd == -1) {
		//can't open file
	 //LOGI(LOG_TAG "get_ADC_channel - Can't open /dev/MT_pmic_adc_cali\n");
	}else
	{
		//open file
		//LOGI(LOG_TAG "get_ADC_channel - Open /dev/MT6516-adc_cali\n");
	}
	#else
	fd = open("/dev/MT6516-adc_cali",O_RDONLY, 0);
	if (fd == -1) {
		//LOGI(LOG_TAG "get_ADC_channel - Can't open /dev/MT6516-adc_cali\n");
	}
	else
	{
			//LOGI(LOG_TAG "get_ADC_channel - Open /dev/MT6516-adc_cali\n");
	}
	#endif
	adc_in_data[0] = adc_channel;
	adc_in_data[1] = adc_count;
	ret = ioctl(fd, ADC_CHANNEL_READ, adc_in_data);
	close(fd);
	if (adc_in_data[1]==0) {
		//LOGI(LOG_TAG "read channel[%d] %d times : %d\n", adc_channel, adc_count, adc_in_data[0]);
		return adc_in_data[0];
	}
	return -1;
}

/*To get the charging voltage*/
 static jint getChargingVoltage(JNIEnv *env, jclass clazz) {
	 //LOGI(LOG_TAG "in getChargerVoltage");
	int temp = 0;
	temp = get_ADC_channel(ADC_CHARGER, ADC_COUNT);
	//LOGI(LOG_TAG "read temp[%d] \n", temp);
	if (temp != -1) {
		chargingVoltage = (temp/ADC_COUNT);
		if(chargingVoltage > 4100){
			charger_exist = true;
			is_charging = true;
		} else {
			chargingVoltage = 0;
			charger_exist = false;
			is_charging = false;
		}
	}else {
		chargingVoltage = -1;
		charger_exist = false;
		is_charging = false;
	}
	//LOGI(LOG_TAG "read voltage:[%d] \n", chargingVoltage);
	return chargingVoltage;
}

 /*To get the charging current*/
static jint getChargingCurrent(JNIEnv *env, jclass clazz) {

	int temp = 0;
	int tempbat = 0;
	temp = get_ADC_channel(ADC_I_SEN, ADC_COUNT);/* I_sense */

	tempbat = get_ADC_channel(ADC_BAT_SEN, ADC_COUNT);
	bat_voltage = (tempbat/ADC_COUNT);

	//LOGI(LOG_TAG "read temp_current:[%d] \n", temp);
		if (temp != -1) {
			temp = (temp/ADC_COUNT);
			if(charger_exist){
				#ifdef FEATURE_FTM_SWCHR_I_68mohm
				ChargingCurrent = ((temp-bat_voltage)*1000)/56;//68��Ϊ56 ����
			//	LOGI(LOG_TAG "read ChargingCurrent1:[%d] \n", ChargingCurrent);
            	#else
				ChargingCurrent = ((temp-bat_voltage)*10)/2;
			//	LOGI(LOG_TAG "read ChargingCurrent2:[%d] \n", ChargingCurrent);
           	   #endif
			}else {
				ChargingCurrent = 0;
			//	LOGI(LOG_TAG "read ChargingCurrent3:[%d] \n", ChargingCurrent);
			}
		}else {
			ChargingCurrent = -1;
			//LOGI(LOG_TAG "read ChargingCurrent4:[%d] \n", ChargingCurrent);
		}
		return ChargingCurrent;
}

/*battery voltage
 * return�� voltage
 *
 * */
static jint getVoltage(JNIEnv *env, jclass clazz) {
	int temp = 0;
	temp = get_ADC_channel(ADC_BAT_SEN, ADC_COUNT);
	if (temp != -1) {
			bat_voltage = (temp/ADC_COUNT);
			#ifdef FEATURE_FTM_PMIC_632X
			adc_vbat_current = ((bat_voltage)*1024)/(4*1200);
			#else
			adc_vbat_current = ((bat_voltage)*1024)/(2*2800);
			#endif
		} else {
			bat_voltage = -1;
			adc_vbat_current = -1;
		}
	//LOGI(LOG_TAG "read bat_voltage:[%d] \n", bat_voltage);
	return bat_voltage;

}

//cur ad
static jint getCurrent(JNIEnv *env, jclass clazz) {
	int temp = 0;
		temp = get_ADC_channel(ADC_BAT_SEN, ADC_COUNT);
		if (temp != -1) {
				bat_voltage = (temp/ADC_COUNT);
				#ifdef FEATURE_FTM_PMIC_632X
				adc_vbat_current = ((bat_voltage)*1024)/(4*1200);
				#else
				adc_vbat_current = ((bat_voltage)*1024)/(2*2800);
				#endif
			} else {
				bat_voltage = -1;
				adc_vbat_current = -1;
			}
	//	LOGI(LOG_TAG "read adc_vbat_current:[%d] \n", adc_vbat_current);
		return adc_vbat_current;
}

/*battery current
 * return�� current
 *
 * */
static jint getFGCurrent(JNIEnv *env, jclass clazz) {
	#ifdef FEATURE_FTM_PMIC_632X
	int temp = 0;
	temp = get_ADC_channel(ADC_BAT_FG_CURRENT, ADC_COUNT);
	if (temp != -1) {
		battery_fg_current = temp;
	} else {
		battery_fg_current = -1;
	}
	#endif

	return  battery_fg_current;
}

//register
static JNINativeMethod methods[] = {
		{ "getChargingVoltage", "()I",(void *) getChargingVoltage },
		{ "getChargingCurrent", "()I",(void *) getChargingCurrent },
		{ "getVoltage", "()I",(void *) getVoltage },
		{ "getCurrent", "()I",(void *) getCurrent },
		{ "getFGCurrent", "()I",(void *) getFGCurrent },
};

// This function only registers the native methods
static int register_com_mediatek_sensor(JNIEnv *env) {
	jclass clazz = env->FindClass("com/malata/factorytest/item/NativeBatteryMethods");
	return (*env).RegisterNatives(clazz,
				methods, sizeof(methods) /sizeof(methods[0]));
}
//
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env = NULL;
	jint result = -1;
	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		goto bail;
	}
	assert(env != NULL);

	if (register_com_mediatek_sensor(env) < 0) {
		goto bail;
	}

	//success -- return valid version number
	result = JNI_VERSION_1_4;

	bail: return result;
}


