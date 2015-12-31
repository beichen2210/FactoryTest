package com.mlt.factorytest.item;

import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;

/** 
* @ClassName: IrControl 
* @Description:This class is used to obtain reflection method
* @Function: obtain reflection method
* @author:   huangguoxiong
* @date:     2015-2-11  am10:00:17  
* Copyright (c) 2015,  Malata All Rights Reserved.
*/
public class IrControl { 

	public static final String TAG = "RC/Native";
	//Used to retrieve the underlying service 
	public static Object objir;
	// For transmitting method
	public static Method method_transmit;
	// Access to stop transmitting methodֹ
	public static Method method_stop;

//	static {
//
//		System.loadLibrary("aremote");
//
//	}
	
	public IrControl(Context context) {
		try {
			IrControl.objir = context.getSystemService("remoteir");
			IrControl.method_transmit = objir.getClass().getMethod("transmit",
					new Class[] { byte[].class, int.class });
			IrControl.method_stop = objir.getClass().getMethod("cancelTransmit",
					new Class[0]);
		} catch (Exception e) {
		}
		if (objir == null) {
			Log.i(TAG, "objIR is null");
		}
		if (method_transmit == null) {
			Log.i(TAG, "method_transmit is null");
		}
		if (method_stop == null) {
			Log.i(TAG, "method_stop is null");
		}

	}
	
	 private byte[] buildBuffer(int[] frame, boolean isRepeatMode){
	        int frequency = frame[0];

	        int size = ((frame.length - 1) * 2) + 4;
	        if(isRepeatMode) size += 2;

	        byte[] buffer = new byte[size];
	        int idx = 0;

	        buffer[idx++] = isRepeatMode ? (byte)0x80 : 0x00;
	        buffer[idx++] = (byte)((frequency >> 16) & 0xff);
	        buffer[idx++] = (byte)((frequency >> 8) & 0xff);
	        buffer[idx++] = (byte)(frequency & 0xff);

	        if(isRepeatMode){
	            buffer[idx++] = 0x00;
	            buffer[idx++] = 0x00;
	        }

	        for(int i=1;i<frame.length;i++){
	            buffer[idx++] = (byte)((frame[i] >> 8) & 0xff);
	            buffer[idx++] = (byte)(frame[i] & 0xff);
	        }

	        return buffer;
	    }



	    /** 
	    * @MethodName: sendIR 
	    * @Description:To send data
	    * @param frame
	    * @param isRepeatMode
	    * @return  
	    * @return int   
	    * @throws 
	    * Copyright (c) 2015,  Malata All Rights Reserved.
	    */
	    public int sendIR(int[] frame, boolean isRepeatMode){
	    	 Log.i(TAG, "sendIR()");
	        if(objir == null || method_transmit == null) return -1;

	        int ret = 0;
	        try {
	            byte[] buffer = buildBuffer(frame, isRepeatMode);
	            ret = (Integer)method_transmit.invoke(objir, buffer, buffer.length);
	        }catch (Exception e){}

	        return ret;
	    }



	    /** 
	    * @MethodName: stopIR 
	    * @Description:Stop sending data 
	    * @return  
	    * @return int   
	    * @throws 
	    * Copyright (c) 2015,  Malata All Rights Reserved.
	    */
	    public int stopIR(){
	    	Log.i(TAG,"stopIR()");
	        if(objir == null || method_transmit == null) return -1;

	        int ret = 0;
	        try {
	            ret = (Integer)method_stop.invoke(objir);
	        }catch (Exception e){}

	        return ret;
	    }


}
