#include <jni.h>
#include "stdio.h"
#include "fcntl.h"
#define RED_LED_FILE		"/sys/class/leds/red/brightness"
#define GREEN_LED_FILE		"/sys/class/leds/green/brightness"
#define BLUE_LED_FILE		"/sys/class/leds/blue/brightness"
#define BUTTON_LED_FILE	    "/sys/class/leds/button-backlight/brightness"
#define KEYPAD_LED_FILE	    "/sys/class/leds/keypad-backlight/brightness"
#define BRIGHTNESS_FILE     "/sys/class/leds/lcd-backlight/brightness"
#define LCD_SLEEP_TIME 4100
#define LED_SLEEP_TIME 300000
#define KEYPAD_SLEEP_TIME 600000
static int brightness = 0;
bool bool_brightness = true;
extern "C" {
void write_int(char const* path, int value) {
	int fd;
	if (path == NULL)
		return; //-1;
	fd = open(path, O_RDWR); //���ļ�
	if (fd >= 0) {
		char buffer[20];
		int bytes = sprintf(buffer, "%d\n", value);
		int amt = write(fd, buffer, bytes);
		close(fd);
		if (amt == -1) {
			return;
		} else {
		}
	}
	close(fd);
}

void JNICALL Java_com_mlt_factorytest_item_thread_BackLightThread_renewLcdBrightness(
		JNIEnv *, jobject, jint brightness) {
	write_int(BRIGHTNESS_FILE, brightness);
	usleep(LCD_SLEEP_TIME);
}

/*
 * Class:     com_malata_factorytest_item_thread_BackLightThread
 * Method:    writeLedGreen
 * Signature: (I)V
 */
void JNICALL Java_com_mlt_factorytest_item_thread_BackLightThread_writeLedGreen(
		JNIEnv *, jclass) {
	write_int(GREEN_LED_FILE, 255);
	usleep(LED_SLEEP_TIME);
	write_int(GREEN_LED_FILE, 0);
	usleep(LED_SLEEP_TIME);
}

/*
 * Class:     com_malata_factorytest_item_thread_BackLightThread
 * Method:    writeLedRed
 * Signature: (I)V
 */
void JNICALL Java_com_mlt_factorytest_item_thread_BackLightThread_writeLedRed(
		JNIEnv *, jclass) {
	write_int(RED_LED_FILE, 255);
	usleep(LED_SLEEP_TIME);
	write_int(RED_LED_FILE, 0);
	usleep(LED_SLEEP_TIME);
}

/*
 * Class:     com_malata_factorytest_item_thread_BackLightThread
 * Method:    writeKeypadBrightness
 * Signature: (I)V
 */
void JNICALL Java_com_mlt_factorytest_item_thread_BackLightThread_writeKeypadBrightness(
		JNIEnv *, jclass) {
	write_int(BUTTON_LED_FILE, 255);
	usleep(KEYPAD_SLEEP_TIME);
	write_int(BUTTON_LED_FILE, 0);
		usleep(KEYPAD_SLEEP_TIME);
}

}

