package com.mlt.factorytest;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;

import com.mlt.factorytest.adapter.TestItemAdapter;
import com.mlt.factorytest.item.MyApplication;
import com.mlt.factorytest.item.tools.SaveStatusTool;
/**
 * 
 * file name:ManualTestActivity.java
 * Copyright MALATA ,ALL rights reserved
 * 
 * This class is an activity,show all test items view,
 * and you can click one item to start test,when test finish,
 * you will back to this activity,the result will be show.
 * 
 * 2015-1-26
 * author:laiyang   
 * Modification history
 * -------------------------------------
 * 
 * -------------------------------------
 */
public class ManualTestActivity extends Activity {
	// log's tag
	private static final String TAG = "MaunalTestAcitivty";
	// GridView is to show all test items
	private GridView mGridView;
	// GridView's adapter
	private TestItemAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manual_test);
		
		MyApplication.clearResult();
		// init view and set Adapter
		mGridView = (GridView)findViewById(R.id.girdView);
		mAdapter = new TestItemAdapter(this);
		mGridView.setAdapter(mAdapter);
	}
	
	@Override
	protected void onResume() {
		TestItemAdapter.updateString();
		// update GridView when current activity will be showed 
		mGridView.invalidateViews();
		super.onResume();
	}
	
	
	@Override
	protected void onStop() {
		// when leave this activity,save current test result in SharedPreference
		Log.i(TAG, "is saving manual status");
		SaveStatusTool.saveTestResults(this);
		super.onStop();
	}
}
