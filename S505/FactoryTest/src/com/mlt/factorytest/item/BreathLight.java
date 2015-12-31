package com.mlt.factorytest.item;

import com.mlt.factorytest.R;

import android.os.IBreathLedsService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class BreathLight extends AbsHardware {
    private Context mContext;
    private IBreathLedsService mILS;
    private int mCount;

    public BreathLight(String text, Boolean visible) {
        super(text, visible);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mCount = 0;
        mILS = null;

        // Access to the breathing light services
        if (mILS == null) {
            mILS = IBreathLedsService.Stub.asInterface(ServiceManager
                    .getService("breath_leds"));
        }
        closeAllBreathLeds();
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (mCount < 3) {
                    try {
                        // TODO Auto-generated method stub
                        mILS.turnOnLeds(mCount);
                        // iLS.ledBreath(count);
                        mILS.ledTwinkle(mCount);
                    } catch (RemoteException e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                    mCount++;
                    try {
                        Thread.sleep(300);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        closeAllBreathLeds();
    }

    /**
     * @MethodName: closeAllBreathLeds
     * @Description:Close all breathing lamp
     * @return void
     * @throws Copyright
     *             (c) 2015, mlt All Rights Reserved.
     */
    private void closeAllBreathLeds() {
        try {
            // Log.i("pss", "closeAllBreathLed");
            // System.out.println("closeAllBreathLed");
            if (mILS != null) {
                // Log.i("pss", "closeAllBreathLed no null");
                System.out.println("closeAllBreathLed no null");
                mILS.turnOffLeds(0);
                mILS.turnOffLeds(1);
                mILS.turnOffLeds(2);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView(Context context) {
        // TODO Auto-generated method stub
        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.item_breath_light, null);
        return view;
    }

}
