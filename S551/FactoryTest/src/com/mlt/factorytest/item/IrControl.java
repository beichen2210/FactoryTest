package com.mlt.factorytest.item;

import java.lang.reflect.Method;

import com.mlt.factorytest.R;

import android.content.Context;
import android.util.Log;

/**
 * @ClassName: IrControl
 * @Description:This class is used to obtain reflection method
 * @Function: obtain reflection method
 * @author: huangguoxiong
 * @date: 2015-2-11 am10:00:17 Copyright (c) 2015, Malata All Rights Reserved.
 */
public class IrControl {

    public static final String TAG = "RC/Native";
    // Used to retrieve the underlying service
    public static Object mObject;
    // For transmitting method
    public static Method mMethodTransmit;
    // Access to stop transmitting methodֹ
    public static Method mMethodStop;

    // static {
    //
    // System.loadLibrary("aremote");
    //
    // }
    /**
     * The underlying mapping method
     * */
    public IrControl(Context context) {
        try {
            IrControl.mObject = context.getSystemService("remoteir");
            IrControl.mMethodTransmit = mObject.getClass().getMethod(
                    "transmit", new Class[] { byte[].class, int.class });
            IrControl.mMethodStop = mObject.getClass().getMethod(
                    "cancelTransmit", new Class[0]);
        } catch (Exception e) {
        }
        if (mObject == null) {
            Log.i(TAG, "objIR is null");
        }
        if (mMethodTransmit == null) {
            Log.i(TAG, "method_transmit is null");
        }
        if (mMethodStop == null) {
            Log.i(TAG, "method_stop is null");
        }

    }

    private byte[] buildBuffer(int[] frame, boolean isRepeatMode) {
        int frequency = frame[0];

        int size = ((frame.length - 1) * 2) + 4;
        if (isRepeatMode)
            size += 2;

        byte[] buffer = new byte[size];
        int idx = 0;

        buffer[idx++] = isRepeatMode ? (byte) 0x80 : 0x00;
        buffer[idx++] = (byte) ((frequency >> 16) & 0xff);
        buffer[idx++] = (byte) ((frequency >> 8) & 0xff);
        buffer[idx++] = (byte) (frequency & 0xff);

        if (isRepeatMode) {
            buffer[idx++] = 0x00;
            buffer[idx++] = 0x00;
        }

        for (int i = 1; i < frame.length; i++) {
            buffer[idx++] = (byte) ((frame[i] >> 8) & 0xff);
            buffer[idx++] = (byte) (frame[i] & 0xff);
        }

        return buffer;
    }

    /**
     * To Send data
     * */
    public int sendIR(int[] frame, boolean isRepeatMode) {
        if (mObject == null || mMethodTransmit == null)
            return -1;

        int ret = 0;
        try {
            byte[] buffer = buildBuffer(frame, isRepeatMode);
            ret = (Integer) mMethodTransmit.invoke(mObject, buffer,
                    buffer.length);
        } catch (Exception e) {
        }

        return ret;
    }

    /**
     * Stop sending data
     */
    public int stopIR() {
        if (mObject == null || mMethodTransmit == null)
            return -1;
        int ret = 0;
        try {
            ret = (Integer) mMethodStop.invoke(mObject);
        } catch (Exception e) {
        }

        return ret;
    }
}
