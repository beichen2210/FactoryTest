package com.mlt.factorytest.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.mlt.factorytest.R;
import com.mlt.factorytest.item.thread.WifiThread;
import com.mlt.factorytest.item.tools.ListViewMethod;
import com.mlt.factorytest.item.tools.WifiAdmin;

/**
 * @ClassName: WIFI_BT_GPS
 * @Description: test wifi/BT/GPS and show the result of test
 * @Function: Wifi, bluetooth, GPS star search positioning.The list of the three
 *            test results are, shows the result is divided into two: ssid and
 *            strength.At the same time the wifi is sorted according to the
 *            signal strength size sequence
 * @author: peisaisai
 * @date: 2015-01-15 2:10:04 Copyright (c) 2015, Malata All Rights Reserved.
 */

/** 
* @ClassName: WifiBtGps 
* @Description: del for VFOZBENQ-92 20150910
* @author:   peisaisai
* @date:     2015.8.5 am10:09:59 
* @time: 10:09:59 
* Copyright (c) 2015,  Malata All Rights Reserved.
*/
/** 
* @ClassName: WifiBtGps 
* @Description: modify for MOPLUES-39
* @Function: Change  method of connection
* @author:   peisaisai
* @date:     20151130 pm3:38:26 
* @time: pm3:38:26 
* Copyright (c) 2015,  Malata All Rights Reserved.
*/
public class WifiBtGps extends AbsHardware {
    public static WifiBtGps mWifiBtGps;

    // Some of the arg0 handler message queue
    private final int MSG_UPDATE_INITFAIL_WIFI = 1;
    private final int MSG_UPDATE_INIT_WIFI = 2;
    private final int MSG_UPDATE_WIFI_FINISH = 3;
    private final int MSG_UPDATE_CONNECTED_WIFI = 4;
    private final int MSG_UPDATE_WIFI_LISTVIEW = 5;
    private final int MSG_UPDATE_WIFI_FAIL = 6;
    
    // One of the biggest time needed for each test
    private final int WIFITEST_MAX_TIME = 60;

    // The maximum number of search results
    private final int WIFI_MAX_NUM = 5;

    private final int WIFITEST_MAX_NUM = 2;
    public static int mWifiTestNum;

    private final String SHAREPREFERCES_NAME = "TestState";

    // Whether open threads connected wifi
    //private boolean mIsWifiThreadTested;

	// Whether close the WIFI connection thread
	//public boolean mIsWifiThreadExit;

	private Context mContext;

	// Out of the control switch threads
	private boolean mIsWifiExit;
	
	//A sign of wifi test is tested
	private boolean mIsWifiTested;
	
	public static boolean mIsWifiThreadExit;

    // the switch to control the threads of timer
    private boolean mIsWifiTimeThreadExit;

    // ID is textview display text
    private TextView mbtnWifiConnect;

    private WifiAdmin mWifiAdmin;
    private ListView  mlvWifi;
    private SimpleAdapter mAdapterWifi;
    private List<Map<String, Object>> mListWifi;
    public WifiBtGps(String text, Boolean visible) {
        super(text, visible);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mWifiBtGps = WifiBtGps.this;
        Log.i("pss", "onResume");
        if (!mIsWifiTested) {
            startWifiTest();
            mIsWifiTested = true;
        }
    }

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.i("pss", "onPause");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @Description:The data clean up and close the wifi, bt, GPS
	 * 
	 * @see com.malata.factorytest.item.AbsHardware#onDestory() Copyright (c)
	 * 2015, Malata All Rights Reserved.
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i("pss", "onDestroy");
		mWifiAdmin = new WifiAdmin(mContext);
		
