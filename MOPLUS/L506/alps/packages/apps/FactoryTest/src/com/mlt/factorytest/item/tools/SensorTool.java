package com.mlt.factorytest.item.tools;

import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorTool {
	
	private static List<Sensor> sensors = null;
	
	private SensorTool() {
		
	}
	/**
	 * if the device has the type of sensor, return true,else return false
	 * 
	 * @date 2015-4-1 pm 2:48:14
	 * @param manager
	 * @param type
	 * @return
	 */
	public static boolean hasSensor(SensorManager manager, int type) {
		if(sensors == null) {
			sensors = manager.getSensorList(Sensor.TYPE_ALL);
		}
		Log.i("tag",""+sensors);
		for(Sensor sensor : sensors) {
			if(type == sensor.getType()) {
				return true;
			}
		}
		return false;
	}
}
