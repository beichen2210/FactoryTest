package com.mlt.factorytest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.mlt.factorytest.item.AbsHardware.TestResult;
import com.mlt.factorytest.item.MyApplication;
import com.mlt.factorytest.item.tools.SaveStatusTool;
/**  
 *  
 * file name:AutoTestActivity.java
 * Copyright MALATA ,ALL rights reserved.
 * 2015-1-26  
 * author:laiyang    
 * 
 * The class is to Auto test all items, when finish one test, step to next test.
 * You can click back to pause and exit AutoTest.
 * 
 * Modification history
 * -------------------------------------
 * 
 * -------------------------------------
 */ 
public class AutoTestActivity extends Activity {
	
	private static final String TAG = "AutoTestActivity";
	/**
	 * string of current test position
	 */
	private final String CURRENT_POSITION = "position";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(null != savedInstanceState) {
			MyApplication.setCurrentAutoPos(savedInstanceState.getInt(CURRENT_POSITION, 0));
		} else {
			MyApplication.setCurrentAutoPos(0);
		}
		MyApplication.clearResult();
	}
	
	@Override
	protected void onResume() {
		// AutoTest finish!
		if(MyApplication.getCurrentAutoPos() == MyApplication.getResults().size()) {
			Toast.makeText(this, getResources().
					getString(R.string.auto_test_finish), Toast.LENGTH_LONG).show();
			finish();
			super.onResume();
			return;
		}	
		// pause and exit AutoTest when user click back key
		if(MyApplication.getCurrentAutoPos() > 0 && TestResult.UnCheck 
				== MyApplication.getResults().get(MyApplication.getCurrentAutoPos() - 1)) {
			finish();
			super.onResume();
			return;
		}
		// step to next Test
		// set Intent and currentAutoPos++
		Intent i = new Intent(this, ItemTestActivity.class);
		i.putExtra("position", MyApplication.getCurrentAutoPos());
		startActivity(i);
		MyApplication.setCurrentAutoPos(MyApplication.getCurrentAutoPos() + 1);
		
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		Log.i(TAG, "is saving auto status");
		// when leave this activity,save current test result in SharedPreference
		SaveStatusTool.saveTestResults(this);
		super.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// when the activity being killed, save current test position
		outState.putInt(CURRENT_POSITION, MyApplication.getCurrentAutoPos());
		super.onSaveInstanceState(outState);
	}
}
