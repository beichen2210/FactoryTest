package com.mlt.factorytest.item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mlt.factorytest.R;
import com.mlt.factorytest.item.tools.GPSAdmin;
import com.mlt.factorytest.item.tools.ListViewMethod;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.GpsSatellite;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/** 
* @ClassName: Gps 
* @Description: modify for LFZS-136
* @Function: GPS search star and positioning
* @author:   peisaisai
* @date:     20150805 pm4:04:22 
* @time: pm4:04:22 
* Copyright (c) 2015,  Malata All Rights Reserved.
*/
public class Gps extends AbsHardware {
    
    private Context mContext;
    private GPSAdmin mGpsAdmin;
    private ListView mlvGps;
    private SimpleAdapter mAdapterGps;
    private List<Map<String, Object>> mListGps;
    private long mGpsPosTime;
    private TextView mbtnGpsConnect,mtvPosTime;
    
    private boolean mboolGpsTestExit;
    private boolean mIsGpsExit;
    private boolean mIsGpsTimeThreadExit;
    
    //gps handler
    private final int MSG_UPDATE_INIT_GPS = 13;
    private final int MSG_UPDATE_INITFAIL_GPS = 14;
    private final int MSG_UPDATE_GPS_LISTVIEW = 15;
    private final int MSG_UPDATE_GPS_TIMEFINISH = 16;
    private final int MSG_UPDATE_GPS_LOCATIONFINISH = 17;
    private final int MSG_UPDATE_GPS_SEARCHING = 18;
    private final int MSG_UPDATE_GPS_TIME = 19;
    
    //the max num of satelites
    private final int GPS_MAX_NUM = 3;
    
    private final int GPSTEST_MAX_TIME = 300;
    
    private final String SHAREPREFERCES_NAME = "GPSTestState";
    public Gps(String text, Boolean visible) {
        super(text, visible);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        initData();
        startGpsTest();
    }
    private void initData() {
        // TODO Auto-generated method stub
        mboolGpsTestExit = false;
        mIsGpsExit = false;
        mIsGpsTimeThreadExit = false;
        mGpsPosTime = 0;
        mGpsAdmin = new GPSAdmin(mContext, mHandler);
     // Used to save the current state of mobile test item
        SharedPreferences sp = mContext.getSharedPreferences(
                SHAREPREFERCES_NAME, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putBoolean("gpsstate", Settings.Secure
                .isLocationProviderEnabled(mContext.getContentResolver(),
                        LocationManager.GPS_PROVIDER));
        editor.commit();
    }
    

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        mboolGpsTestExit = true;
        mIsGpsExit = true;
        mIsGpsTimeThreadExit = true;
        mListGps.clear();
        mGpsAdmin.clearSateliteList();
        super.onDestroy();
    }
    
    
  //The number of save the satellite
    private int mSateliteNum;

