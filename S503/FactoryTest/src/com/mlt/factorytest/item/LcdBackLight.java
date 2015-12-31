package com.mlt.factorytest.item;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mlt.factorytest.ItemTestActivity;
import com.mlt.factorytest.R;
import com.mlt.factorytest.item.thread.BackLightThread;

/**
 * @ClassName: BackLight
 * @Description: This class is mainly to screen backlight, led lights flashing
 *               and keyboard backlighting for testing
 * @Function: Open the backlight testing thread, control toast display test
 * @author: peisaisai
 * @date: 2015-01-15 11:30:04 Copyright (c) 2015, mlt All Rights Reserved.
 */
public class LcdBackLight extends AbsHardware {

    private final int MSG_LCD_DARK_CHANGE_BRIGHT = 0;
    private final int MSG_LCD_BRIGHT_CHANGE_DARK = 1;
    private final int MSG_LCD_CONSTANT_DARK = 2;
    private final int MSG_LCD_BRIGHT_DARK_BRIGHT = 4;
    // Automatically adjust the brightness has been opened
    private final int MSG_LCD_AOTU_BACKLIGHT_OPEN = 3;

    // if the activity is pause , mIsLcdTestPause is true , or it is false
    // if it is true , test pauses.
    // if it is false , test continues.
    public static boolean mIsLcdTestPause;

    // the instantiation of BackLight Class
    // Used to invoke the functions in this class or property
    public static LcdBackLight mLcdBackLight;

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

    public LcdBackLight(String text, Boolean visible) {
        super(text, visible);
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mIsTestShowFlag = false;
        mCurrentBrightness = getCurrentScreenBrightness();
        mIsBackLightExit = false;
        BackLightThread.mCurrentBrightness = mCurrentBrightness;

        /** set the acitivty title */
        // ItemTestActivity.itemActivity.setTitle(R.string.item_LCDBackLight);
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        releaseWakeLock();
        mIsLcdTestPause = true;
        BackLightThread.renewLcdBrightness(mCurrentBrightness);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsBackLightExit = true;
        BackLightThread.renewLcdBrightness(mCurrentBrightness);
    }

    @Override
    public void onResume() {
        super.onResume();
        acquireWakeLock();
        BackLightThread.renewLcdBrightness(BackLightThread.mCurrentBrightness);
        mIsLcdTestPause = false;
        if (!mIsTestShowFlag) {
            testShow();
            mIsTestShowFlag = true;
        }
    }

    /**
     * @MethodName: testShow
     * @Description: Open a thread to Open the screen backlight testing
     * @return void
     * @throws Copyright
     *             (c) 2015, mlt All Rights Reserved.
     */
    private void testShow() {
        Thread thread = new Thread(new BackLightThread(context, handler));
        thread.start();
    }

    /**
     * @MethodName: getCurrentScreenBrightness
     * @Description:Get the current screen brightness
     * @return
     * @return int
     * @throws Copyright
     *             (c) 2015, mlt All Rights Reserved.
     */
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

    @Override
    public View getView(Context context) {
        mLcdBackLight = this;
        this.context = context;
        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.item_lcd_backlight, null);
        mtvLcd = (TextView) view.findViewById(R.id.mtvlcdbacklight);

        return view;
    }

    /**
     * @Fields: handler TODO：Receives the message, the information displayed on
     *          the screen
     */
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.arg1) {

            // The screen brightness from dark to bright
            case MSG_LCD_DARK_CHANGE_BRIGHT:
                // mtvLcd.setText(context.getString(R.string.lcd_backlight));
                break;

            // The screen brightness from bright to dark
            case MSG_LCD_BRIGHT_CHANGE_DARK:
                // mtvLcd.setText(context.getString(R.string.lcd_backlight1));
                break;

            // The screen brightness remain dark
            case MSG_LCD_CONSTANT_DARK:
                // mtvLcd.setText(context.getString(R.string.lcd_backlight2));
                break;

            // Automatically adjust the brightness
            case MSG_LCD_AOTU_BACKLIGHT_OPEN:
                mtvLcd.setText(context.getString(R.string.mtvlcdbacklight3));
                break;

            case MSG_LCD_BRIGHT_DARK_BRIGHT:
                mtvLcd.setText(context.getString(R.string.mtvlcdbacklight4));
                break;
            default:
                break;
            }
        };
    };

    private WakeLock wakeLock = null;

    /**
     * @MethodName: acquireWakeLock
     * @Description:Without operation to keep the screen in the awakened state
     * @return void
     * @throws Copyright
     *             (c) 2015, mlt All Rights Reserved.
     */
    @SuppressWarnings("deprecation")
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
     * @Description:release the lock of screen
     * @return void
     * @throws Copyright
     *             (c) 2015, mlt All Rights Reserved.
     */
    private void releaseWakeLock() {
        if ((wakeLock != null) && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}
