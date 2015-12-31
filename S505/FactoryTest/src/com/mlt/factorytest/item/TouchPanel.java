package com.mlt.factorytest.item;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import com.mlt.factorytest.item.AbsHardware;
import com.mlt.factorytest.ItemTestActivity;
import com.mlt.factorytest.R;
import com.mlt.factorytest.customview.TouchPanelView;
/**
 * 
 * file name:TouchPanel.java
 * Copyright MALATA ,ALL rights reserved
 * 2015-2-10
 * author:laiyang
 * 
 * The test divide two steps.
 * FullScreen to shwo touch view ,can touch anywhere in screen,if touch's point has square
 * the square change to green,if all squares are touched,step to next,if next are all touched
 * too, pass button will be showed,and user can click pass button to finish this test,or click
 * fail button to finish the test. 
 * 
 * Modification history
 * -------------------------------------
 * 
 * -------------------------------------
 */ 
public class TouchPanel extends AbsHardware implements
	View.OnClickListener {
	/**
	 * The string of Log's TAG
	 */
	private static final String TAG = "TouchPanel";
	
//	public static final int MSG_STEP_TO_NEXT = 0x01;
	/**
	 * The message of finish current activity 
	 */
	public static final int MSG_STEP_TO_FINISH = 0x02;
	/**
	 * The application's context
	 */
	private static Context mContext;
	/**
	 * use to access msg to change view
	 */
	public Handler mHandler;
	/**
	 * button pass, click this will finish this activity,and test status is pass
	 */
	private Button mbtnPass;
	/**
	 * button fail, click this will finish this activity,and test status is fail
	 */
	private Button mbtnFail;
	/**
	 * custom view to test touch panel
	 */
	private TouchPanelView mTPView;
	/**
	 * the constructor of TouchPanel
	 * @param text TouchPanel's text
	 * @param visible
	 */
	public TouchPanel(String text, Boolean visible) {
		super(text, visible);
	}
	
	@Override
	public View getView(Context context) {
		mContext = context;
		
		// set window fullscreen
		setWindowFullScreen();
		
		// bind MainLooper and Handler
		mHandler = new TouchHandler(context.getMainLooper());
		
		// when testing, forbid status bar pull-down 
		View layout = LayoutInflater.from(context).inflate(R.layout.item_touch_panel, null);
		layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED);
		
		// init all sub view of layout,and set buttons listener
		initViews(layout);
		return layout;
	}
	/**
	 * init all sub view of layout,and set buttons listener
	 * @date 2015-2-10 pm 6:01:04
	 * @param v
	 */
	private void initViews(View v) {
		
		// init view
		mbtnPass = (Button) v.findViewById(R.id.btn_tp_pass);
		mbtnFail = (Button) v.findViewById(R.id.btn_tp_fail);
		mTPView = (TouchPanelView)v.findViewById(R.id.tp_area);
		mTPView = (TouchPanelView)v.findViewById(R.id.tp_area);
		mbtnPass.setVisibility(View.INVISIBLE);
		
		// set listeners
		mbtnFail.setOnClickListener(this);
		mbtnPass.setOnClickListener(this);
		mTPView.setTouchPanel(this);
	}
	/**
	 * set activity window fullScreen
	 * @date 2015-2-10 pm 5:58:22
	 */
	private void setWindowFullScreen() {
		ItemTestActivity.itemActivity.
			getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		ItemTestActivity.itemActivity.
			getWindow().requestFeature(Window.FEATURE_PROGRESS);
		ItemTestActivity.itemActivity.getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN, 
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	/*
	 * when show this activity,hidden buttonBar
	 */
	@Override
	public void onResume() {
		// hidden buttonBar in bottom
		ItemTestActivity.itemActivity.handler.
				sendEmptyMessage(ItemTestActivity.MSG_BTNBAR_INVISIBLE);
		super.onResume();
	}
	/**
	 * 
	 * 2015-2-10
	 * author:laiyang
	 * 
	 * Handler to access message to change view
	 */
	private class TouchHandler extends Handler {
		
		public TouchHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			/** MSG to finish all activity */
			case MSG_STEP_TO_FINISH:
				mbtnPass.setVisibility(View.VISIBLE);
				break;
				
			default:
				break;
			}
		}
		
	}
	/*
	 * click pass or fail button to finish cunrrent test
	 */
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		
		/** click pass to set Test result pass */
		case R.id.btn_tp_pass:
			setResult(TestResult.Pass);
			ItemTestActivity.itemActivity.finish();
			mTPView.stepToFinish();
			break;
			
		/** click pass to set Test result fail */
		case R.id.btn_tp_fail:
			setResult(TestResult.Fail);
			ItemTestActivity.itemActivity.finish();
			break;
			
		default:
			break;
		}
	}
}
