package com.mlt.factorytest.item;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
//import android.os.ServiceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mlt.factorytest.R;

import android.os.SystemProperties;
import android.os.ServiceManager;





import com.android.internal.telephony.Phone;// 存在

//import com.android.internal.telephony.gemini.GeminiPhone;

import com.android.internal.telephony.PhoneConstants;//存在

//import com.mediatek.common.featureoption.FeatureOption;
//import com.mediatek.settings.FeatureOption;

import com.mediatek.telephony.TelephonyManagerEx;

//import com.android.internal.telephony.gemini.MTKPhoneFactory;

import com.mlt.factorytest.ItemTestActivity;
import com.mlt.factorytest.item.NativeWiFiMacMethods;
import com.mlt.factorytest.item.tools.BTAdmin;
import com.android.internal.telephony.PhoneFactory;//存在


/** 
* @ClassName: VersionAndBatteryTest 
* @PackageName:com.malata.factorytest.item
* @Description: get the phone Version infomation and battery charging infomation.
* @author:   chehongbin
* @date:     2015年1月27日 上午11:12:27  
* Copyright (c) 2015 MALATA,All Rights Reserved.
*/

public class VersionAndBatteryTest extends AbsHardware {
	private static final String TAG = "BATTERY_CHARGE";
	private static final String TAG_BARCODE = "BARCODE";
	private Context mContext;
	private TextView mtvIMEI1,mtvIMEI2;// phone IMEI
	private TextView mtvSWVer,mtvWiFiMAC,mtvBTADDR,mtvBuildTime,mtvBarCode;
	private TextView mtvBatteryChargedState,mtvBatteryVoltage,mtvBatteryCurrent,
					 mtvBatteryChargingVoltage,mtvBatteryChargingCurrent;
	private String mIMEI1,mIMEI2;
	private static final String FILENAME_PROC_VERSION = "/proc/version";
	
    private static final int EVENT_BATTERY_INFO_UPDATE = 1; //  battery charging info update 
    private static final int EVENT_PASS_BUTTON_UPDATE = 2; //  battery charging info update 
    private static final int UPDATE_INTERVAL = 1000; // 1 s update once
  
    private boolean mBatteryThreadruning = false;//thread battery switch
    private boolean mChargingdState = false;// judge battery charging or not
    private Bundle mBundle = new Bundle();
    private Intent mIntentBatteryState;// battery broadcast intent 
    private Builder mBuilder;
    private int mtimes = 1;
    //chb add for LFZS-154 2015-8-20 begin
    private BTAdmin mBtAdmin;
    private BluetoothAdapter btAdapter;  
    //chb add for LFZS-154 2015-8-20 begin 
    
