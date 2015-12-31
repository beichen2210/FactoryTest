package com.mlt.factorytest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.WindowManager;

import com.android.internal.os.storage.ExternalStorageFormatter;
import com.mlt.factorytest.R;

public class RebootActivity extends Activity {
	private Context mContext ;
	private boolean DIALOG_FEATURE = true;
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item_reboot);
		setTitle(R.string.bt_reboot);
		//Intent intent = new Intent(TelephonyIntents.SECRET_CODE_ACTION,
        //        Uri.parse("android_secret_code://" + "119"));
		//this.sendBroadcast(intent);
//	    Intent localIntent = new Intent();
//	    localIntent.setClassName("com.android.settings", "com.android.settings.MasterClear");
//	    startActivity(localIntent);
		
//		ComponentName cName = new ComponentName("com.android.settings","com.android.settings.MasterClear"); 
//		Intent intent2 = new Intent(Intent.ACTION_MAIN);
//		intent2.setComponent(cName);
//		this.startActivity(intent2);
		
//		Intent intent = new Intent("com.android.internal.os.storage.FORMAT_AND_FACTORY_RESET");
//        ComponentName COMPONENT_NAME
//                = new ComponentName("android", "com.android.internal.os.storage.ExternalStorageFormatter");
//            intent.setComponent(COMPONENT_NAME);
//            startService(intent);
		//resetFactory();
	} 
	
	
	
//	public void factoryreboot() {
//		if(DIALOG_FEATURE){
//	         AlertDialog dialog = new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.reset_factory_title))
//	         .setMessage(mContext.getString(R.string.reset_factory_context))
//	         .setPositiveButton(mContext.getString(R.string.reset_factory_ok), mOkclick)
//	         .setNegativeButton(mContext.getString(R.string.reset_factory_cancel), null)
//	         .create();
//	         dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);                  
//	         dialog.show();  
//	     }else{
//	         resetFactory();
//	     }
//		
//		
//	}
	
	
//	private void factoryreboot() {
//	    mBuilder = new Builder(mContext);
//	    
//			mBuilder.setMessage(R.string.reset_factory_context);
//		
//			mBuilder.setTitle(R.string.reset_factory_title);
//			mBuilder.setPositiveButton(R.string.reset_factory_ok, new OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				dialog.dismiss();
//				//do something , When you click on the Dialog button
//				 resetFactory();
//			}
//		});
//			mBuilder.setNegativeButton(mContext.getString(R.string.reset_factory_cancel), null);
//			mBuilder.create().show();
//	    }
	   
	public void mOnClickListener(View view)	{ 
			resetFactory();
	};    
	
	 

	    
	private void resetFactory(){
		 Intent intent = new Intent("com.android.internal.os.storage.FORMAT_AND_FACTORY_RESET");
	     ComponentName COMPONENT_NAME
	                = new ComponentName("android", "com.android.internal.os.storage.ExternalStorageFormatter");
	     intent.setComponent(COMPONENT_NAME);
	     startService(intent);
	}
	
}










/*import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class RebootActivity extends Activity {
	private static final String TAG = "RecoverySystem";
	static final int RESULT_ENABLE = 1;
	DevicePolicyManager mDPM;
	ComponentName rebootReceiver;
	private CheckBox ckReboot ,disckReboot ;
	private Button mWipeDataButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		rebootReceiver = new ComponentName(RebootActivity.this,
				RebootRecevier.class);
		
		setContentView(R.layout.reboot);
		
		
		ckReboot = (CheckBox) findViewById(R.id.reboot_checkBox);
		ckReboot.setOnCheckedChangeListener(EnablerebootListener);
		
		disckReboot = (CheckBox) findViewById(R.id.disreboot_checkBox);
		disckReboot.setOnCheckedChangeListener(DisablerebootListener);
		
		mWipeDataButton = (Button) findViewById(R.id.reboot_button);
		mWipeDataButton.setOnClickListener(mWipeDataListener);
		
		
	}
	
	
	void updateButtonStates() {
		boolean active = mDPM.isAdminActive(rebootReceiver);
		if (active) {
			mWipeDataButton.setEnabled(true);
			ckReboot.setEnabled(false);
			disckReboot.setEnabled(true);
			
		} else {
			mWipeDataButton.setEnabled(false);
			ckReboot.setEnabled(true);
			disckReboot.setEnabled(false);
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case RESULT_ENABLE:
			if (resultCode == Activity.RESULT_OK) {
				Log.i("RebootReceiver", "Admin enabled!");
			} else {
				Log.i("RebootReceiver", "Admin enable FAILED!");
			}
			return;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	
	private OnCheckedChangeListener EnablerebootListener =  new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			// Launch the activity to have the user enable our admin.
			Intent intent = new Intent(	DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
					rebootReceiver);
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
					R.string.reboot_message);
			startActivityForResult(intent, RESULT_ENABLE);
			
		}
	};
	
	
	private OnCheckedChangeListener  DisablerebootListener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			mDPM.removeActiveAdmin(rebootReceiver);
			updateButtonStates();
			
		}
	}; 

	private OnClickListener mWipeDataListener = new OnClickListener() {
		public void onClick(View v) {

			boolean active = mDPM.isAdminActive(rebootReceiver);
			if (active) {
				// 恢复出厂
				 mDPM.wipeData(0);
			}

		}
	};

	
	
	
	
	@Override
	protected void onResume() {
		super.onResume();

		updateButtonStates();
	}
	
	
	
	
}*/
