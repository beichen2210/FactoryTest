package com.mlt.factorytest.item;

//chb modify for VFOZ-5:2015-7-28 begin
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mlt.factorytest.R;

/**
* @ClassName: AudioMicLoopTest
* @PackageName:com.malata.factorytest.item
* @Description: Test the Mic and receiver
* @author:   chehongbin
* @date:     2015年1月27日 下午1:30:28
* Copyright (c) 2015 MALATA,All Rights Reserved.
*/
public class AudioMicLoopTest extends AbsHardware {
    private static final String TAG = "MICLOOP";
    private Context mContext;
    private boolean mRunningMic = false; //Switch for the maic MIC test process
    private boolean mRunningRefMic = false; // Switch for the sub MIC test process
    private AudioManager mAudioManager = null;
    private TextView mHeadsetout;

    /**
    * @Fields: headSetReceiver
    * @Description：  broadcast receiver:
    *                             Receive the headphones plug, and carries on the processing
    */
    BroadcastReceiver headSetReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "into headsetreceiver!");

                String action = intent.getAction();

                if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                    if (intent.getIntExtra("state", 0) == 1) {
                        Log.d(TAG, "this is headphone plugged");
                        mRunningMic = false;
                        mHeadsetout.setVisibility(View.VISIBLE);
                    } else {
                        mRunningMic = true;
                        
                        new MicloopThread().start();
                        mHeadsetout.setVisibility(View.INVISIBLE);
                    }
                }
            }
        };

    public AudioMicLoopTest(String text, Boolean visible) {
        super(text, visible);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("InflateParams")
	@Override
    public View getView(Context context) {
        this.mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.item_ringtone, null);
        mHeadsetout = (TextView) view.findViewById(R.id.headsetout_tip);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mRunningMic = true;
       
        /**Create the headset plug event listeners of the filter*/
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        mContext.registerReceiver(headSetReceiver, ifilter);

        /** To obtain the audio services*/
        mAudioManager = ((AudioManager) mContext.getSystemService("audio"));
        mRunningMic = true;
        new MicloopThread().start(); // resume the activity ,start the MicloopThread
    }

    @Override
    public void onPause() {
        super.onPause();
        this.mRunningMic = false;
        this.mAudioManager.setParameters("SET_LOOPBACK_TYPE=0");
    }

    public void onStop() {
        super.onStop();
        this.mRunningMic = false;
        this.mAudioManager.setParameters("SET_LOOPBACK_TYPE=0");
        mContext.unregisterReceiver(headSetReceiver); //when stop the activity; unregister the headsetReceiver
    }
    /**
    * @ClassName: MicloopThread
    * @PackageName:com.malata.factorytest.item
    * @Description:Setting up the main mic loopback parameters to 21;
    */
    class MicloopThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(1000L); //At least 1 s between state action

                if (AudioMicLoopTest.this.mRunningMic) {
                    AudioMicLoopTest.this.mAudioManager.setParameters(
                        "SET_LOOPBACK_TYPE=21");
                }
                return;
            } catch (InterruptedException localInterruptedException) {
            }
        }
    }

    /**
    * @ClassName: RefMicloopThread
    * @PackageName:com.malata.factorytest.item
    * @Description: Setting up the sub mic loopback parameters to 25;
    */
    class RefMicloopThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(1000L); //At least 1 s between state action

                if (AudioMicLoopTest.this.mRunningRefMic) {
                    AudioMicLoopTest.this.mAudioManager.setParameters(
                        "SET_LOOPBACK_TYPE=25");
                }
                return;
            } catch (InterruptedException localInterruptedException) {
            }
        }
    }
}
//chb modify for VFOZ-5:2015-7-28 end
