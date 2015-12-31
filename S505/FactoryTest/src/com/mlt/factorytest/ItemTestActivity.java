package com.mlt.factorytest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.mlt.factorytest.item.AbsHardware;
import com.mlt.factorytest.item.AbsHardware.TestResult;
import com.mlt.factorytest.item.MyApplication;
import com.mlt.factorytest.item.tools.TestItemTool;
/**
 * 
 * file name:ItemTestActivity.java
 * Copyright MALATA ,ALL rights reserved
 * 2015-1-26 
 * author:laiyang
 * Modification history
 * -------------------------------------
 * 
 * -------------------------------------
 */
public class ItemTestActivity extends Activity {
	// AbsHardware object
	private AbsHardware mHW;
	
	private static final String TAG = "ItemTestActivity";
	
	private RelativeLayout mContentView;
	// this class' object
	public static ItemTestActivity itemActivity;
	// pass button's ID
	public static final int BUTTON_PASS = R.id.btn_pass;
	// fail button's ID
	public static final int BUTTON_FAIL = R.id.btn_fail;
	
	//messages to control button or buttonBar visible/clickable
	public static final int MSG_BTN_PASS_CLICKABLE = 0x100010;
	
	public static final int MSG_BTN_PASS_UNCLICKABLE = 0x100011;
	
	public static final int MSG_BTN_FAIL_CLICKABLE = 0x100020;
	
	public static final int MSG_BTN_FAIL_UNCLICKABLE = 0x100021;
	
	public static final int MSG_BTNBAR_VISIBLE = 0x100030;
	
	public static final int MSG_BTNBAR_INVISIBLE = 0x100031;
	// Button fail:click this button,test fail and this activity finished
	private Button mbtnFail;
	// Button pass:click this button,test pass and this activity finished
	private Button mbtnPass;
	/**
	 * Handler: accept messages to control view and activity
	 */
	public Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch(msg.what) {
			/** 1.MSG_BTN_FAIL_CLIKABLE:set fail-button clickable */
			case MSG_BTN_FAIL_CLICKABLE:
				if(mbtnFail == null) {
					mbtnFail = (Button) findViewById(BUTTON_PASS);
				}
				mbtnFail.setBackgroundResource(R.drawable.selector_button);
				setButtonAvailable(BUTTON_FAIL, true);
				break;
			
			/** 2.MSG_BTN_FAIL_UNCLICKABLE: set fail_button unclickable */
			case MSG_BTN_FAIL_UNCLICKABLE:
				if(mbtnFail == null) {
					mbtnFail = (Button) findViewById(BUTTON_PASS);
				}
				mbtnFail.setBackgroundResource(R.drawable.selector_button_unable);
				setButtonAvailable(BUTTON_FAIL, false);
				break;
			
			/** 3.MSG_BTN_PASS_CLCKABLE: set pass-btn clickable */
			case MSG_BTN_PASS_CLICKABLE:
				if(mbtnPass == null) {
					mbtnPass = (Button) findViewById(BUTTON_PASS);
				}
				mbtnPass.setBackgroundResource(R.drawable.selector_button);
				setButtonAvailable(BUTTON_PASS, true);
				break;
			
			/** 4.MSG_BTN_PASS_CLCKABLE: set pass-btn unclickable */
			case MSG_BTN_PASS_UNCLICKABLE:
				if(mbtnPass == null) {
					mbtnPass = (Button) findViewById(BUTTON_PASS);
				}
				mbtnPass.setBackgroundResource(R.drawable.selector_button_unable);
				setButtonAvailable(BUTTON_PASS, false);
				break;
			
			/** 5.MSG_BTNBAR_VISIBLE: set button bar visible */
			case MSG_BTNBAR_VISIBLE:
				setButtomBarVisiblity(true);
				break;
				
			/** 6.MSG_BTNBAR_INVISIBLE: set button bar invisible */
			case MSG_BTNBAR_INVISIBLE:
				setButtomBarVisiblity(false);
				break;
				
			default:
				break;
			}			
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		itemActivity = this;
		
		//set the window no title  modify by chb
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		RelativeLayout layout = (RelativeLayout)LayoutInflater.
				from(this).inflate(R.layout.activity_item_test, null);
		
		loadData();
		View subview = mHW.getView(this);
		mContentView = (RelativeLayout) layout.findViewById(R.id.rl_content);
		if(subview != null) {
			RelativeLayout.LayoutParams lp = new RelativeLayout.
					LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			mContentView.addView(subview, lp);
		} 
		setContentView(layout);
		mHW.onCreate();
	}
	/**
	 * get position from Intent
	 * according position to load test item's data
	 * @date 2015-2-11 pm 2:37:39
	 */
	private void loadData() {
		Intent data = getIntent();
		int position = data.getIntExtra("position", 0);
		mHW = TestItemTool.getHardWares(this, position);
		mHW.setPosition(position);
		mHW.setActivity(this);
	}

	/**
	 * set Pass or fail button's clickable
	 * @param id button's ID  
	 * ItemTestActivity.BUTTON_PASS or ItemTestActivity.BUTTON_FAIL
	 * @param available 
	 * true is clickable ,false is unclickable
	 */
	public void setButtonAvailable(int id, boolean available) {
		((Button)this.findViewById(id)).setClickable(available);
	}
	
	/**
	 * set bottomBar's visibility
	 * @param visible
	 */
	public void setButtomBarVisiblity(boolean visible) {
		LinearLayout bar = (LinearLayout)this.findViewById(R.id.pass_fail_bar);
		if(visible) {
			bar.setVisibility(View.VISIBLE);
		} else {
			bar.setVisibility(View.GONE);
		}
	}
	/**
	 * buttons call back method
	 * @date 2015-1-26 am 11:21:24
	 * @param v
	 */
	public void onClick(View v) {
		// judge which button has been clicked,and deal with it
		switch(v.getId()) {
		/** 1.btn_pass: clicked */
		case R.id.btn_pass:
			mHW.setResult(TestResult.Pass);
			finish();
			break;
			
		/** 2.btn_fail: clicked */
		case R.id.btn_fail:
			mHW.setResult(TestResult.Fail);
			finish();
			break;
			
		default:
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	
		if(!mHW.onKeyDown(keyCode, event)) {
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}
	
	@Override
	protected void onStart() {
		mHW.onStart();
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		mHW.onResume();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		mHW.onPause();
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		mHW.onStop();
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		mHW.onDestroy();
		super.onDestroy();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		mHW.onNewIntent(intent);
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mHW.onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		mHW.onWindowFocusChanged(hasFocus);
		super.onWindowFocusChanged(hasFocus);
	}
	
	@Override
	public void onBackPressed() {
		mHW.onBackPressed();
		super.onBackPressed();
	}
}
