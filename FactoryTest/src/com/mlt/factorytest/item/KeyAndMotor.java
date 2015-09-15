package com.mlt.factorytest.item;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mlt.factorytest.R;
import com.mlt.factorytest.ItemTestActivity;

/**
 * @ClassName: KeyAndMotor
 * @Description: This class is for keys and vibrator test
 * @Function: Mainly for access to the module test vibrator vibration, as well
 *            as the key block, when press a keystroke, displays the effect on
 *            the interface
 * @author: peisaisai
 * @date: 2015-01-15 13:44:32 Copyright (c) 2015, Malata All Rights Reserved.
 */
public class KeyAndMotor extends AbsHardware {

    // HOME_KEY Block of code
    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;

    // /PSS: power-off FactoryTest @{
    //private static final String FACTORYTEST_NORMAL_POWERKEY_SHUTDOWN_ACTION = "android.intent.action.factoryTestshutdown";
    //private static final String FACTORYTEST_NORMAL_POWERKEY_RENEW_ACTION = "android.intent.action.factoryTestrenew";
    //private static final String FACTORYTEST_NORMAL_POWERKEY_DISABLE_ACTION = "android.intent.action.factoryTestdisable";
    // /@}

    // pss FactoryTest 20150131 begin
    private static final String FACTORYTEST_CLOSERECENTAPPS_ACTION = "com.android.factorytest.closerecentapps";
    private static final String FACTORYTEST_MENU_DISABLE_ACTION = "com.android.factorytest.menudisable";
    //private static final String FACTORYTEST_HOME_DISABLE_ACTION = "com.android.factorytest.homedisable";
    private static final String FACTORYTEST_STARTRECENTAPPS_ACTION = "com.android.factorytest.startrecentapps";
    // end
    
    private final int BACK = 0;
    private final int MENU = 1;
    private final int RECENTAPPS = 1;
    private final int VOLUME_UP = 2;
    private final int VOLUME_DOWN = 3;
    private final int POWER = 4;
    private final int HOME = 5;
    private final int MOTOR = 6;
    
    // the number of test cases
    private final int TESTCASE_MAX_NUM = 6;// chb modify for LFZS-119:factorytest deleted power_key test 2015.7.23 

    private Context mContext;
    
    // The ID of key button
    private TextView mtvVolumeUp,
                     //mtvPower,// chb modify for LFZS-119:factorytest deleted power_key test 2015.7.23 
                     mtvVolumeDown, 
                     mtvMenu,
                     mtvHome,
                     mtvBack;

    private Button mbtMotorStop;
    private int mKeyMotorTestCount;
    private int []mKeyMotorFlag;

    // Frequency of the vibration of the vibrator
    private long[] mPattern = new long[] { 500, 500, 500, 500 };

    // Vibrator class instantiation
    private TipHelper mTipHelper;

    public KeyAndMotor(String text, Boolean visible) {
        super(text, visible);
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        ItemTestActivity.itemActivity.getWindow().setFlags(
                FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);
        mKeyMotorFlag = new int[]{0, 0, 0, 0, 0, 0, 0};
        mKeyMotorTestCount = 0;
        //motorTest(); // chb modify for LFZS-119:factorytest deleted power_key test 2015.7.23 
        super.onCreate();
    }

