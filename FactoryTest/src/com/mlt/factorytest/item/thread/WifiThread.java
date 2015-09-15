package com.mlt.factorytest.item.thread;

import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mlt.factorytest.ItemTestActivity;
import com.mlt.factorytest.item.WifiBtGps;
import com.mlt.factorytest.item.tools.WifiAdmin;

/**
 * @ClassName: WifiThread
 * @Description: connect the strongest signal of WIFI
 * @Function: TODO ADD FUNCTION
 * @author: peisaisai
 * @date: 2015-01-15 14:46:39 Copyright (c) 2015, Malata All Rights Reserved.
 */
public class WifiThread implements Runnable {
    private Context mContext;
    private Handler mHandler;
    private WifiAdmin mWifiAdmin;
    private int mWifiTestNum;
    public static String mWifiSsid = "";
    private final int MSG_UPDATE_CONNECTED_WIFI = 4;
    private final int MSG_UPDATE_CONNECTED1_WIFI = 20;
    private List<Map<String, Object>> mListWifi;

    public WifiThread(Context mContext, Handler mHandler, WifiAdmin mWifiAdmin,
            List<Map<String, Object>> mListWifi, int mWifiTestNum) {
        this.mContext = mContext;
        this.mHandler = mHandler;
        this.mWifiAdmin = mWifiAdmin;
        this.mListWifi = mListWifi;
        this.mWifiTestNum = mWifiTestNum;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        int index = 0;
        try {
            mWifiSsid = mListWifi.get(index).get("ssid").toString();
        } catch (Exception e) {
        }

        while (true) {
            if (unexpectedShutdown()) {
                break;
            }
            if (mWifiTestNum != WifiBtGps.mWifiTestNum) {
                break;
            }
            if (WifiBtGps.mWifiBtGps.mIsWifiThreadExit) {
                break;
            }

			// To refresh the WIFI information, judge the current connection
			// WIFI to connect WIFI is the same with you, if not the same, will
			// disconnect the current connection, then create a new WIFI to
			// connect to
			mWifiAdmin = new WifiAdmin(mContext);
			if (mWifiAdmin.mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
				if (!mWifiAdmin.getSSID()
						.substring(1, mWifiAdmin.getSSID().length() - 1)
						.equals(mWifiSsid)) {

                    mWifiAdmin.addNetwork(mWifiAdmin.createWifiInfo(mWifiSsid,
                            "", 2));
                } else {
                    Message msg = new Message();
                    if (0 == mWifiTestNum) {
                        msg.arg1 = MSG_UPDATE_CONNECTED_WIFI;
                    } else {
                        msg.arg1 = MSG_UPDATE_CONNECTED1_WIFI;
                    }
                    if (isWifiConnected(mContext)) {
                        mHandler.sendMessage(msg);
                        WifiBtGps.mWifiBtGps.mIsWifiThreadExit = true;
                        break;
                    }
                    
                }
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

		}

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
     
     public boolean isWifiConnected(Context context) {  
         if (context != null) {  
             ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
                     .getSystemService(Context.CONNECTIVITY_SERVICE);  
             NetworkInfo mWiFiNetworkInfo = mConnectivityManager  
                     .getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
             if (mWiFiNetworkInfo != null) {  
                 return mWiFiNetworkInfo.isAvailable();  
             }  
         }  
         return false;  
     }
}
