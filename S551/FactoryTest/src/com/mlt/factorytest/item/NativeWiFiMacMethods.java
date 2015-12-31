package com.mlt.factorytest.item;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/** 
* @ClassName: NativeWiFiMacMethods 
* @PackageName:com.malata.factorytest.item
* @Function: Get wifi Mac,though the jni library。
* @author:   chehongbin
* @date:     2015-2-2 下午4:56:07  
* Copyright (c) 2015 MALATA,All Rights Reserved.
* 
* Modify History
* ---------------------------
* Who	:	chehongbin
* When	:	2015-2-2
* JIRA	:	
* What	:	ADD text dispaly of sms input number
*/
public class NativeWiFiMacMethods {
	
	static{
		System.loadLibrary("macAddrJni"); //wifi mac jni lib 
	}
	
	public static native String getMacAddr(Context context);
	
	public String getLocalMacAddress(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}

}
