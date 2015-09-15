package com.mlt.factorytest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

import com.mlt.factorytest.adapter.TestItemAdapter;
import com.mlt.factorytest.item.tools.SaveStatusTool;
/**
 *  
 * file name:TestReportActivity.java <p>
 * Copyright MALATA ,ALL rights reserved <p>
 * 2015-1-26<p>
 * author:laiyang<p>
 * 
 * This activity is to show AutoTest's results
 */
public class TestReportActivity extends Activity {

	/**
	 * list all of test items
	 */
	private GridView mGridView;
	/**
	 * the adapter of gridView
	 */
	private TestItemAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_report);
		setTitle(getString(R.string.bt_test_report));
		mGridView = (GridView) findViewById(R.id.gv_report);
		
		// load saved test status
		initStatus();
		mAdapter = new TestItemAdapter(this);
		mGridView.setAdapter(mAdapter);
	}
	/**
	 * read sharedPreferences to get test status
	 * @date 2015-1-31 am 10:56:19
	 */
	private void initStatus() {
		SaveStatusTool.readResults(this);
	}
	/**
	 * when back to this activity, redraw gridview
	 * (non-Javadoc)
	 * @date 2015-1-31 am 10:56:19
	 */
	@Override
	protected void onResume() {
		// update GridView when current activity will be showed 
		mGridView.invalidateViews();
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		// when leave this activity,save test results
		SaveStatusTool.saveTestResults(this);
		super.onStop();
	}
}
