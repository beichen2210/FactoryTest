package com.mlt.factorytest.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;

import com.mlt.factorytest.ItemTestActivity;
import com.mlt.factorytest.R;

/** 
* @ClassName: FingerprintTest 
* @PackageName:com.mlt.factorytest.item
* @author:   chehongbin
* @date:     2015-11-5 下午2:07:52  
* Copyright (c) 2015 MALATA,All Rights Reserved.
*/
public class FingerprintTest extends AbsHardware {
	private View view;
	private Context mContext;
	private TextView mFingerPrintAvailable;
	private static final String FACTORYTEST_FINGERPRINT_TEST = "com.android.factorytest.fingerprinttest";
	
	public FingerprintTest(String text, Boolean visible) {
		super(text, visible);
	}

	@Override
	public View getView(Context context) {
		this.mContext = context;//chb add for VFOZESGW-81 at 20151023
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		view = layoutInflater.inflate(R.layout.item_fingerprint_test, null);
		mFingerPrintAvailable = (TextView)view.findViewById(R.id.tv_fingerprint_exist);

		return view;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_UNCLICKABLE);
	}

	@Override
	public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FACTORYTEST_FINGERPRINT_TEST);
        mContext.registerReceiver(mFingerPrintTestBroadcastReceiver, intentFilter);
	}
	
	//chb add for 
	public BroadcastReceiver mFingerPrintTestBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context mContext, Intent mIntent) {
            String action = mIntent.getAction();
            if (action.equals(FACTORYTEST_FINGERPRINT_TEST)) {
            	mFingerPrintAvailable.setTextColor(Color.GREEN);
            	mFingerPrintAvailable.setVisibility(view.VISIBLE);
            	ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_CLICKABLE);
            }else {
            	mFingerPrintAvailable.setText(R.string.tv_fingerprint_unavailable);
            	mFingerPrintAvailable.setTextColor(Color.RED);
            	mFingerPrintAvailable.setVisibility(view.VISIBLE);
			}
        }
    };
    
	@Override
	public void onPause() {
		super.onPause();
		 mContext.unregisterReceiver(mFingerPrintTestBroadcastReceiver);
	}
	
	public void onDestroy() {
		super.onDestroy();
	};
	
	
}
