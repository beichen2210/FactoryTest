package com.mlt.factorytest.item;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
/**
 * 
 * file name:AbsHardware.java
 * Copyright MALATA ,ALL rights reserved
 * 2015-1-31
 * author:laiyang
 * Modification history
 * -------------------------------------
 *   
 * -------------------------------------
 */
public abstract class AbsHardware {
	
	/**
	 * TestResult : Pass,Fail,UnCheck
	 * @author laiyang 
	 *
	 */
	public enum TestResult {Pass,Fail,UnCheck};
	/**
	 * Test Item's name 
	 */
	public String text;
	
	private boolean visible;
	/**
	 * Application Context
	 */
	private Context context;
	/**
	 * the position of test list
	 */
	private int position;
	/**
	 * the container activity
	 */
	private Activity containerActivity;
	/**
	 * get the container activity
	 * @date 2015-4-11 pm 3:09:45
	 * @return
	 */
	public Activity getActivity() {
		return containerActivity;
	}
	/**
	 * set container activity
	 * @date 2015-4-11 pm 3:11:30
	 * @param activity
	 */
	public void setActivity(Activity activity) {
		this.containerActivity = activity;
		context = activity;
	}
	/**
	 * get the test position in list
	 * @date 2015-4-11 pm 2:59:47
	 * @return
	 */
	public int getPosition() {
		return position;
	}
	/**
	 * set the test position in list
	 * @date 2015-4-11 pm 3:00:34
	 * @param mPosition
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * save the testResult
	 */
	private TestResult result = TestResult.UnCheck;
	/**
	 * get test result
	 * @return
	 */
	public TestResult getResult() {
		return result;
	}
	/**
	 * set test result
	 * @param result
	 */
	public void setResult(TestResult result) {
		MyApplication.getResults().set(position, result);
		this.result = result;
	}
	/** 
	 * the constructor of AbsHardware
	 * @param text item's name 
	 * @param visible the item's visiblity
	 */
	public AbsHardware(String text, Boolean visible) {
		this.text = text;
		this.visible = visible;
	}
	  
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public abstract View getView(Context context);
	/**
	 * 
	 * @return
	 */
	public Context getContext() {
		return context;
	}
	
	public void onStart() {
		
	}
	
	public void onPause() {
		
	}
	
	public void onResume() {
		
	}
	
	public void onStop() {
		
	}
	
	public void onDestroy() {
		
	}
	
	public void onCreate() {
		
	}
	/**
	 * default return false <p> if want to stop KeyEvent send,return true
	 * @param keyCode
	 * @param event
	 * @return
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		return false;
	}
	
	public void onNewIntent(Intent intent) {
		
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	}
	public void onWindowFocusChanged(boolean hasFocus) {
		
	}
	
	public void onBackPressed() {
		
	}
}