    /**
     * @MethodName: updateGpsList
     * @Description:GPS test, each time the search to the satellite, and the
     *                  list, convenient updating data, when the search to the
     *                  satellite is greater than or equal to 4, it will send
     *                  search success message
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void updateGpsList() {
        // TODO Auto-generated method stub
        List<GpsSatellite> numSatelliteList = mGpsAdmin.getSateliteList();
        mSateliteNum = numSatelliteList.size();
        if (!mIsGpsExit) {
            Map<String, Object> map;
            mListGps.clear();
            for (GpsSatellite s : numSatelliteList) {
                map = new HashMap<String, Object>();
                map.put("ssid", s.getPrn());
                map.put("strength", s.getSnr());
                mListGps.add(map);
                if (mListGps.size() > GPS_MAX_NUM) {
                  //sendMsg(MSG_UPDATE_GPS_LOCATIONFINISH); pss del for VFOZBENQ-95 20150911
                    break;
                }
            }
        }
    }
    
    /**
     * @MethodName: closeGpsTest
     * @Description:After the GPS test, have to do operation
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void closeGpsTest() {
        mIsGpsTimeThreadExit = true;
        mIsGpsExit = true;
        SharedPreferences sp2 = mContext.getSharedPreferences(
                SHAREPREFERCES_NAME, Context.MODE_PRIVATE);
        if (!sp2.getBoolean("gpsstate", false)) {
            mGpsAdmin.closeGps();
        }
    }

    /**
     * @MethodName: startGpsTest
     * @Description:Start to do the GPS test timer thread
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void startGpsTest() {
        mGpsAdmin.openGps();
        sendMsg(MSG_UPDATE_INIT_GPS);
        final long preTime = System.currentTimeMillis();
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                long lasTime;
                int count = 0;
                while (true) {
                    if (unexpectedShutdown()) {
                        break;
                    }
                    boolean gpsEnabled = Settings.Secure
                            .isLocationProviderEnabled(
                                    mContext.getContentResolver(),
                                    LocationManager.GPS_PROVIDER);
                    if (gpsEnabled && !mIsGpsExit) {
                        sendMsg(MSG_UPDATE_GPS_SEARCHING);
                    }
                    lasTime = System.currentTimeMillis();
                    mGpsPosTime = lasTime - preTime;
                    if (GPSTEST_MAX_TIME <= (lasTime - preTime) / 1000) {
                        if (!gpsEnabled) {
                            sendMsg(MSG_UPDATE_INITFAIL_GPS);
                        } else {
                            sendMsg(MSG_UPDATE_GPS_TIMEFINISH);
                        }
                        mIsGpsTimeThreadExit = true;
                        break;
                    } else {
                        Log.i("pss", "lastTime = " + lasTime + "/npreTime = "
                                + preTime + "/nmGpsPosTime = " + mGpsPosTime
                                + "/ncount = " + count);
                        if ((lasTime - preTime) / 1000 >= count) {
                            count++;
                            sendMsg(MSG_UPDATE_GPS_TIME);
                        }
                    }
                    if (mIsGpsTimeThreadExit) {
                        break;
                    }
                    // sendMsg(UPDATE_GPS_SEARCHING);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
            case MSG_UPDATE_INIT_GPS:
                Log.i("pss", "MSG_UPDATE_INIT_GPS start");
                mbtnGpsConnect.setText(mContext.getString(R.string.mtvinitgps));
                Log.i("pss", "MSG_UPDATE_INIT_GPS end");
                break;
            case MSG_UPDATE_INITFAIL_GPS:
                mbtnGpsConnect
                        .setText(mContext.getString(R.string.mtvinitgpsfail));
                mbtnGpsConnect.setBackgroundColor(Color.RED);
                closeGpsTest();
                break;
            case MSG_UPDATE_GPS_SEARCHING:
                Log.i("pss", "MSG_UPDATE_GPS_SEARCHING start");
                mbtnGpsConnect
                        .setText(mContext.getString(R.string.mtvgpsscanning));
                Log.i("pss", "MSG_UPDATE_GPS_SEARCHING end");
                break;
                
            // update the listview of gps
            case MSG_UPDATE_GPS_LISTVIEW:
                Log.i("pss", "update_gps_list");
                updateGpsList();
                updateListview(mListGps, mAdapterGps, mlvGps);
                break;

            case MSG_UPDATE_GPS_LOCATIONFINISH:
                Log.i("pss", "update_gps_location_finish");
                mbtnGpsConnect.setText(mContext
                        .getString(R.string.mtvgpssearsuccess) + mSateliteNum);
                mtvPosTime
                        .setText(mContext.getString(R.string.mtvpostime)
                                + (mGpsPosTime / 1000)
                                + mContext.getString(R.string.mtvs));
                closeGpsTest();
                break;

            // gps search success
            case MSG_UPDATE_GPS_TIMEFINISH:
                Log.i("pss", "update gps timefinish start");
                mbtnGpsConnect.setText(mContext
                        .getString(R.string.mtvgpssearfail));
                sendMsg(MSG_UPDATE_GPS_TIME);
                closeGpsTest();
                Log.i("pss", "update gps timefinish end");
                break;
            case MSG_UPDATE_GPS_TIME:
                Log.i("pss", "update gps time start");
                mtvPosTime
                        .setText(mContext.getString(R.string.mtvpostime)
                                + (mGpsPosTime / 1000)
                                + mContext.getString(R.string.mtvs));
                Log.i("pss", "update gps time end");
                break;

            default:
                break;
            }
        };
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
        Log.i("pss", "updateListview start");
        sAdapter = new SimpleAdapter(mContext, list,
                R.layout.item_bt_wifi_gps_list_item, new String[] { "ssid",
                        "strength" }, new int[] { R.id.mtvsatnum,
                        R.id.mtvsatstrength });
        listview.setAdapter(sAdapter);
        ListViewMethod.setListViewHeightBasedOnChildren(listview);
        Log.i("pss", "updateListview end");
    }
    
    private void sendMsg(int msgArg1) {
        Message msg = new Message();
        msg.arg1 = msgArg1;
        mHandler.sendMessage(msg);
    }
    
    @Override
    public View getView(Context context) {
        // TODO Auto-generated method stub
        this.mContext = context;
        LayoutInflater factory = LayoutInflater.from(context);
        
        View view = factory.inflate(R.layout.item_gps, null);
        
        mListGps = new ArrayList<Map<String, Object>>();
        mbtnGpsConnect = (TextView) view.findViewById(R.id.mtvgpscon1);
        mlvGps = (ListView) view.findViewById(R.id.mlsgps1);
        mtvPosTime = (TextView) view.findViewById(R.id.mtvpostime1);
        return view;
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
         //Log.i("pss", ""+activityManager.getRunningTasks(1).get(0).topActivity
         //        .getClassName());
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
