package com.mlt.factorytest.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;

import com.mlt.factorytest.ItemTestActivity;
import com.mlt.factorytest.R;
import com.mlt.factorytest.item.AbsHardware.TestResult;
import com.mlt.factorytest.item.MyApplication;
/**
 * 
 * file name:TestItemAdapter.java
 * Copyright MALATA ,ALL rights reserved
 * 
 * This class is GridView's adapter,is to show all test items,
 * and can click one item to start one test
 * 
 * 2015-1-26
 * author:laiyang
 * Modification history
 * -------------------------------------
 * 
 * -------------------------------------
 */
public class TestItemAdapter implements ListAdapter {
	// application context
	private Context mContext;
	
	private List<TestResult> mResults;
	
	private List<String> mTexts;
	
	public TestItemAdapter(Context context) {
		mContext = context;
		mResults = MyApplication.getResults();
		mTexts = MyApplication.getHWTexts();
	}
	
	@Override
	public int getCount() {
		return mResults.size();
	}
	
	@Override
	public Object getItem(int position) {
		return mResults.get(position);
	}
	
	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
	
	@Override
	public int getItemViewType(int position) {
		return position;
	}
	/**
	 * show test item in gridview
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		
		TestResult result = mResults.get(position);
		Button bt = null;
		if(convertView == null) {// load xml
			convertView = LayoutInflater.from(mContext).inflate(R.layout.gridview_item, null);
			bt = (Button)convertView.findViewById(R.id.bt_item);
			convertView.setTag(bt);
		} else {
			bt = (Button)convertView.getTag();
		}
		bt.setText(mTexts.get(position));
		bt.setOnClickListener(null);
		bt.setOnClickListener(new OnClickListener(position));
		switch(result) { 
		/** 1.pass*/
		case Pass:
			bt.setBackgroundResource(R.drawable.selector_button_green);
			break;
		
		/** 2.pass */
		case Fail:
			bt.setBackgroundResource(R.drawable.selector_button_red);
			break;
			
		case UnCheck:
		// no break , no need to deal 
			
		default:
			break;
		}
		return convertView;
	}
	
	@Override
	public int getViewTypeCount() {
		return 1;
	}
	
	@Override
	public boolean hasStableIds() {
		return false;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public void registerDataSetObserver(DataSetObserver arg0) {

	}
	
	@Override
	public void unregisterDataSetObserver(DataSetObserver arg0) {

	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}
	
	@Override
	public boolean isEnabled(int arg0) {
		return false;
	}
	
	private class OnClickListener implements View.OnClickListener {
		private int position;
		public OnClickListener(int position) {
			this.position = position;
		}
		/**
		 * when click item, jump to sub page to show test item
		 */
		@Override
		public void onClick(View v) {
			if(isFastDoubleClick()) {
				return;
			}
			Intent i = new Intent(mContext, ItemTestActivity.class);
			i.putExtra("position", position);
			mContext.startActivity(i);
		}
	}
	
	/**
	 * last click time
	 */
	private static long lastClickTime;
	/**
	 * double click gap time
	 */
	private final static long GAP_TIME = 2000;
	/**
	 * is to avoid user's double click
	 * @date 2015-4-1 pm 4:26:38
	 * @return
	 */
	public static boolean isFastDoubleClick() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		if (0 < timeD && timeD < GAP_TIME) {
			return true;
		}
		lastClickTime = time;
		return false;
	}
}