    private boolean mIsUpdateCurrent = false;//chb add for VFOZESGW-75 at 20151016
    private boolean mIsSendMessage = true;//chb add for VFOZESGW-75 at 20151016
    @Override
    public void onCreate() {
    	super.onCreate();
    	this.mtimes = 1; 
		Log.i(TAG, "oncreat");
		/**set the pass button can't click*/
		ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_UNCLICKABLE);
		/**set the acitivty title*/
		ItemTestActivity.itemActivity.setTitle(R.string.item_VersionInfo);
    }
    
	public VersionAndBatteryTest(String text, Boolean visible) {
		super(text, visible);
	}
	  
	@Override
	public View getView(Context context) {
		this.mContext = context;
		//chb add for LFZS-154 2015-8-20 begin
		mBtAdmin = new BTAdmin(mContext);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter.getState() == 10) {
			Log.d(TAG, "ba.getState:"+btAdapter.getState());
			mBtAdmin.openBT();
			Log.d(TAG, "ba.getState:"+btAdapter.getState());
		} 
		try {
			Thread.sleep(200);
		} catch (Exception e) {
		}
		//chb add for LFZS-154 2015-8-20 begin
		View view = LayoutInflater.from(context).inflate(R.layout.item_version_battery, null);
		mtvBatteryChargedState = (TextView) view.findViewById(R.id.textview_batteryCharging);
		mtvBatteryChargingCurrent = (TextView) view.findViewById(R.id._textview_chargingCurrent);
		mtvBatteryChargingVoltage = (TextView) view.findViewById(R.id._textview_chargingVol);
		mtvIMEI1 = (TextView) view.findViewById(R.id.imei1);
		mtvIMEI2 = (TextView) view.findViewById(R.id.imei2);
		mtvSWVer = (TextView) view.findViewById(R.id.sw1);
		mtvWiFiMAC = (TextView) view.findViewById(R.id.wifimac1);
		mtvBTADDR = (TextView) view.findViewById(R.id.btaddrc1);
		mtvBarCode = (TextView) view.findViewById(R.id.serial_text1);
		test();
		return view;
		
	}
		
	@SuppressLint({ "NewApi", "ResourceAsColor", "DefaultLocale" })
	private TestResult test() {
		/**Get the IMEI*/
		TelephonyManager telephonyManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		String IMEI = telephonyManager.getDeviceId();// IMEI
		getIMEIInfo();
		if(isGeminiEnabled()){//The determination of single or double card
			mtvIMEI1.setText(mIMEI1);
			mtvIMEI2.setText(mIMEI2);
		}else{
			mtvIMEI1.setText(IMEI);// android api
			mtvIMEI2.setText("no second sim.");
		}
		
		/**Get the SW Ver*/
		String systemsw = android.os.Build.DISPLAY;//ROM 
		mtvSWVer.setText(systemsw);
		
		/**Get the BT addr*/
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		String bluetoothAddress = bluetoothAdapter.getAddress();
		mtvBTADDR.setText(bluetoothAddress);
		
		/**Get the WIFI Mac*/
		new MacAsyncTask().execute();
		  
		IBinder binder= ServiceManager.getService("NvRAMAgent");
		NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
		int file_lid=36;//The lid of AP_CFG_REEB_PRODUCT_INFO_LID is 25
		byte[] buff = null;
		try {   
			buff = agent.readFile(file_lid);//read buffer from AP_CFG_REEB_PRODUCT_INFO_LID
			Log.e("chb", "agent_buff:  "+buff);
		} catch (RemoteException e) {
			e.printStackTrace();
		} 
		mtvBarCode.setText(getSerialNumber());
		
    	return getResult();
	}
	
	public  String getSerialNumber(){
        return SystemProperties.get("gsm.serial");
    }
	
	private boolean isLetterOrNumber(char buff)  {
	    return ((buff <= 'Z') && (buff >= 'A')) 
			|| ((buff >= '0') && (buff <= '9')) 
			|| ((buff <= 'z') && (buff >= 'a'));
	  }
	
	/** 
	* @ClassName: MacAsyncTask 
	* @PackageName:com.malata.factorytest.item
	* @Description: TODO ADD Description
	* @Function: TODO ADD FUNCTION
	*/
	class MacAsyncTask extends AsyncTask<String, String, String>{
		private String mMac;
		@Override
		protected String doInBackground(String... params) {
			mMac = NativeWiFiMacMethods.getMacAddr(mContext);
			return null;
		}
		@Override
		protected void onPostExecute(String unused) {
			mtvWiFiMAC.setText(mMac);
		}
	}
	
	
	/** 
	* @Fields: mReceiver 
	* TODO：battert BroadcastReceiver,
	* to get thebattery info :battery health,battery temp,battery states,battery level,
	* Battery charging type,battery technology...
	*/
	BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
			boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
					status == BatteryManager.BATTERY_STATUS_FULL;
			int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
			boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
			boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
			Log.i(TAG, "usbCharge："+usbCharge);
			Log.i(TAG, "acCharge："+acCharge);
			Log.i(TAG, "isCharging："+isCharging);
			if(isCharging && usbCharge) {
				//ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_CLICKABLE); chb deleted
				mtvBatteryChargedState.setBackgroundColor(Color.GREEN);
				mtvBatteryChargedState.setText(R.string.chargingUSB);
				mChargingdState = true;
				Log.i("battery","ischarged" );
			}else if (isCharging && acCharge) {
                                //ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_CLICKABLE); chb deleted
				mtvBatteryChargedState.setBackgroundColor(Color.GREEN);
				mtvBatteryChargedState.setText(R.string.chargingAC);
				mChargingdState = true;
			}			
			else {
				mtvBatteryChargedState.setBackgroundColor(Color.RED);
				mtvBatteryChargedState.setText(R.string.discharging);
				mChargingdState = false;
				
			}	
		}
	};
	
	
	
	/** 
    * @Fields: mUpdateHandler 
    * TODO: Receive messages displayed battery voltage and current information
    */
    @SuppressLint("HandlerLeak")
	public Handler updataBatteryInfoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case EVENT_BATTERY_INFO_UPDATE:
                Bundle b = msg.getData();
                mtvBatteryChargingVoltage.setText(b.getString("VOL"));
                mtvBatteryChargingCurrent.setText(b.getString("CUR"));
                //chb modify for VFOZESGW-75 at 20151016 begin
                if (mtvBatteryChargingVoltage.getText() != "0 mV") {
                	mIsUpdateCurrent = true;
                	Log.i(TAG, "mIsUpdateCurrent:"+mIsUpdateCurrent);
                	Log.i(TAG, "mtvBatteryChargingVoltage.getText():"+mtvBatteryChargingVoltage.getText());
				}
                if (mIsUpdateCurrent && mIsSendMessage) {
                     Message msg1 = new Message();
                     msg1.what = EVENT_PASS_BUTTON_UPDATE;
                     updataBatteryInfoHandler.sendEmptyMessageDelayed(EVENT_PASS_BUTTON_UPDATE,1000);
                     mIsSendMessage = false;
				}
                break;
            case EVENT_PASS_BUTTON_UPDATE:
            	ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_CLICKABLE);
            	break;
             //chb modify for VFOZESGW-75 at 20151016 end
            default:
                break;
            }
        }
    };
	
     /** 
     * @ClassName: FunctionThread 
     * @PackageName:com.malata.factorytest.item
     * @Description: In the Thread,get the battery information
     * @Function: TODO ADD FUNCTION
     * @author:   chehongbin
     * @date:     2015年1月14日 下午5:43:50  
     * Copyright (c) 2015,  Malata All Rights Reserved.
     */
     class FunctionThread extends Thread {
         @Override
         public void run() {
             while (mBatteryThreadruning) {
             	int charingVoltage = 0;
             	int charingCurrent = 0;
             	int batteryCurrent = 0;
             	int batteryVoltage = 0;
             	
             	if (mChargingdState) {
             		//get the battery charging voltage and current  
             		charingVoltage = NativeBatteryMethods.getChargingVoltage();
             		charingCurrent = NativeBatteryMethods.getChargingCurrent();
             		String voltageString = Integer.toString(charingVoltage);
             		String currentString = Integer.toString(charingCurrent);
	         		mBundle.putString("VOL", voltageString+"mV");
					mBundle.putString("CUR", currentString+"mA");
 				}else { 
 					 //if the battery  no charging;the charging voltage and current is 0;  
 					 mBundle.putString("VOL", "0 mV");
 					 mBundle.putString("CUR", "0 mA");
 				}
             	
                 Message msg = new Message();
                 msg.what = EVENT_BATTERY_INFO_UPDATE;
                 msg.setData(mBundle);
                 //sendMessage
                 updataBatteryInfoHandler.sendMessage(msg);
                 //updata 1s one times
                 try {
                     sleep(1*UPDATE_INTERVAL);//UPDATE_INTERVAL = 1000;
                 } catch (InterruptedException e) {
                     Log.e(TAG, "Catch InterruptedException");
                 }
             }
         }
     }
	
	
	/** 
	* @MethodName: getIMEIInfo 
	* @Description:TODO  
	* @return void   
	* @throws 
	*/
 	private void getIMEIInfo(){
		 TelephonyManagerEx mTelephonyManagerEx;
		 mTelephonyManagerEx = TelephonyManagerEx.getDefault();
		 //mIMEI1 = mTelephonyManagerEx.getDeviceId(PhoneConstants.GEMINI_SIM_1);
		 //mIMEI2 = mTelephonyManagerEx.getDeviceId(PhoneConstants.GEMINI_SIM_2);
		 mIMEI1 = mTelephonyManagerEx.getDeviceId(PhoneConstants.SIM_ID_1);
		 mIMEI2 = mTelephonyManagerEx.getDeviceId(PhoneConstants.SIM_ID_2);
	}
	
	/** 
	* @MethodName: isGeminiEnabled 
	* @Description:Single card or double card support methods,
	* 				"FeatureOption.MTK_GEMINI_SUPPORT" -- MTK API  
	* @return boolean    
	* @throws 
	* Copyright (c) 2015,  Malata All Rights Reserved.
	*/
 	public static boolean isGeminiEnabled() {
		return SystemProperties.get("ro.mtk_gemini_support").equals("1");
	}
	
	 
	@Override
	public void onStop() {
		super.onStop();
		mChargingdState = false;
		//unregistration when destroyed
		if(null != mBatteryReceiver) {
			mContext.unregisterReceiver(mBatteryReceiver); 
		} else {
			
		}
		//chb add for LFZS-154 begin
		if (btAdapter.getState() == 12) {
			mBtAdmin.closeBT();
		}
		//chb add for LFZS-154 end
	}
	
	@Override
    public void onPause() {
        super.onPause();
        //stop FunctionThread when the activity stop
        mBatteryThreadruning = false;
    }
	@Override
	public void onResume() {
		super.onResume();
	    //Resume FunctionThread when the activity Resume.
	    mBatteryThreadruning = true;
	    //start FunctionThread to get battery info.
	    new FunctionThread().start();
	    
	    /**Create the battery status changed event listeners of the filter*/
    	IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		mContext.registerReceiver(mBatteryReceiver, ifilter);
		
		mIntentBatteryState = mContext.registerReceiver(mBatteryReceiver, ifilter);
		
		Log.i(TAG, "onresume");
	}
	
}
	 
	 