    /**
     * @MethodName: startKeyMotorThread
     * @Description:In order to determine whether the test over start a thread
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void judgeKeyMotorExit(int count) {
        // TODO Auto-generated method stub
        if (count == TESTCASE_MAX_NUM) {
            Message msg = new Message();
            msg.what = ItemTestActivity.itemActivity.MSG_BTN_PASS_CLICKABLE;
            ItemTestActivity.itemActivity.handler.sendMessage(msg);
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        motorTest(); // chb modify for LFZS-119:factorytest deleted power_key test 2015.7.23 
        IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction(FACTORYTEST_NORMAL_POWERKEY_DISABLE_ACTION);
        intentFilter.addAction(FACTORYTEST_MENU_DISABLE_ACTION);
        //intentFilter.addAction(FACTORYTEST_HOME_DISABLE_ACTION);
       
        // send a broadcast to stop the function of the power button
        mContext.registerReceiver(mKeyReceiver, intentFilter);
        //Intent powerIntent = new Intent();
       // powerIntent.setAction(FACTORYTEST_NORMAL_POWERKEY_SHUTDOWN_ACTION);
        //mContext.sendBroadcast(powerIntent);

        // send a broadcast to stop the function of the menu button
        Intent menuIntent = new Intent();
        menuIntent.setAction(FACTORYTEST_CLOSERECENTAPPS_ACTION);
        mContext.sendBroadcast(menuIntent);

        acquireWakeLock();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mContext.unregisterReceiver(mKeyReceiver);
        mTipHelper.cancel(); // chb modify for LFZS-119:factorytest deleted power_key test 2015.7.23 
        // send a broadcast to start the function of the power button
        //Intent powerIntent = new Intent();
        //powerIntent.setAction(FACTORYTEST_NORMAL_POWERKEY_RENEW_ACTION);
       // mContext.sendBroadcast(powerIntent);

        // send a broadcast to start the function of the menu button
        Intent menuIntent = new Intent();
        menuIntent.setAction(FACTORYTEST_STARTRECENTAPPS_ACTION);
        mContext.sendBroadcast(menuIntent);

        releaseWakeLock();
    }

    /**
     * @MethodName: MotorStop_OnClick
     * @Description:Stop button events of vibrator
     * @param v
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    public void onClickMotorStop(View v) {
        mTipHelper.cancel();
        mbtMotorStop.setVisibility(View.INVISIBLE);
    }

    /**
     * @MethodName: motorTest
     * @Description:start MotorTest
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void motorTest() {

        // Create an instance and open the vibrator, and closed with button
        // events to control the vibrator
    	// chb modify for LFZS-119:factorytest deleted power_key test 2015.7.23 begin
    	 if (0 == mKeyMotorFlag[MOTOR]){
	        mTipHelper = new TipHelper(ItemTestActivity.itemActivity);
	        mTipHelper.vibrate(mPattern, true);
    	 }
    	// chb modify for LFZS-119:factorytest deleted power_key test 2015.7.23 end
        mbtMotorStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                onClickMotorStop(arg0);
                if (0 == mKeyMotorFlag[MOTOR]) {
                    mKeyMotorFlag[MOTOR] = 1;
                    mKeyMotorTestCount++;
                    judgeKeyMotorExit(mKeyMotorTestCount);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        mTipHelper.cancel();
        super.onDestroy();
    }

    @Override
    public View getView(Context context) {
        // TODO Auto-generated method stub\
        this.mContext = context;
        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.item_key_motor, null);
        mtvBack = (TextView) view.findViewById(R.id.mtvback);
        mtvHome = (TextView) view.findViewById(R.id.mtvhome);
        mtvMenu = (TextView) view.findViewById(R.id.mtvmenu);
        //mtvPower = (TextView) view.findViewById(R.id.mtvpower);// chb modify for LFZS-119:factorytest deleted power_key test 2015.7.23 
        mtvVolumeDown = (TextView) view.findViewById(R.id.mtvvolumedown);
        mtvVolumeUp = (TextView) view.findViewById(R.id.mtvvolumeup);
        mbtMotorStop = (Button) view.findViewById(R.id.mbtmotorStop);

        // send a message to stop the function of the pass button
        Message msg = new Message();
        msg.what = ItemTestActivity.itemActivity.MSG_BTN_PASS_UNCLICKABLE;
        ItemTestActivity.itemActivity.handler.sendMessage(msg);
        return view;
    }

    /*
     * (non-Javadoc)
     * 
     * @Description: Six physical button on the screen, but no power rows block,
     * to modify the framework layer
     * 
     * @see com.malata.factorytest.item.AbsHardware#onKeyDown(int,
     * android.view.KeyEvent) Copyright (c) 2015, Malata All Rights Reserved.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mtvBack.setVisibility(View.INVISIBLE);
            if (0 == mKeyMotorFlag[BACK]) {
                mKeyMotorFlag[BACK] = 1;
                mKeyMotorTestCount++;
                judgeKeyMotorExit(mKeyMotorTestCount);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mtvVolumeDown.setVisibility(View.INVISIBLE);
            if (0 == mKeyMotorFlag[VOLUME_DOWN]) {
                mKeyMotorFlag[VOLUME_DOWN] = 1;
                mKeyMotorTestCount++;
                judgeKeyMotorExit(mKeyMotorTestCount);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mtvVolumeUp.setVisibility(View.INVISIBLE);
            if (0 == mKeyMotorFlag[VOLUME_UP]) {
                mKeyMotorFlag[VOLUME_UP] = 1;
                mKeyMotorTestCount++;
                judgeKeyMotorExit(mKeyMotorTestCount);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            mtvMenu.setVisibility(View.INVISIBLE);
            if (0 == mKeyMotorFlag[MENU]) {
                mKeyMotorFlag[MENU] = 1;
                mKeyMotorTestCount++;
                judgeKeyMotorExit(mKeyMotorTestCount);
            }
            return true;
        } else if ((keyCode == KeyEvent.KEYCODE_HOME)
                && (event.getRepeatCount() == 0)) {
            mtvHome.setVisibility(View.INVISIBLE);
            if (0 == mKeyMotorFlag[HOME]) {
                mKeyMotorFlag[HOME] = 1;
                mKeyMotorTestCount++;
                judgeKeyMotorExit(mKeyMotorTestCount);
            }
            return true;
        }
      // chb modify for LFZS-119:factorytest deleted power_key test 2015.7.23  begin
        /*else if ((keyCode == KeyEvent.KEYCODE_POWER)
                && (event.getRepeatCount() == 0)) {
            mtvPower.setVisibility(View.INVISIBLE);
            if (0 == mKeyMotorFlag[POWER]) {
                mKeyMotorFlag[POWER] = 1;
                mKeyMotorTestCount++;
                judgeKeyMotorExit(mKeyMotorTestCount);
            }
            return true;
        }*/
     // chb modify for LFZS-119:factorytest deleted power_key test 2015.7.23 end
        else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * @ClassName: TipHelper
     * @Description: Vibrator class mainly realize the two vibration modes, one
     *               is a vibration only once, the other is a continuous
     *               vibration
     * @Function: TODO ADD FUNCTION
     * @author: peisaisai
     * @date: 2015-01-15 11:45:33 Copyright (c) 2015, Malata All Rights
     *        Reserved.
     */
    private class TipHelper {
        Activity activity;
        Vibrator vib;
        public TipHelper(ItemTestActivity itemActivity) {
            // TODO Auto-generated constructor stub
            this.activity = itemActivity;
            vib = (Vibrator) activity
                    .getSystemService(Service.VIBRATOR_SERVICE);
        }

