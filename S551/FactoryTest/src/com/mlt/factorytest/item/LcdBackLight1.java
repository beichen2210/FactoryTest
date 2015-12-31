package com.mlt.factorytest.item;

import com.mlt.factorytest.R;
import com.mlt.factorytest.ItemTestActivity;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class LcdBackLight1 extends AbsHardware {
    private final int MSG_LCD_DARK_CHANGE_BRIGHT = 0;
    private final int MSG_LCD_BRIGHT_CHANGE_DARK = 1;
    private final int MSG_LCD_CONSTANT_DARK = 2;

    // Automatically adjust the brightness has been opened
    private final int MSG_LCD_AOTU_BACKLIGHT_OPEN = 3;
    private final int MSG_LCD_SET_BACKLIGHTNESS = 4;

    // The screen test maximum brightness and minimum brightness
    private final int MSG_LCD_MAXBRIGHTNESS = 255;
    private final int MSG_LCD_MINBRIGHTNESS = 30;

    private final int mBackLightSetSleep = 5;
 
    // the instantiation of BackLight Class
    // Used to invoke the functions in this class or property
    public static LcdBackLight1 mLcdBackLight1;

    // Used to display the prompt bar text
    private TextView mtvLcd;

    // the flag of destruction , if it is true ,the module finished, or it is
    // testing
    public boolean mIsBackLightExit;

    // This flag to control the whole operation process of the activity, the
    // test only once
    private boolean mIsTestShowFlag;

    // The current class environment
    private Context context;

    // The current screen brightness before test
    private int mCurrentBrightness;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mIsBackLightExit = false;
        mIsTestShowFlag = false;
        mCurrentBrightness = getCurrentScreenBrightness();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        acquireWakeLock();
        if (!mIsTestShowFlag) {
            mIsTestShowFlag = true;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub

                    while (true) {
                        
                        if (mIsBackLightExit) {
                            break;
                        }
                        
                        if (isAutoBrightness()) {
                            Message msg = new Message();
                            msg.arg1 = MSG_LCD_AOTU_BACKLIGHT_OPEN;
                            handler.sendMessage(msg);
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            continue;
                        }
                        
                        Message msg1 = new Message();
                        msg1.arg1 = MSG_LCD_BRIGHT_CHANGE_DARK;
                        handler.sendMessage(msg1);
                        for (int i = MSG_LCD_MAXBRIGHTNESS; i >= MSG_LCD_MINBRIGHTNESS; i--) {
                            if (mIsBackLightExit) {
                                break;
                            }
                            Message msg = new Message();
                            msg.arg1 = MSG_LCD_SET_BACKLIGHTNESS;
                            msg.arg2 = i;
                            handler.sendMessage(msg);
                            try {
                                Thread.sleep(mBackLightSetSleep);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        Message msg2 = new Message();
                        msg2.arg1 = MSG_LCD_CONSTANT_DARK;
                        handler.sendMessage(msg2);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        Message msg3 = new Message();
                        msg3.arg1 = MSG_LCD_DARK_CHANGE_BRIGHT;
                        handler.sendMessage(msg3);
                        for (int i = MSG_LCD_MINBRIGHTNESS; i <= MSG_LCD_MAXBRIGHTNESS; i++) {
                            if (mIsBackLightExit) {
                                break;
                            }
                            Message msg = new Message();
                            msg.arg1 = MSG_LCD_SET_BACKLIGHTNESS;
                            msg.arg2 = i;
                            handler.sendMessage(msg);
                            try {
                                Thread.sleep(mBackLightSetSleep);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        releaseWakeLock();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mIsBackLightExit = true;
        mIsTestShowFlag = true;
    }

    public LcdBackLight1(String text, Boolean visible) {
        super(text, visible);
        // TODO Auto-generated constructor stub
    }

    @Override
    public View getView(Context context) {
        // TODO Auto-generated method stub
        mLcdBackLight1 = this;
        this.context = context;
        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.item_lcd_backlight, null);
        mtvLcd = (TextView) view.findViewById(R.id.mtvlcdbacklight);
        return view;
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.arg1) {

            // The screen brightness from dark to bright
            case MSG_LCD_DARK_CHANGE_BRIGHT:
                //mtvLcd.setText(context.getString(R.string.lcd_backlight));
                break;

            // The screen brightness from bright to dark
            case MSG_LCD_BRIGHT_CHANGE_DARK:
                //mtvLcd.setText(context.getString(R.string.lcd_backlight1));
                break;

            // The screen brightness remain dark
            case MSG_LCD_CONSTANT_DARK:
                //mtvLcd.setText(context.getString(R.string.lcd_backlight2));
                break;

            // Automatically adjust the brightness
            case MSG_LCD_AOTU_BACKLIGHT_OPEN:
                mtvLcd.setText(context.getString(R.string.mtvlcdbacklight3));
                break;

            case MSG_LCD_SET_BACKLIGHTNESS:
                WindowManager.LayoutParams lp = ItemTestActivity.itemActivity
                        .getWindow().getAttributes();
                lp.screenBrightness = Float.valueOf(msg.arg2) * (1f / 255f);
                ItemTestActivity.itemActivity.getWindow().setAttributes(lp);
                break;
            default:
                break;
            }
        };
    };

    /**
     * @MethodName: isAutoBrightness
     * @Description:To determine the current adjustment is to automatically
     *                 adjust the brightness or manual adjustment
     * @return
     * @return boolean
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
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

    private int getCurrentScreenBrightness() {
        int brightnessValue = 0;
        try {
            brightnessValue = android.provider.Settings.System.getInt(
                    ItemTestActivity.itemActivity.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return brightnessValue;
    }

    private WakeLock wakeLock = null;

    /**
     * @MethodName: acquireWakeLock
     * @Description:lock the screen
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void acquireWakeLock() {
        if (wakeLock == null) {
            // Log.i("pss", "Acquiring wake lock");
            PowerManager pm = (PowerManager) ItemTestActivity.itemActivity
                    .getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                    context.getClass().getCanonicalName());
            wakeLock.acquire();
        }
    }

    /**
     * @MethodName: releaseWakeLock
     * @Description:release lock
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}
