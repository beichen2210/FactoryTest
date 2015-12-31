package com.mlt.factorytest.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mlt.factorytest.R;
import com.mlt.factorytest.ItemTestActivity;

/**
 * @ClassName: RemoteControl
 * @Description: This class is used to launch signals
 * @Function: Test the remote control
 * @author: huangguoxiong
 * @date: 2015-2-11 am9:45:59 Copyright (c) 2015, Malata All Rights Reserved.
 */
public class RemoteControl extends AbsHardware {
    // The RemoteControl UI
    private View mView;
    // Used to call sendIR method
    private IrControl mIda;
    // Tell the user whether there is a remote control function
    private TextView mtvPrompt;
    // true --cancel transmit;false--transmit
    private boolean mIsTransmit;
    private static final int mCycleNum = 4;

    public RemoteControl(String text, Boolean visible) {
        super(text, visible);
    }

    @Override
    public View getView(Context context) {
        mView = LayoutInflater.from(context).inflate(
                R.layout.item_remote_control, null);
        mIda = new IrControl(context);
        mtvPrompt = (TextView) mView.findViewById(R.id.tv_remote_control);

        /** If objir is null. There is no function of remote control */
        if (IrControl.mObject == null) {
            mtvPrompt.setText(R.string.remote_tip);
            ItemTestActivity.itemActivity.handler
                    .sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_UNCLICKABLE);
        }

        /** set the acitivty title */
        ItemTestActivity.itemActivity.setTitle(R.string.item_remote_control);
        return mView;
    }

    int[][] data = new int[][] {
            { 38000, 342, 171, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22, 21, 22,
                    21, 22, 21, 22, 21, 64, 21, 64, 21, 64, 21, 64, 21, 64, 21,
                    64, 21, 22, 21, 64, 21, 22, 21, 22, 21, 22, 21, 22, 21, 64,
                    21, 22, 21, 22, 21, 22, 21, 64, 21, 64, 21, 64, 21, 64, 21,
                    22, 21, 64, 21, 64, 21, 64, 21, 1557 },
            { 38460, 114, 117, 19, 58, 20, 57, 19, 58, 19, 57, 20, 58, 20, 57,
                    20, 97, 19, 58, 19, 58, 19, 57, 21, 56, 20, 57, 20, 97, 19,
                    59, 19, 97, 19, 97, 20, 151, 19, 885 },
            { 38400, 34, 33, 35, 33, 68, 33, 35, 32, 35, 33, 35, 32, 35, 33,
                    35, 32, 35, 67, 35, 33, 68, 33, 35, 3565 },
            { 38400, 33, 30, 65, 30, 33, 31, 33, 30, 33, 31, 33, 30, 33, 31,
                    33, 30, 33, 63, 33, 31, 64, 31, 33, 3235, 33, 30, 33, 31,
                    64, 31, 33, 30, 33, 31, 33, 30, 33, 31, 33, 30, 33, 63, 33,
                    31, 64, 31, 33, 3235 },
            { 38000, 342, 171, 20, 63, 20, 20, 20, 20, 20, 63, 20, 63, 20, 20,
                    20, 20, 20, 20, 20, 63, 20, 20, 20, 20, 20, 63, 20, 20, 20,
                    20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
                    20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
                    63, 20, 20, 20, 63, 20, 20, 20, 20, 20, 63, 20, 20, 20,
                    760, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
                    20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 63,
                    20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
                    20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
                    20, 63, 20, 63, 20, 63, 20, 20 } };

    int[][] test = new int[][] {
            { 38000, 346, 170, 23, 19, 23, 20, 22, 62, 23, 19, 23, 19, 23, 20,
                    23, 19, 22, 20, 23, 61, 23, 62, 23, 19, 23, 62, 23, 62, 23,
                    62, 22, 62, 23, 62, 23, 62, 23, 19, 23, 19, 23, 19, 23, 62,
                    23, 19, 23, 19, 23, 19, 23, 19, 23, 62, 23, 62, 23, 62, 22,
                    20, 22, 62, 23, 62, 23, 62, 23, 1542 },
            { 38000, 346, 170, 23, 19, 23, 20, 22, 62, 23, 19, 23, 19, 23, 20,
                    22, 20, 23, 19, 23, 61, 23, 62, 23, 19, 23, 62, 23, 62, 23,
                    62, 22, 62, 23, 62, 23, 19, 23, 62, 23, 19, 23, 19, 23, 62,
                    23, 19, 23, 19, 23, 19, 23, 62, 23, 19, 23, 62, 23, 62, 22,
                    20, 22, 62, 23, 62, 23, 62, 23, 1542 },
            { 38000, 346, 170, 23, 19, 23, 20, 22, 62, 23, 19, 23, 19, 23, 20,
                    23, 19, 22, 20, 23, 61, 23, 62, 23, 19, 23, 62, 23, 62, 23,
                    62, 22, 62, 23, 62, 23, 19, 23, 19, 23, 62, 23, 19, 23, 62,
                    23, 19, 23, 19, 23, 19, 23, 62, 23, 62, 23, 19, 23, 62, 22,
                    20, 22, 62, 23, 62, 23, 62, 23, 1542 },
            { 38000, 346, 170, 23, 19, 23, 20, 22, 62, 23, 19, 23, 19, 23, 20,
                    22, 20, 22, 20, 23, 61, 23, 62, 23, 19, 23, 62, 23, 62, 23,
                    62, 23, 61, 23, 62, 23, 62, 23, 19, 23, 19, 23, 62, 23, 62,
                    22, 20, 23, 19, 23, 19, 23, 19, 23, 62, 23, 61, 23, 20, 22,
                    20, 22, 62, 23, 62, 23, 62, 23, 1542 } };

    @Override
    public void onResume() {
        super.onResume();
        mIsTransmit = false;

        /** Open the thread kept sending data */
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                while (!mIsTransmit) {
                    for (int j = 0; j < mCycleNum; j++) {
                        int[] frame = test[j];
                        mIda.sendIR(frame, false);
                    }
                }
                /*
                 * myCycle: while (true) { if (mIsTransmit) { break; } for (int
                 * j = 0; j < mCycleNum; j++) { if (mIsTransmit) { break
                 * myCycle; }
                 * 
                 * int[] frame = test[j]; mIda.sendIR(frame, false); } }
                 */
            }
        }).start();
    }

    /** On destroy Stop sending data */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsTransmit = true;
        mIda.stopIR();
    }

}
