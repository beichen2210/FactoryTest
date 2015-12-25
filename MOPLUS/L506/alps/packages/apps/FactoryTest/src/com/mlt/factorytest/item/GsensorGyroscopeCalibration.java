package com.mlt.factorytest.item;

import java.util.Locale;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mlt.factorytest.R;
import com.mediatek.engineermode.sensor.EmSensor;
import com.mlt.factorytest.ItemTestActivity;
import com.mlt.factorytest.item.tools.SensorTool;
/**
 * 
 * file name:GsensorGyroscopeCalibration.java
 * Copyright MALATA ,ALL rights reserved
 * 2015-1-26
 * author:laiyang
 * Modification history
 * -------------------------------------
 * 
 * -------------------------------------
 */
public class GsensorGyroscopeCalibration extends AbsHardware implements 
			SensorEventListener {

	// message calibration gyroscope fail
	public static final int MSG_CAL_GYRO_FAIL = 10020;
	// message calibration GSensor fail
	public static final int MSG_CAL_GSSOR_FAIL = 10021;
	// message calibration gyroscope success
	private static final int MSG_CAL_GYRO_SUCCESS = 10010;
	// message calibration gyroscope success
	private static final int MSG_CAL_GSSOR_SUCCESS = 10011;
	// message calibration gyroscope start
	private static final int MSG_CAL_GYRO_ING = 10040;
	// message calibration gyroscope start
	private static final int MSG_CAL_GSSOR_ING = 10041;
	// message update gsensor view
	private static final int MSG_UPDATE_GSENSOR = 10030;
	// message update gyroscope view
	private static final int MSG_UPDATE_GYROSCOPE = 10031;
	// message update gyroscope view
	private static final int MSG_SET_BTN_BG = 10032;
	// SensorEventType: Gyroscope
	public static final int TYPE_GSSOR = Sensor.TYPE_ACCELEROMETER;
	// SensorEventType: GSensor
	public static final int TYPE_GYRO = Sensor.TYPE_GYROSCOPE;
	// default calibration tolerance 20%
	private static final int TOLERANCE = 20;
	// application context
	public Context mContext;
	// button start:when click this button,start calibration GSensor & Gyroscope
	private Button mbtnStart;
	/** use button show GSensor calibration status,
	 * red background is not calibration or calibration fail,
	 * green background is calibration success
	 */
	private TextView mbtnGssorCalStatus;
	// use TextView show Gyroscope calibration status
	private TextView mbtnGyroCalStatus;
	// show GSensor's value (x, y, z)
	private TextView mtvGSsorValue;
	// show Gyroscope's value (x, y, z)
	private TextView mtvGyroValue;
	// handMessage to update view or make Toast
	private Handler mHandler;
	// the SensorManager is to listener sensors changed
	private SensorManager mSensorManager;
	// Sensor GSensor
	private Sensor mGSensor;
	/**
	 * Sensor Gyroscope
	 */
	private Sensor mGyroscope;
	/**
	 * default false
	 * if true ,means GSensor calibration success ,change status bar
	 */
	private boolean isGSensorCalSuccess;
	/**
	 * default false
	 * if true ,means Gyroscope calibration success ,change status bar
	 */
	private boolean isGyroCalSuccess;
	// flag the device whether has GSensor
	private boolean hasGSensor;
	// flag the device whether has Gyroscope
	private boolean hasGyroscope;
	/**
	 * constructor of GsensorGyroscopeCalibration
	 * @param text
	 * @param visible
	 */
	public GsensorGyroscopeCalibration(String text, Boolean visible) {
		super(text, visible);
	}
	/*
	 * init test view
	 */
	@Override
	public View getView(Context context) {
		this.mContext = context;
		// load layout xml file
		View v = LayoutInflater.from(context).inflate(R.layout.item_gssogyrocal, null);
		
		// init views,and set listeners
		initViews(v);          
		// init handler, accept messages
		mHandler = new CalibrationHandler(ItemTestActivity.itemActivity.getMainLooper());
		// init SensorManager and sensors
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		
		if(hasGSensor = SensorTool.hasSensor(mSensorManager, Sensor.TYPE_ACCELEROMETER)){
			mGSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		} else {
			mtvGSsorValue.setText(context.getString(R.string.tv_gssor_not_available));
			mbtnGssorCalStatus.setVisibility(View.INVISIBLE);
		}
		if(hasGyroscope = SensorTool.hasSensor(mSensorManager, Sensor.TYPE_GYROSCOPE)) {
			mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		} else {
			mtvGyroValue.setText(context.getString(R.string.tv_gyro_not_available));
			mbtnGyroCalStatus.setVisibility(View.INVISIBLE);
		}
		// set pass button unClickable
		if(hasGSensor || hasGyroscope) {
			ItemTestActivity.itemActivity.handler.
				sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_UNCLICKABLE);
		}
		// init flags which control calibration
		initflags();
		
		/**set the acitivty title*/
		ItemTestActivity.itemActivity.setTitle(R.string.item_GssorGyroCal);
		
		return v;
	}
	/**
	 * init views and set listeners
	 * @author laiyang
	 * @date 2015-1-26 9:30:51
	 * @param v ParentView
	 */
	private void initViews(View v) {
		mbtnStart = (Button) v.findViewById(R.id.bt_start_cal);
		mbtnGssorCalStatus = (TextView) v.findViewById(R.id.bt_gssor_cal_status);
		mbtnGyroCalStatus = (TextView) v.findViewById(R.id.bt_gyro_cal_status);
		mtvGSsorValue = (TextView) v.findViewById(R.id.tv_gssor_value);
		mtvGyroValue = (TextView) v.findViewById(R.id.tv_gyro_value);
		// set listeners
		mbtnStart.setOnClickListener(new OnStartCalListener());
	}
	/**
	 * init flags which control calibration <p>
	 * {@link #isGSensorCalSuccess},
	 * {@link #isGyroCalSuccess},
	 * {@link #hasGSensor},
	 * {@link #hasGyroscope}
	 * @date 2015-1-26 9:35:15
	 */
 	private void initflags() {
		isGSensorCalSuccess = false;
		isGyroCalSuccess = false;
		
	}
	/**
	 * when show activity, register sensors' listener
	 */
	@Override
	public void onResume() {
		// modified by ly 1/27
		if(hasGSensor) {
			mSensorManager.registerListener(this, mGSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
		if(hasGyroscope) {
			mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
		}
		super.onResume();
	}
	/*
	 * when leave this activity, unregister sensor's listener
	 * 
	 */
	@Override
	public void onPause() {
		// modified by ly 1/27
		if(hasGSensor || hasGyroscope) {
			mSensorManager.unregisterListener(this);
		}
		super.onPause();
	}
	
	/**
	 * send message to mHandler
	 * @param what
	 */
	private void sendMessage(int what) {
		Message msg = mHandler.obtainMessage();
		msg.what = what;
		mHandler.sendMessage(msg);
	}
	/**
	 * when click this, start calibration sensors
	 */
	class OnStartCalListener implements View.OnClickListener {
		/*
		 * (non-Javadoc)
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			// start calibration,set start button unclickable
			mbtnStart.setClickable(false);
			mbtnStart.setBackgroundResource(R.drawable.selector_button_unable);
			if(v.getId() ==  R.id.bt_start_cal) { // start calibration
				new Thread(){
					public void run() {
						// if true, start calibration GSensor,and send message to change view
						if(hasGSensor) {
							sendMessage(MSG_CAL_GSSOR_ING);
							setCalibration(TYPE_GSSOR, MSG_CAL_GSSOR_FAIL);
						}
						// if true, start calbiration Gyroscope,and send message to change view
						if(hasGyroscope) {
							sendMessage(MSG_CAL_GYRO_ING);
							setCalibration(TYPE_GYRO, MSG_CAL_GYRO_FAIL);
						}
						// Calibration finish, set start button clickable
						mbtnStart.setClickable(true);
						sendMessage(MSG_SET_BTN_BG);
					}
				}.start();
			}	
		}
	}
	/**
	 * according to type to calibration sensor
	 * @date 2015-1-31 pm 2:59:46
	 * @param type sensors type
	 * @param error if occurt error, the error's type
	 */
    private void setCalibration(int type, int error) {
        int result = 0;
        if (type == TYPE_GSSOR) {// if type is GSensor, do Gsensor calibration
            result = EmSensor.doGsensorCalibration(TOLERANCE);
        } else if (type == TYPE_GYRO) {// if type is Gyrosocpe, do Gyroscope calibration
            result = EmSensor.doGyroscopeCalibration(TOLERANCE);
        }

        if (result == EmSensor.RET_SUCCESS) {// calibration success,send message
        	if(type == TYPE_GSSOR) {
        		isGSensorCalSuccess = true;
				sendMessage(MSG_CAL_GSSOR_SUCCESS);
			} else if(type == TYPE_GYRO) {
				isGyroCalSuccess = true;
				sendMessage(MSG_CAL_GYRO_SUCCESS);
			}
        } else {// calibration fail,send message
        	sendMessage(error);
        }
    }
    
    class CalibrationHandler extends Handler{
    	// string of calibration success
    	private String sCalSuccess;
    	// string of calibration fail
		private String sCalFail;
		// init sCalSuccess and sCalFail
		{
			sCalSuccess = GsensorGyroscopeCalibration.this.mContext.
					getResources().getString(R.string.toast_cal_success);
			
			sCalFail = GsensorGyroscopeCalibration.this.mContext.
					getResources().getString(R.string.toast_cal_failed);
		}
		
		public CalibrationHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			// if has GSensor and GSensor calibration success and
			// has Gysrocope and Gyrocope calibration success ,set pass button clickable
			if((!hasGSensor || isGSensorCalSuccess) && (!hasGyroscope || isGyroCalSuccess)) {
				// set pass button clickable
				ItemTestActivity.itemActivity.handler.
					sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_CLICKABLE);
			}
			switch(msg.what) {
			/** 1.calibration gyroscope success */
			case MSG_CAL_GYRO_SUCCESS:
				mbtnGyroCalStatus.setText(mContext.getString(R.string.cal_status_success));
				mbtnGyroCalStatus.setBackgroundResource(R.drawable.selector_button_green);
				break;
			
			/** 2.calibration gsensor success */
			case MSG_CAL_GSSOR_SUCCESS:
				mbtnGssorCalStatus.setText(mContext.getString(R.string.cal_status_success));
				mbtnGssorCalStatus.setBackgroundResource(R.drawable.selector_button_green);
				break;
			
			/** 3.calibration gyroscope fail */
			case MSG_CAL_GYRO_FAIL:
				mbtnGyroCalStatus.setText(mContext.getString(R.string.cal_status_fail));
				mbtnGyroCalStatus.setBackgroundResource(R.drawable.selector_button_red);
				break;
				
			/** 4.calibration gsensor fail */
			case MSG_CAL_GSSOR_FAIL:
				mbtnGssorCalStatus.setText(mContext.getString(R.string.cal_status_fail));
				mbtnGssorCalStatus.setBackgroundResource(R.drawable.selector_button_red);
				break;
			
			/** 5.update gsensor data */
			case MSG_UPDATE_GSENSOR:
				mtvGSsorValue.setText((String)msg.obj);
				break;
				
			/** 6.update gyroscope data */
			case MSG_UPDATE_GYROSCOPE:
				mtvGyroValue.setText((String)msg.obj);
				break;
				
			/** 7.set start button's background */
			case MSG_SET_BTN_BG:
				mbtnStart.setBackgroundResource(R.drawable.selector_button);
				break;
			
			/** 8.status is calibration gyroscope  */
			case MSG_CAL_GYRO_ING:
				mbtnGyroCalStatus.setText(mContext.getString(R.string.cal_status_ing));
				//mbtnGyroCalStatus.setBackgroundResource(R.drawable.selector_textview);
				break;

			/** 9.status is calibration gsensor  */	
			case MSG_CAL_GSSOR_ING:
				mbtnGssorCalStatus.setText(mContext.getString(R.string.cal_status_ing));
				//mbtnGssorCalStatus.setBackgroundResource(R.drawable.selector_textview);
				break;
				
			default:
				break;
			}
		}
    }
    /**
     * when SensorChanged ,call back this method,and update views
     * 
     */
	@Override
	public void onSensorChanged(SensorEvent event) {
		Message msg = null;
		float[] values = event.values;
		synchronized (this) {
			switch (event.sensor.getType()) {
			/** 1.sensor event type is gsensor, format to string and send message */
			case TYPE_GSSOR:
				msg = mHandler.obtainMessage(MSG_UPDATE_GSENSOR, 
						String.format(Locale.ENGLISH, "X:%+8.4f\nY:%+8.4f\nZ:%+8.4f", 
						values[0], values[1], values[2]));
				mHandler.sendMessage(msg);
				break;
			
			/** 2.sensor event type is gyroscope, format to string and send message */
			case TYPE_GYRO:
				msg = mHandler.obtainMessage(MSG_UPDATE_GYROSCOPE, 
						String.format(Locale.ENGLISH, "X:%+8.4f\nY:%+8.4f\nZ:%+8.4f", 
					values[0], values[1], values[2]));
				mHandler.sendMessage(msg);
				break;
				
			/** 3.default : break */
			default:
				break;
			}
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// not used
	}
}