        //
        public void vibrate(long milliseconds) {
            vib.vibrate(milliseconds);
        }
        public void vibrate(long[] pattern, boolean isRepeat) {
            vib.vibrate(pattern, isRepeat ? 1 : -1);
        }
        public void cancel(){
            vib.cancel();
        }
    }

    private WakeLock mWakeLock = null;

    /**
     * @MethodName: acquireWakeLock
     * @Description:Without operation to keep the screen in the awakened state
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void acquireWakeLock() {
        if (mWakeLock == null) {
            // Log.i("pss", "Acquiring wake lock");
            PowerManager pm = (PowerManager) ItemTestActivity.itemActivity
                    .getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                    mContext.getClass().getCanonicalName());
            mWakeLock.acquire();
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

        // Log.i("pss", "Releasing wake lock");
        if ((mWakeLock != null) && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }

    }

    /**
     * @Fields: mKeyReceiver TODO：receive two broadcasts to make the power
     *          button and the menu button disappear
     */
    public BroadcastReceiver mKeyReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context mContext, Intent mIntent) {
            // TODO Auto-generated method stub
            String action = mIntent.getAction();
            /*if (action.equals(FACTORYTEST_NORMAL_POWERKEY_DISABLE_ACTION)) {
                mtvPower.setVisibility(View.INVISIBLE);
                if (0 == mKeyMotorFlag[POWER]) {
                    mKeyMotorFlag[POWER] = 1;
                    mKeyMotorTestCount++;
                    judgeKeyMotorExit(mKeyMotorTestCount);
                }
            } else */if (action.equals(FACTORYTEST_MENU_DISABLE_ACTION)) {
                mtvMenu.setVisibility(View.INVISIBLE);
                if (0 == mKeyMotorFlag[MENU]) {
                    mKeyMotorFlag[MENU] = 1;
                    mKeyMotorTestCount++;
                    judgeKeyMotorExit(mKeyMotorTestCount);
                }
            }/*else if(action.equals(FACTORYTEST_HOME_DISABLE_ACTION)){
				mtvHome.setVisibility(View.INVISIBLE);
				if(0 == mKeyMotorFlag[HOME]){
					mKeyMotorFlag[HOME] = 1;
                    mKeyMotorTestCount++;
                    judgeKeyMotorExit(mKeyMotorTestCount);
				}
			}*/
        }
    };

}
