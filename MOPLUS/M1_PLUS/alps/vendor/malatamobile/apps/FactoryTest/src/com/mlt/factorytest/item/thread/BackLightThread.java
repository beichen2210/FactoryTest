package com.mlt.factorytest.item.thread;

import com.mlt.factorytest.ItemTestActivity;
import com.mlt.factorytest.item.LcdBackLight;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

/**
 * @ClassName: BackLightThread
 * @Description: A backlight test
 * @Function: Mainly divided into five
 *            methods:renewLCD_Brightness,writeLED_Bright
 *            ,writeLED_Dark,writeKEYPAD_Bright,writeKEYPAD_Dark. The five
 *            methods can be used to screen backlight test, buttons backlight
 *            testing, the LED lamp test
 * @author: peisaisai
 * @date: 2015-01-15 14:42:36 Copyright (c) 2015, mlt All Rights Reserved.
 */
public class BackLightThread implements Runnable {

    private final int MSG_LCD_DARK_BRIGHT = 0;
    private final int MSG_LCD_BRIGHT_DARK = 1;
    private final int MSG_LCD_CONSTANT_DARK = 2;
    private final int MSG_LCD_AOTU_BACKLIGHT = 3;
    private final int MSG_LCD_BRIGHT_DARK_BRIGHT = 4;

    // The screen test maximum brightness and minimum brightness
    private final int MSG_LCD_MAXBRIGHTNESS;
    private final int MSG_LCD_MINBRIGHTNESS;

	// the flag is to judge the current brightness is bright or dark.
	private boolean mIsDarkOrBright = false;

	private Context mContext;
	private Handler mHandler;

	// It said the testing screen brightness
	public static int mCurrentBrightness;

    public BackLightThread(Context context, Handler mHandler) {
        this.mContext = context;
        this.mHandler = mHandler;
        MSG_LCD_MAXBRIGHTNESS = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_screenBrightnessSettingMaximum);
        MSG_LCD_MINBRIGHTNESS = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_screenBrightnessSettingMinimum);
        Log.i("pss", "MSG_LCD_MAXBRIGHTNESS = "+MSG_LCD_MAXBRIGHTNESS);
        Log.i("pss", "MSG_LCD_MINBRIGHTNESS = "+MSG_LCD_MINBRIGHTNESS);
    }

    /** 
     * @MethodName: renewLcdBrightness 
     * @Description:Let the screen brightness value is equal to the value you
      *                  wrote
     * @param brightness  
     * @return void   
     * @throws 
     * Copyright (c) 2015,  mlt All Rights Reserved.
     */
     public static native void renewLcdBrightness(int brightness);

     /** 
     * @MethodName: writeLedRed 
     * @Description:Let the LED lights. 
     * @return void   
     * @throws 
     * Copyright (c) 2015,  mlt All Rights Reserved.
     */
     public static native void writeLedRed();

     /** 
     * @MethodName: writeLedGreen 
     * @Description:Let the LED darks.  
     * @return void   
     * @throws 
     * Copyright (c) 2015,  mlt All Rights Reserved.
     */
     public static native void writeLedGreen();

     /** 
     * @MethodName: writeKeypadBrightness 
     * @Description:Let the The keyboard backlight lights.  
     * @return void   
     * @throws 
     * Copyright (c) 2015,  mlt All Rights Reserved.
     */
     public static native void writeKeypadBrightness();

     static {
         System.loadLibrary("backlightJni");
     }

	@Override
	public void run() {

		// count is used to Calculate the current display screen brightness
		// values
		// The initial value is MSG_LCD_MAXBRIGHTNESS
		int count = MSG_LCD_MAXBRIGHTNESS;
		
//		the num of Light from light to dark, from dark to bright
//		int count_num = 0;

        boolean sleep = false;
        while (true) {
            mCurrentBrightness = count;
            if (unexpectedShutdown()) {
                break;
            }
            if (LcdBackLight.mLcdBackLight.mIsBackLightExit) {
                break;
            }
            if (LcdBackLight.mIsLcdTestPause) {
                continue;
            }

			// To determine the current adjustment is to automatically adjust
			// the brightness or manual adjustment.if it is to automatically
			// adjust
			// the brightness,it will use handler to send a message
			if (isAutoBrightness()) {
				Message msg = new Message();
				msg.arg1 = MSG_LCD_AOTU_BACKLIGHT;
				mHandler.sendMessage(msg);
			} else {

                // Here let thread to sleep for a second is to prevent after you
                // close the automatic adjustment, not to react in the human eye
                // before thread can open test directly
                if (!sleep) {
                    sleeps();
                    sleep = true;
                }
                Log.i("pss", "" + count);
                if (MSG_LCD_MINBRIGHTNESS == count) {
                    mIsDarkOrBright = true;
                    Message msg1 = new Message();
                    msg1.arg1 = MSG_LCD_BRIGHT_DARK_BRIGHT;
                    mHandler.sendMessage(msg1);
                    try {
                        Thread.sleep(1800);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Message msg = new Message();
                    msg.arg1 = MSG_LCD_BRIGHT_DARK_BRIGHT;
                    mHandler.sendMessage(msg);
                }
                if (MSG_LCD_MAXBRIGHTNESS == count) {
                    mIsDarkOrBright = false;
                    Message msg = new Message();
                    msg.arg1 = MSG_LCD_BRIGHT_DARK_BRIGHT;
                    mHandler.sendMessage(msg);

                }
                renewLcdBrightness(count);
                if (mIsDarkOrBright) {
                    count++;
                } else {
                    count--;
                }

			}

		}
//		renewLCD_Brightness(MSG_LCD_MINBRIGHTNESS);
//		Message msg = new Message();
//		msg.arg1 = MSG_LCD_CONSTANT_DARK;
//		mHandler.sendMessage(msg);
	}

    private void sleeps() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @MethodName: isAutoBrightness
     * @Description:To determine the current adjustment is to automatically
     *                 adjust the brightness or manual adjustment
     * @return
     * @return boolean
     * @throws Copyright
     *             (c) 2015, mlt All Rights Reserved.
     */
    public static boolean isAutoBrightness() {
        boolean isAutoAdjustBright = false;
        try {
            isAutoAdjustBright = Settings.System.getInt(
                    ItemTestActivity.itemActivity.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return isAutoAdjustBright;
    }
    
    /** 
     * @MethodName: unexpectedShutdown 
     * @Description:If the program closed unexpectedly, return true
     *              else return false;
     * @return  
     * @return boolean   
     * @throws 
     * Copyright (c) 2015,  mlt All Rights Reserved.
     */
     private boolean unexpectedShutdown(){

         ActivityManager activityManager = (ActivityManager) mContext
                 .getSystemService(Context.ACTIVITY_SERVICE);
       //  Log.i("pss", ""+activityManager.getRunningTasks(1).get(0).topActivity
       //          .getClassName());
         try {
             if (activityManager.getRunningTasks(1).get(0).topActivity
                     .getClassName().equals("com.mlt.factorytest.ItemTestActivity")) {
                 //onDestroy();
                 return false;
             }
         } catch (NullPointerException e) {
         }
         return true;
     }
}