		//pss modify for MOPLUES-44 20151217 start
		//before  mWifiAdmin.forget(mWifiAdmin.getNetworkId());
		if(mWifiAdmin.getNetworkId() > 0){
			mWifiAdmin.forget(mWifiAdmin.getNetworkId());
		}
		//pss modify for MOPLUES-44 20151217 end
		closeWifiBtGpsTest();
	}

	public void closeWifiBtGpsTest() {
		Log.i("pss", "colse start");
		mIsWifiExit = true;
		mIsWifiTested = true;
		mIsWifiTimeThreadExit = true;
		SharedPreferences sp = mContext.getSharedPreferences(
				SHAREPREFERCES_NAME, Context.MODE_PRIVATE);
		if (!sp.getBoolean("wifistate", false)) {
			mWifiAdmin.closeWifi();
		}else{
			mWifiAdmin.openWifi();
		}
		mListWifi.clear();
		Log.i("pss", "colse end");
	}

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Log.i("pss", "oncreate");
        mWifiTestNum = 0;
        startWifiBtGpsTest();
    }

    /**
     * @MethodName: startWifiBtGpsTest
     * @Description: start wifi-bt-gps Testing
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    public void startWifiBtGpsTest() {

        mWifiDiscoveredNum = 0;

        mIsWifiExit = false;
        mIsWifiTested = false;
        mIsWifiTimeThreadExit = false;
        mWifiAdmin = new WifiAdmin(mContext);

        // Used to save the current state of mobile test item
        SharedPreferences sp = mContext.getSharedPreferences(
                SHAREPREFERCES_NAME, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putBoolean("wifistate", mWifiAdmin.mWifiManager.isWifiEnabled());
        editor.commit();
    }

	//private WifiThread wifiThread;

    /**
     * @MethodName: startWifiThread
     * @Description:Start a thread of connecting wifi.
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    /*private void startWifiThread() {
        wifiThread = new WifiThread(mContext, handler, mWifiAdmin, mListWifi,
                mWifiTestNum);
        Thread td = new Thread(wifiThread);
        td.start();
    }*/

	private void closeWifiTest() {
		mlvWifi.setEnabled(false);
		mIsWifiTimeThreadExit = true;
		/*pss del for MOPLUES-44 20151217
		SharedPreferences sp = mContext.getSharedPreferences(
				SHAREPREFERCES_NAME, Context.MODE_PRIVATE);
		if (!sp.getBoolean("wifistate", false)) {
			mWifiAdmin.closeWifi();
		}*/
	}

	/**
	 * @MethodName: WIFI_Test
	 * @Description:WIFI on search, first of all determine whether WIFI open,
	 *                   then I will search more times WIFI, the final results
	 *                   from big to small of sorting methods are displayed in
	 *                   the interface.At this time also can open a thread, the
	 *                   current list when the parameter passed in, connect
	 *                   search to WIFI signal is the strongest one.
	 * @return void
	 * @throws Copyright
	 *             (c) 2015, Malata All Rights Reserved.
	 */
	private void startWifiTest() {
		// TODO Auto-generated method stub
		Log.i("pss", "wifiTest");
		mWifiAdmin.openWifi();
		Log.i("pss","MSG_UPDATE_INIT_WIFI  1");
		sendMsg(MSG_UPDATE_INIT_WIFI);
		mListWifi = new ArrayList<Map<String, Object>>();
//		mListGps = new ArrayList<Map<String, Object>>();
		final long preTime = System.currentTimeMillis();
		new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                long lasTime;
                while (true) {
                    if (unexpectedShutdown()) {
                        break;
                    }
                    lasTime = System.currentTimeMillis();
                    if (mIsWifiTimeThreadExit) {
                        break;
                    }
                    if (WIFITEST_MAX_TIME <= (lasTime - preTime) / 1000) {
                        //mIsWifiThreadExit = true;
                        mIsWifiExit = true;
                        if (mIsWifiTimeThreadExit) {
                            break;
                        }
                        if (mWifiAdmin.mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
                            sendMsg(MSG_UPDATE_INITFAIL_WIFI);
                        } else {
                            sendMsg(MSG_UPDATE_WIFI_FAIL);
                        }
                        break;
                    }
                }
            }
        }).start();
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (true) {
                    if (unexpectedShutdown()) {
                        break;
                    }
                    Log.i("pss", "mIsWifiExit = " + mIsWifiExit);
                    if (mIsWifiExit) {
                            sendMsg(MSG_UPDATE_CONNECTED_WIFI);
                            Log.i("pss", "sendMsg(MSG_UPDATE_CONNECTED_WIFI);");
                        break;
                    }
                    Log.i("pss", "" + mWifiAdmin.mWifiManager.getWifiState());
                    Log.i("pss", "" + WifiManager.WIFI_STATE_ENABLED);
                    if (mWifiAdmin.mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                        updateWIFIlist();
                        try {
                            Thread.sleep(1000); //pss modify for MOPLUES-44 20151217
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else {
                        Log.i("pss","MSG_UPDATE_INIT_WIFI  2");
                        sendMsg(MSG_UPDATE_INIT_WIFI);
                    }
                }
            }
        }).start();

	}

    /**
     * @MethodName: update_WIFIlist
     * @Description: update list_wifi
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void updateWIFIlist() {
        Log.i("pss", "updatewifilist");
        mWifiAdmin = new WifiAdmin(mContext);
        mWifiAdmin.startScan();
        List<ScanResult> listResults = mWifiAdmin.getWifiList();
        mListWifi.clear();
        Map<String, Object> map;
        //Log.i("pss", "" + listResults.size()); //pss del for 20151217
        if (listResults != null) {

            for (ScanResult scanResult : listResults) {

                // Filter out the WIFI password, keep no WIFI password
                if (mWifiAdmin.getSecurity(scanResult) != mWifiAdmin.SECURITY_NONE) {
                    continue;
                }
                map = new HashMap<String, Object>();
                map.put("ssid", scanResult.SSID);
                
                map.put("bssid", scanResult.BSSID);
                
                map.put("strength", "" + scanResult.level);
                if ((!map.get("ssid").toString().contains("NVRAM WARNING"))
                        && (mListWifi.size() < WIFI_MAX_NUM)) {
                    mListWifi.add(map);
                }
            }
            sendMsg(MSG_UPDATE_WIFI_LISTVIEW);
        } else {
            Log.i("pss", "list Results == null");
        }
    }

    // Found that the number of eligible WIFI
    private int mWifiDiscoveredNum;
    /**
     * @Fields: handler Technique of information update picture.
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.arg1) {
            case MSG_UPDATE_INIT_WIFI:
                mbtnWifiConnect.setText(mContext.getString(R.string.mtvinitwifi));
                // Log.i("pss", "receive a message:updateinit wifi");
                break;

            // update the connectting wifi info
            case MSG_UPDATE_INITFAIL_WIFI:
                mbtnWifiConnect.setText(mContext
                        .getString(R.string.mtvinitwififail));
                mbtnWifiConnect.setBackgroundColor(Color.RED);
                closeWifiTest();
                Log.i("pss", "receive a message: UPDATE_INITFAIL_WIFI");
                break;

            case MSG_UPDATE_CONNECTED_WIFI:
                mbtnWifiConnect.setText(mContext
                        .getString(R.string.mtvwificonnectting));
                //updateConnectWifi();

                mWifiAdmin = new WifiAdmin(mContext);
                Log.i("pss", "mWifiAdmin.getSpeed() > 0 :" + (mWifiAdmin.getSpeed() > 0));
                if (mWifiAdmin.getSpeed() > 0) {
                    sendMsg(MSG_UPDATE_WIFI_FINISH);
                }else if(mListWifi != null && mListWifi.size() > 0){ //pss add for MOPLUES-44 20151217
                    String ssid = mListWifi.get(0).get("ssid").toString();
                    String bssid = mListWifi.get(0).get("bssid").toString();
                    mWifiAdmin.connect(ssid, bssid, handler);
                }
                
                Log.i("pss", "receive a message:update connected wifi");
                break;

            // update the listview of wifi
            case MSG_UPDATE_WIFI_LISTVIEW:
                mbtnWifiConnect.setText(mContext
                        .getString(R.string.mtvwifiscanning));
                Collections.sort(mListWifi, new ListComparator());
                updateListview(mListWifi, mAdapterWifi, mlvWifi);
                mWifiDiscoveredNum++;
                if (mWifiDiscoveredNum > 3) {
                    mIsWifiExit = true;
                }
                break;

            // searing WIFI finished
            case MSG_UPDATE_WIFI_FINISH:
                mWifiAdmin = new WifiAdmin(mContext);
                mbtnWifiConnect.setText(mContext
                            .getString(R.string.mtvconcomplete));
                closeWifiTest();
                break;
            case MSG_UPDATE_WIFI_FAIL:
                mWifiTestNum++;
                if(mWifiTestNum != WIFITEST_MAX_NUM){
                    startWifiBtGpsTest();
                    startWifiTest();
                }else{
                    mbtnWifiConnect.setText(mContext
                            .getString(R.string.mtvconfail));
                    closeWifiTest();
                }
                break;
            default:
                break;
            }
        }

	};

    /**
     * @MethodName: updateListview
     * @Description:TODO
     * @param list
     * @param sAdapter
     * @param listview
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void updateListview(List<Map<String, Object>> list,
            SimpleAdapter sAdapter, ListView listview) {
        sAdapter = new SimpleAdapter(mContext, list,
                R.layout.item_bt_wifi_gps_list_item, new String[] { "ssid",
                        "strength" }, new int[] { R.id.mtvsatnum,
                        R.id.mtvsatstrength });
        listview.setAdapter(sAdapter);
        ListViewMethod.setListViewHeightBasedOnChildren(listview);
    }

	private void sendMsg(int msgArg1) {
		Message msg = new Message();
		msg.arg1 = msgArg1;
		handler.sendMessage(msg);
	}

	@Override
	public View getView(Context context) {
		// TODO Auto-generated method stub
		this.mContext = context;
		LayoutInflater factory = LayoutInflater.from(context);
		View view = factory.inflate(R.layout.item_bt_wifi_gps, null);
		initList();
		initModule(view);

		return view;
	}

	private void initList() {
		// TODO Auto-generated method stub
		mListWifi = new ArrayList<Map<String, Object>>();
	}

    private void initModule(View view) {
        // TODO Auto-generated method stub
        mbtnWifiConnect = (TextView) view.findViewById(R.id.mtvwificon);
        mlvWifi = (ListView) view.findViewById(R.id.mlswifi);
		//listOnItemLongclick();
	}

	/**
	 * @MethodName: listOnItemLongclick
	 * @Description:
	 * @return void
	 * @throws Copyright
	 *             (c) 2015, Malata All Rights Reserved.
	 */
	/*private void listOnItemLongclick() {
		// TODO Auto-generated method stub
		mlvWifi.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				final String ssid = mListWifi.get(arg2).get("ssid").toString();

				new AlertDialog.Builder(mContext)
						.setMessage("connect  " + ssid + "?")
						.setCancelable(false)
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated method stub
										WifiThread.mWifiSsid = ssid;
									}
								})
						.setNegativeButton("NO",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// TODO Auto-generated method stub
										arg0.cancel();
									}
								}).create().show();
			}
		});
	}*/

	/**
	 * @ClassName: ListComparator
	 * @Description: To sort the list
	 * @Function: According to the growing up of sequence order
	 * @author: peisaisai
	 * @date: 2015-01-15 14:38:11 Copyright (c) 2015, Malata All Rights
	 *        Reserved.
	 */
	private class ListComparator implements Comparator<Map<String, Object>> {

		@Override
		public int compare(Map<String, Object> map1, Map<String, Object> map2) {
			// TODO Auto-generated method stub

			return (Integer.parseInt(map2.get("strength").toString()))
					- (Integer.parseInt(map1.get("strength").toString()));

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
                return false;
            }
        } catch (NullPointerException e) {
        }
        onDestroy();
        return true;
    }
    

}
