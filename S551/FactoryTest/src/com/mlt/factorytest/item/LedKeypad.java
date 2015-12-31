package com.mlt.factorytest.item;

import com.mlt.factorytest.R;
import com.mlt.factorytest.R.string;
import com.mlt.factorytest.ItemTestActivity;
import com.mlt.factorytest.item.thread.BackLightThread;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LedKeypad extends AbsHardware {

    private Context context;

    // the flag of LedTest, if it is true ,LedTest finished
    private boolean mIsLedTestExit;

    // the flag of KeypadTest, if it is true ,KeypadTest finish
    private boolean mIsKeypadTestExit;

    // the flag of LedKeypadTest, if it is true ,LedkeypadTest pauses.
    private boolean mIsLedKeypadTestPause;

    // the flag of LedKeypadTest, if it is true ,LedkeypadTest finishes.
    private boolean mIsLedKeypadExit;


    public LedKeypad(String text, Boolean visible) {
        super(text, visible);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mIsLedTestExit = false;
        mIsKeypadTestExit = false;
        mIsLedKeypadExit = false;

        /** set the acitivty title */
        // ItemTestActivity.itemActivity.setTitle(R.string.item_BackLight);
    }
    
    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        ledTest();
        keypadTest();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        acquireWakeLock();
        mIsLedKeypadTestPause = false;
		mIsLedTestExit = false;
        mIsKeypadTestExit = false;
        mIsLedKeypadExit = false;
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        releaseWakeLock();
        mIsLedKeypadTestPause = true;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mIsLedKeypadExit = true;
    }

    /**
     * @MethodName: LedTest
     * @Description: start LedTest.
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void ledTest() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (true) {
                    if (unexpectedShutdown()) {
                        break;
                    }
                    if (mIsLedKeypadTestPause) {
                        continue;
                    }
                    if (mIsLedTestExit) {
                        mIsLedTestExit = false;
                        break;
                    }
                    if (mIsLedKeypadExit) {
                        break;
                    }
                    BackLightThread.writeLedGreen();
                    if (mIsLedTestExit) {
                        mIsLedTestExit = false;
                        break;
                    }
                    if (mIsLedKeypadExit) {
                        break;
                    }
                    BackLightThread.writeLedRed();
                }
            }
        }).start();
    }

    /**
     * @MethodName: KeypadTest
     * @Description: start KeypadTest
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void keypadTest() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (true) {
                    if (unexpectedShutdown()) {
                        break;
                    }
                    if (mIsLedKeypadExit) {
                        break;
                    }
                    if (mIsLedKeypadTestPause) {
                        continue;
                    }
                    if (mIsKeypadTestExit) {
                        mIsKeypadTestExit = false;
                        break;
                    }
                    if (mIsLedKeypadExit) {
                        break;
                    }
                    BackLightThread.writeKeypadBrightness();
                    if (mIsKeypadTestExit) {
                        mIsKeypadTestExit = false;
                        break;
                    }
                    if (mIsLedKeypadExit) {
                        break;
                    }
                    BackLightThread.writeKeypadBrightness();
                }
            }
        }).start();
    }

    @Override
    public View getView(Context context) {
        // TODO Auto-generated method stub
        this.context = context;
        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.item_ledkeypad_backlight, null);
        return view;
    }

    // unused pss 20150206
    private WakeLock wakeLock = null;

    /**
     * @MethodName: acquireWakeLock
     * @Description:Without operation to keep the screen in the awakened state
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
     * @Description:release the lock of screen
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void releaseWakeLock() {
        if ((wakeLock != null) && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }

    }
    
    /** 
     * @MethodName: unexpectedShutdown 
     * @Description:If the program closed unexpectedly, return true
     *              else return false;
     * @return  
     * @return boolean   
     * @throws 
     * Copyright (c) 2015,  malata All Rights Reserved.
     */
     private boolean unexpectedShutdown(){
         ActivityManager activityManager = (ActivityManager) context
                 .getSystemService(Context.ACTIVITY_SERVICE);
         try {
             if (activityManager.getRunningTasks(1).get(0).topActivity
                     .getClassName().equals("com.mlt.factorytest.ItemTestActivity")) {
                 
                 return false;
             }
         } catch (NullPointerException e) {
         }
         onDestroy();
         return true;
     }
}
