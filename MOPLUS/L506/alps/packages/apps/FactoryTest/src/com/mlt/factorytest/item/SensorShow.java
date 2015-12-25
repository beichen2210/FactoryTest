package com.mlt.factorytest.item;

import java.util.Locale;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mlt.factorytest.R;
import com.mlt.factorytest.ItemTestActivity;
import com.mlt.factorytest.customview.GSensorOriView;
import com.mlt.factorytest.item.tools.SensorTool;
/**
 * 
 * file name:SensorShow.java
 * Copyright MALATA ,ALL rights reserved
 * 2015-2-10
 * author:laiyang
 * 
 * show all sensors(Gyroscope,Gsensor,Psensor,Lsensor,Msensor) data,
 * in Gsensor, show custom view to device direction test
 * some device has no one or more sensors,so if device has no sensor(like Gyroscope，Msensor),
 * hint the device doesn't have the sensor.
 * 
 * Modification history
 * -------------------------------------
 * 
 * -------------------------------------
 */
public class SensorShow extends AbsHardware implements SensorEventListener{
	// ori change value
	private static final float ORI_CHANGE_LEN;
	static {
		ORI_CHANGE_LEN = (float) (9.8f * Math.sin(45*Math.PI/180));
	}
	// Handler's Message : update gyroscope
	private final int MSG_UPDATE_GYRO = 1001;
	// Handler's Message : update gsensor
	private final int MSG_UPDATE_GSSOR = 1002;
	// Handler's Message : update gsensor
	private final int MSG_UPDATE_MSSOR = 1003;
	// Handler's Message : update lsensor
	private final int MSG_UPDATE_LSSOR = 1004;
	// Handler's Message : update psensor
	private final int MSG_UPDATE_PSSOR = 1005;
	// SensorManager
	public SensorManager mSsorManager;
	// sensor: gyroscope
	public Sensor gyro;
	// sensor: gyroscope
	public Sensor gssor;
	// sensor: msensor 
	public Sensor mssor;
	// sensor: lsensor
	public Sensor lssor;
	// sensor: lsensor
	public Sensor pssor;
	// Context of Application
	public Context mContext;
	// Context of Application
	private RelativeLayout mPSsorLayout;
	// layout of Lsensor
	private RelativeLayout mLSsorLayout;
	// layout of Lsensor
	public TextView mtvGyro;
	// Gsensor's TextView,show values of GSensor
	public TextView mtvGssor;
	// Gsensor's TextView,show values of GSensor
	public TextView mtvMssor;
	// Gsensor's TextView,show values of GSensor
	public TextView mtvLssor;
	// PSensor's TextView,show value of PSensor
	public TextView mtvPssor;
	// Gysroscope's hint
	private TextView mtvGyroscopeHint;
	// Gsensor's hint
	private TextView mtvGSensorHint;
	// access Message's of Sensors
	private Handler mHandler;
	// access Message's of Sensors
	private View mLayout;
	// custom view ,show ori of GSensor
	private GSensorOriView mGSsorView;
	// custom view ,show ori of GSensor
	private int mColorWhite;
	// custom view ,show ori of GSensor
	private float mValueLSensor;
	// custom view ,show ori of GSensor
	private float mValuePSensor;
	// if true,the device has Gsensor,or has no Gsensor
	private boolean hasGSsor;
	// if true,the device has Gyroscope,or has no Gyroscope
	private boolean hasGyro;
	// if true,the device has Lsensor,or has no Lsensor
	private boolean hasLSsor;
	// if true,the device has Lsensor,or has no Lsensor
	private boolean hasPSsor;
	// if true,the device has Lsensor,or has no Lsensor
	private boolean hasMSsor;
	/**
	 * construction of class SensorShow 
	 * @param text test item's name
	 * @param visible
	 */
	public SensorShow(String text, Boolean visible) {
		super(text, visible);
	}
	
