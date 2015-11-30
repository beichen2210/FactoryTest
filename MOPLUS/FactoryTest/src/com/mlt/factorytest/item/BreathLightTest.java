package com.mlt.factorytest.item;

import com.mlt.factorytest.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

public class BreathLightTest extends AbsHardware {
    private Context mContext;
    private static final String FACTORYTEST_CLOSE_BREATHLIGHT = "com.android.factorytest.closebreathlight";
    private static final String FACTORYTEST_OPEN_BREATHLIGHT = "com.android.factorytest.openbreathlight";
    private static final String FACTORYTEST_STOP_BREATHLIGHT = "com.android.factorytest.stopbreathlight";

    public BreathLightTest(String text, Boolean visible) {
        super(text, visible);
        // TODO Auto-generated constructor stub
    }

    @SuppressLint("InflateParams")
	@Override
    public View getView(Context context) {
    	this.mContext = context;
        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.item_breath_light, null);
        return view;
    }
    
    
    @Override
    public void onCreate() {
        super.onCreate();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	Intent mBreathLightOpenIntent = new Intent();
    	mBreathLightOpenIntent.setAction(FACTORYTEST_OPEN_BREATHLIGHT);
        mContext.sendBroadcast(mBreathLightOpenIntent);
    }
    @Override
    public void onPause() {
    	super.onPause();
    	Intent mBreathLightCloseIntent = new Intent();
        mBreathLightCloseIntent.setAction(FACTORYTEST_CLOSE_BREATHLIGHT);
        mContext.sendBroadcast(mBreathLightCloseIntent);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent mBreathLightStopIntent = new Intent();
        mBreathLightStopIntent.setAction(FACTORYTEST_STOP_BREATHLIGHT);
        mContext.sendBroadcast(mBreathLightStopIntent);
    }
    
}
