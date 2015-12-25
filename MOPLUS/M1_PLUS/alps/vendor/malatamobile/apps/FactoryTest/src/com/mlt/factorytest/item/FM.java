package com.mlt.factorytest.item;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.mlt.factorytest.R;
import com.mlt.factorytest.ItemTestActivity;

import android.content.ComponentName;

/**
 * @ClassName: WIFI_BT_GPS
 * @Description: go to FMTest APK
 * @Function: test FM interface
 * @author: huanguoxiong
 * @date: 2015-12-17 2:10:04 Copyright (c) 2015, Malata All Rights Reserved.
 */
 
public class FM extends AbsHardware {
    
	private Context mContext;
    private static final String FMTESTPACKAGE="com.mlt.fmtest";
	private static final String FMMAINACTIVITY="mlt.fmtest";
    public FM(String text, Boolean visible) {
        super(text, visible);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();		
    }

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();		
	}

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    public boolean isFMTestExist(String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = mContext.getPackageManager().getApplicationInfo(
                    packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

	@Override
	public View getView(Context context) {
		// TODO Auto-generated method stub
		this.mContext = context;
		LayoutInflater factory = LayoutInflater.from(context);
		View view = factory.inflate(R.layout.item_fm, null);
        
		if(isFMTestExist(FMTESTPACKAGE)){
        Intent mIntent = new Intent(FMMAINACTIVITY);    
	    ItemTestActivity.itemActivity.startActivity(mIntent);
		}else{
        Toast.makeText(mContext,"FMTest is not exist !",Toast.LENGTH_SHORT).show();
		}
		
		return view;
	}
}
