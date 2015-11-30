#include <jni.h>
#include "stdio.h"
#include "fcntl.h"
#define RED_LED_FILE		"/sys/class/leds/red/brightness"
#define GREEN_LED_FILE		"/sys/class/leds/green/brightness"
#define BLUE_LED_FILE		"/sys/class/leds/blue/brightness"
#define BUTTON_LED_FILE	    "/sys/class/leds/button-backlight/brightness"
#define KEYPAD_LED_FILE	    "/sys/class/leds/keypad-backlight/brightness"
#define BRIGHTNESS_FILE     "/sys/class/leds/lcd-backlight/brightness"
#define LCD_SLEEP_TIME 250000
#define LED_SLEEP_TIME 300000
static int brightness_seq[] =
		{ 255,200, 150, 100, 50, 25, 10, -1 };
static int seq_index = 0;
extern "C" {
void write_int(char const* path, int value) {
	int fd;
	if (path == NULL)
		return; //-1;
	fd = open(path, O_RDWR); //打开文件
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
jboolean Java_com_malata_factorytest_item_thread_BackLightThread_LCD_1Test(
		JNIEnv *, jobject) {
	write_int(BRIGHTNESS_FILE, brightness_seq[seq_index++]);
	usleep(LCD_SLEEP_TIME);
	if (brightness_seq[seq_index] == -1) {
		sleep(2);
		seq_index = 0;
		write_int(BRIGHTNESS_FILE, 125); //暂时定为这些
		usleep(LCD_SLEEP_TIME);
		return false;
	}
	return true;
}

/*
 * Class:     com_malata_factorytest_item_thread_BackLightThread
 * Method:    LED_Test
 * Signature: ()V
 */
jboolean Java_com_malata_factorytest_item_thread_BackLightThread_LED_1Test(
		JNIEnv *, jobject) {
		write_int(GREEN_LED_FILE, 255);
		usleep(LED_SLEEP_TIME);
		write_int(GREEN_LED_FILE, 0);
		usleep(LED_SLEEP_TIME);
		write_int(RED_LED_FILE, 255);
		usleep(LED_SLEEP_TIME);
		write_int(RED_LED_FILE, 0);
		usleep(LED_SLEEP_TIME);
		return true;
}

/*
 * Class:     com_malata_factorytest_item_thread_BackLightThread
 * Method:    KEY_Test
 * Signature: ()V
 */
jboolean Java_com_malata_factorytest_item_thread_BackLightThread_KEY_1Test(
		JNIEnv *, jobject) {
		write_int(BUTTON_LED_FILE, 255);
		usleep(500000);
		write_int(BUTTON_LED_FILE, 0);
		usleep(500000);
	return true;
}
}

