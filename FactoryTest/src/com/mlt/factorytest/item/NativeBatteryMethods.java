package com.mlt.factorytest.item;

/** 
* @ClassName: NativeBatteryMethods 
* @PackageName:com.malata.factorytest.item
* @Function: Get the battery information:charging voltage and current.
* @author:   chehongbin
* @date:     2015-2-2 下午4:57:17  
* Copyright (c) 2015 MALATA,All Rights Reserved.
* 
* Modify History
* ---------------------------
* Who	:	chehongbin
* When	:	2015-2-2
* JIRA	:	
* What	:	ADD text dispaly of sms input number
*/
public class NativeBatteryMethods {
	  
	// get battery charging voltage
	public static final native int getChargingVoltage();
	// get battery charging current
	public static final native int getChargingCurrent();
	public static final native int getChargingCurrent1();
	// get battery  voltage
	public static final native int getVoltage();
	// get battery  ad current
	public static final native int getCurrent();
	// get battery  current
	public static final native int getFGCurrent();
	
	//load jni library
	static {
		try {
			System.loadLibrary("BatteryJni");
		} catch(UnsatisfiedLinkError e) {
			e.printStackTrace();
		}
	}
}
