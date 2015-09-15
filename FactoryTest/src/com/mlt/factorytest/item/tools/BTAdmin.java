package com.mlt.factorytest.item.tools;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

/** 
* @ClassName: BTAdmin 
* @Description: Bluetooth management class
* @Function: Can get the bluetooth management class instances, and bluetooth opened I and closed
* @author:   peisaisai
* @date:     2015-01-15 14:56:06  
* Copyright (c) 2015,  Malata All Rights Reserved.
*/
public class BTAdmin {
	private Context mContext;
	public BluetoothAdapter mAdapter;
	public BTAdmin(Context mContext){
		this.mContext = mContext;
		mAdapter = getInstance();
	}
	public  BluetoothAdapter getInstance(){
		if (mAdapter==null) {
			mAdapter = BluetoothAdapter.getDefaultAdapter();
		}
		return mAdapter;
	}
	public void openBT(){
		if (mAdapter.getState()==BluetoothAdapter.STATE_OFF) {
			mAdapter.enable();
		}
	}
	public void closeBT(){
		if (mAdapter.getState()==BluetoothAdapter.STATE_ON) {
			mAdapter.disable();
		}
	}
	
	/** 
	* @MethodName: searchBT 
	* @Description:Search for bluetooth devices
	* @return void   
	* @throws 
	* Copyright (c) 2015,  Malata All Rights Reserved.
	*/
	public void searchBT(){
		mAdapter.startDiscovery();
	}
}
