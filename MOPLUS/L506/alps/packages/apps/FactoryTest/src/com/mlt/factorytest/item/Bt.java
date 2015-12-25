package com.mlt.factorytest.item;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.bool;
import android.R.color;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.GpsSatellite;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.mlt.factorytest.R;
import com.mlt.factorytest.ItemTestActivity;
import com.mlt.factorytest.item.thread.WifiThread;
import com.mlt.factorytest.item.tools.BTAdmin;
import com.mlt.factorytest.item.tools.GPSAdmin;
import com.mlt.factorytest.item.tools.ListViewMethod;
import com.mlt.factorytest.item.tools.WifiAdmin;

import android.content.ComponentName;

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
* @ClassName: Bt 
* @Description: del for VFOZBENQ-92 20150910
* @author:   peisaisai
* @date:     2015.10.16 
* @time: 10:09:59 
* Copyright (c) 2015,  Malata All Rights Reserved.
*/
public class Bt extends AbsHardware {
    
	private Context mContext;
	private BTAdmin mBtAdmin;
	
	private boolean mboolBt;
	
	private final String SHAREPREFERCES_NAME = "TestState";
    public Bt(String text, Boolean visible) {
        super(text, visible);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
		if(!mboolBt){
			mboolBt = true;
			Intent mIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);    
			ItemTestActivity.itemActivity.startActivity(mIntent); 
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
		closeWifiBtGpsTest();
		
	}

	public void closeWifiBtGpsTest() {
		closeBtTest();
	}

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
		startWifiBtGpsTest();
		startBtTest();
    }
	
    /**
     * @MethodName: startWifiBtGpsTest
     * @Description: start wifi-bt-gps Testing
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    public void startWifiBtGpsTest() {
		mboolBt = false;
        mBtAdmin = new BTAdmin(mContext);
        // Used to save the current state of mobile test item
        SharedPreferences sp = mContext.getSharedPreferences(
                SHAREPREFERCES_NAME, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        Log.i("pss", "mBtAdmin:" + mBtAdmin);
        editor.putInt("btstate", mBtAdmin.mAdapter.getState());
        editor.commit();
    }

	/** 
	* @MethodName: closeBtTest 
	* @Description:After the BT test, have to do operation  
	* @return void   
	* @throws 
	* Copyright (c) 2015,  Malata All Rights Reserved.
	*/
	private void closeBtTest() {
		SharedPreferences sp1 = mContext.getSharedPreferences(
				SHAREPREFERCES_NAME, Context.MODE_PRIVATE);

		if (sp1.getInt("btstate", 0) == BluetoothAdapter.STATE_OFF) {
			mBtAdmin.closeBT();
		}

		if (mBtAdmin.mAdapter.isDiscovering()) {
			mBtAdmin.mAdapter.cancelDiscovery();
		}
	}

	/**
	 * @MethodName: BT_Test
	 * @Description:Bluetooth test, first of all empty list, whether the
	 *                        bluetooth open, then to search in the search
	 *                        process, will not be repeated search
	 * @return void
	 * @throws Copyright
	 *             (c) 2015, Malata All Rights Reserved.
	 */
	private void startBtTest() {
		// TODO Auto-generated method stub
		mBtAdmin.openBT();
    }

	@Override
	public View getView(Context context) {
		// TODO Auto-generated method stub
		this.mContext = context;
		LayoutInflater factory = LayoutInflater.from(context);
		View view = factory.inflate(com.mlt.factorytest.R.layout.item_bt, null);

		return view;
	}
}
