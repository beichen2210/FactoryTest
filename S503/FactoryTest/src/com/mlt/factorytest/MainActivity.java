package com.mlt.factorytest;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * 
 * file name:MainActivity.java
 * Copyright MALATA ,ALL rights reserved
 * 2015-1-26
 * author:laiyang
 * Modification history
 * -------------------------------------
 * 
 * -------------------------------------
 */
public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	/**
	 * This method is buttons call back method,
	 * when click buttons,the method will be called,
	 * and switch ID to start different activity.
	 * @date 2015-1-26 pm 3:08:45
	 * @param v click button's view
	 */
	public void onClick(View v) {
		Intent startIntent = null;
		switch(v.getId()) {
		/** 1.ID bt_auto_test:start AutoTestActivity */
		case R.id.bt_auto_test:
			startIntent = new Intent(this, AutoTestActivity.class);
			break;
		/** 2.ID bt_manual_test:start ManualTestActivity */
		case R.id.bt_manual_test:
			startIntent = new Intent(this, ManualTestActivity.class);
			break;
		/** 3.ID bt_test_report:start TestReportActivity */	
		case R.id.bt_test_report:
			startIntent = new Intent(this, TestReportActivity.class);
			break;
		/** 4.ID bt_reboot:start RebootActivity */	
		case R.id.bt_reboot:
			startIntent = new Intent(this, RebootActivity.class);
			break;
			
		default: 
			break;
		}
		
		if(null == startIntent) {
			return;
		}
		
		//start activity
		startActivity(startIntent);
	}
	
}