	@Override
	public View getView(Context context) {
		
		mContext = context;
		
		// init views
		initView();
		// set Title of this Item
		ItemTestActivity.itemActivity.
			setTitle(context.getResources().getString(R.string.item_SensorShow));
		mHandler = new SensorUpdateHandler(ItemTestActivity.itemActivity.getMainLooper());
		
		// initialize all of sensors
		initSensors();
		
		// set Pass button unClickable
		ItemTestActivity.itemActivity.handler.
			sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_UNCLICKABLE);
		mColorWhite = mContext.getResources().getColor(R.color.floralwhite);
		
		return mLayout;
	}
	/**
	 * init all sensors
	 * if the device not available the sensor,will not init the sensor
	 * @date 2015-1-31 pm 3:49:53
	 */
	private void initSensors() {
		
		mSsorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		
		if(hasGyro = SensorTool.hasSensor(mSsorManager, Sensor.TYPE_GYROSCOPE)) {
			gyro = mSsorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
			mtvGyroscopeHint.setVisibility(View.GONE);
		}  else {
			mtvGyro.setText(mContext.getString(R.string.tv_gyro_not_available));
		}
		if(hasGSsor = SensorTool.hasSensor(mSsorManager, Sensor.TYPE_ACCELEROMETER)){
			gssor = mSsorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mtvGSensorHint.setVisibility(View.GONE);
		} else {
			mtvGssor.setText(mContext.getString(R.string.tv_gssor_not_available));
		} 
		if(!(hasMSsor = SensorTool.hasSensor(mSsorManager, Sensor.TYPE_MAGNETIC_FIELD))) {
			mtvMssor.setText(mContext.getString(R.string.tv_mssor_not_available));
		} else {
			mssor = mSsorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		}
		if(!(hasLSsor = SensorTool.hasSensor(mSsorManager, Sensor.TYPE_LIGHT))) {
			mtvLssor.setText(mContext.getString(R.string.tv_lssor_not_available));
		} else {
			lssor = mSsorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		}
		if(!(hasPSsor = SensorTool.hasSensor(mSsorManager, Sensor.TYPE_PROXIMITY))) {
			mtvPssor.setText(mContext.getString(R.string.tv_pssor_not_available));
		} else {
			pssor = mSsorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		}
		
	}
	/**
	 * init all views
	 * @date 2015-1-31 pm 3:53:28
	 */
	private void initView() {
		mLayout = LayoutInflater.from(mContext).inflate(R.layout.item_sensor_show, null);
		mGSsorView = (GSensorOriView) mLayout.findViewById(R.id.gssor_ori);
		mtvGyro = (TextView) mLayout.findViewById(R.id.tv_gyro);
		mtvGssor = (TextView) mLayout.findViewById(R.id.tv_gssor);
		mtvMssor = (TextView) mLayout.findViewById(R.id.tv_mssor);
		mtvLssor = (TextView) mLayout.findViewById(R.id.tv_lssor_value);
		mtvPssor = (TextView) mLayout.findViewById(R.id.tv_pssor_value);
		mtvGyroscopeHint  = (TextView) mLayout.findViewById(R.id.tv_gyroscope_hint);
		mtvGSensorHint  = (TextView) mLayout.findViewById(R.id.tv_gsensor_hint);
		mPSsorLayout = (RelativeLayout) mLayout.findViewById(R.id.rl_pssor);
		mLSsorLayout = (RelativeLayout) mLayout.findViewById(R.id.rl_lssor);
	}

	/**
	 * registerListener of SensorManager
	 * if not have sensor,no register the listener
	 */
	@Override
	public void onResume() {
		if(hasGyro) {
			mSsorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
		}
		if(hasGSsor) {
			mSsorManager.registerListener(this, gssor, SensorManager.SENSOR_DELAY_NORMAL);
		}
		if(hasMSsor) {
			mSsorManager.registerListener(this, mssor, SensorManager.SENSOR_DELAY_NORMAL);
		}
		if(hasLSsor) {
			mSsorManager.registerListener(this, lssor, SensorManager.SENSOR_DELAY_NORMAL);
		}
		if(hasPSsor) {
			mSsorManager.registerListener(this, pssor, SensorManager.SENSOR_DELAY_NORMAL);
		}
		super.onResume();
	}
	/**
	 * when sensor data changed,send handler message to update view
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		Message msg = null;
		float values[] = event.values;
		synchronized (this) { 
			
			switch(event.sensor.getType()) {
			
			/** 1.ACCELEROMETER:SensorEvent type is TYPE_ACCELEROMETER */
			case Sensor.TYPE_ACCELEROMETER:
				sendGSensorChangedMSG(values, msg);
				break;
				
			/** 2.GYROSCOPE:SensorEvent type is TYPE_GYROSCOPE */
			case Sensor.TYPE_GYROSCOPE:
				sendGyroChangedMSG(values, msg);
				break;
				
			/** 3.MAGNETIC_FIELD:SensorEvent type is TYPE_MAGNETIC_FIELD */
			case Sensor.TYPE_MAGNETIC_FIELD:
				sendMSensorChangedMSG(values, msg);
				break;
					
			/** 4.LIGHT:SensorEvent type is TYPE_LIGHT */
			case Sensor.TYPE_LIGHT:
				sendLSensorChangedMSG(values, msg);
				break;
				
			/** 5.PROXIMITY:SensorEvent type is TYPE_PROXIMITY */
			case Sensor.TYPE_PROXIMITY:
				sendPSensorChangedMSG(values, msg);
				break;
			
			default:
				break;
			} // end of switch(event.sensor.getType())
		} // end of synchronized (this)
	} // end of onSensorChanged(SensorEvent event)
	/**
	 * Psensor changed ,send message to update view
	 * @date 2015-2-11 pm 3:31:15
	 * @param values
	 * @param msg
	 */
	private void sendPSensorChangedMSG(float[] values, Message msg) {
		msg = mHandler.obtainMessage(MSG_UPDATE_PSSOR, values[0]);
		mHandler.sendMessage(msg);
	}
	/**
	 * Lsensor changed ,send message to update view
	 * @date 2015-2-11 pm 3:31:15
	 * @param values
	 * @param msg
	 */
	private void sendLSensorChangedMSG(float[] values, Message msg) {
		msg = mHandler.obtainMessage(MSG_UPDATE_LSSOR, values[0]);
		mHandler.sendMessage(msg);
	}
	/**
	 * Msensor changed ,send message to update view
	 * @date 2015-2-11 pm 3:31:15
	 * @param values
	 * @param msg
	 */
	private void sendMSensorChangedMSG(float[] values, Message msg) {
		msg = mHandler.obtainMessage(MSG_UPDATE_MSSOR, 
				String.format(Locale.ENGLISH, "X:%+8.4f\nY:%+8.4f\nZ:%+8.4f", 
				values[0], values[1], values[2]));
		mHandler.sendMessage(msg);
	}
	/**
	 * Gyroscope changed ,send message to update view
	 * @date 2015-2-11 pm 3:31:15
	 * @param values
	 * @param msg
	 */
	private void sendGyroChangedMSG(float[] values, Message msg) {
		msg = mHandler.obtainMessage(MSG_UPDATE_GYRO, 
				String.format(Locale.ENGLISH, "X:%+8.4f\nY:%+8.4f\nZ:%+8.4f", 
				values[0], values[1], values[2]));
		mHandler.sendMessage(msg);
	}
	/**
	 * Gsensor changed ,send message to update view
	 * @date 2015-2-11 pm 3:31:15
	 * @param values
	 * @param msg
	 */
	private void sendGSensorChangedMSG(float[] values, Message msg) {
		msg = mHandler.obtainMessage(MSG_UPDATE_GSSOR, 
				String.format(Locale.ENGLISH, "X:%+8.4f\nY:%+8.4f\nZ:%+8.4f", 
			values[0], values[1], values[2]));
		mHandler.sendMessage(msg);
		if(values[0] > ORI_CHANGE_LEN || values[0] < -ORI_CHANGE_LEN ||
			values[1] > ORI_CHANGE_LEN || values[1] < -ORI_CHANGE_LEN ||
					values[2] > ORI_CHANGE_LEN || values[2] < -ORI_CHANGE_LEN ) {
			updateGssorView(values[0], values[1], values[2]);
		}
		
		if(mGSsorView.getmOriTestSum() == GSensorOriView.ORI_SUM) {
			ItemTestActivity.itemActivity.handler.
				sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_CLICKABLE);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// not used
	}
	/**
	 * when leave this Activity, unregisterListener of SensorManager
	 */
	@Override
	public void onPause() {
		mSsorManager.unregisterListener(this);
		mGSsorView.clearViewState();
		super.onPause();
	}
	/**
	 * 
	 * @date 2015-2-11 pm 3:29:36
	 * @param x Gx 
	 * @param y Gy
	 * @param z Gz
	 */
	public void updateGssorView(float x, float y, float z) {
		if(x > ORI_CHANGE_LEN && !mGSsorView.isRight) {
			mGSsorView.isRight = true;
			mGSsorView.addmOriTestSum();
		} 
		if(x < -ORI_CHANGE_LEN && !mGSsorView.isLeft) {
			mGSsorView.isLeft = true;
			mGSsorView.addmOriTestSum();
		}
		if(y < -ORI_CHANGE_LEN && !mGSsorView.isDown) {
			mGSsorView.isDown = true;
			mGSsorView.addmOriTestSum();
		}
		if(y > ORI_CHANGE_LEN && !mGSsorView.isUp) {
			mGSsorView.isUp = true;
			mGSsorView.addmOriTestSum();
		}
		if(z > ORI_CHANGE_LEN && !mGSsorView.isPositive) {
			mGSsorView.isPositive = true;
			mGSsorView.addmOriTestSum();
		}
		if(z < -ORI_CHANGE_LEN && !mGSsorView.isNegative) {
			mGSsorView.isNegative = true;
			mGSsorView.addmOriTestSum();
			Log.i("hah", ""+z);
		}
		mGSsorView.invalidate();
	}
	
	class SensorUpdateHandler extends Handler {

		public SensorUpdateHandler(Looper looper) {
			super(looper);
		}
	
		public void handleMessage(android.os.Message msg) {
			switch(msg.what) {
			/** 1.Gyroscope changed */
			case MSG_UPDATE_GYRO:
				mtvGyro.setText((String)msg.obj);
				break;
				
			/** 2.GSensor changed */
			case MSG_UPDATE_GSSOR:
				mtvGssor.setText((String)msg.obj);
				break;
			/** 3.MSensor changed */
			case MSG_UPDATE_MSSOR:
				mtvMssor.setText((String)msg.obj);
				break;
			/** 4.LSensor changed */
			case MSG_UPDATE_LSSOR:
				float valuel = ((Float)msg.obj).floatValue();
				if(valuel > 10 /* laiyang change value 20150926, old:30 */) {
					mLSsorLayout.setBackgroundColor(mColorWhite);
				} else {
					mLSsorLayout.setBackgroundColor(Color.GREEN);
				}
				mtvLssor.setText(""+msg.obj);
				break;
			/** 5.PSensor changed */
			case MSG_UPDATE_PSSOR:
				float valuep = ((Float)msg.obj).floatValue();
				if(valuep < 1) {
					mPSsorLayout.setBackgroundColor(Color.BLUE);
				} else {
					mPSsorLayout.setBackgroundColor(mColorWhite);					
				}
				
				mtvPssor.setText(""+msg.obj);
				break;
			default:
				break;
			}
		}
	}
}
