package com.mlt.factorytest.item.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

/**
 * @ClassName: GPSAdmin
 * @Description:GPS management class
 * @Function: Can the GPS management class instances, as well as to monitor
 *            whether the GPS to search satellite and satellite state changes,
 *            GPS control opening and closing.
 * @author: peisaisai
 * @date: 2015-01-15 14:57:52 Copyright (c) 2015, Malata All Rights
 *        Reserved.
 */
public class GPSAdmin {
	private LocationManager mLocationManager;
	private Context mContext;
	private Handler mHandler;
	private LocationListener mLocationListener;
	
	//This message is used to display the GPS are initialized
	private final int MSG_UPDATE_INIT_GPS = 13;
	
	//This message is used to display the GPS satellite status change
	private final int MSG_UPDATE_GPS_LISTVIEW = 15;
	
	public GPSAdmin(Context context) {
		this.mContext = context;
		this.mHandler = null;
		mLocationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		initGPS();
	}

	public GPSAdmin(Context mContext, Handler mHandler) {
		this.mContext = mContext;
		this.mHandler = mHandler;
		mLocationManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
		initGPS();

	}

	/** 
	* @MethodName: initGPS 
	* @Description:  Initialize the GPS, which open the GPS monitoring, as well as the GPS satellite state monitoring
	* @return void   
	* @throws 
	* Copyright (c) 2015,  Malata All Rights Reserved.
	*/
	private void initGPS() {
		mLocationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {

				//Log.i("pss", "onLocationChanged");
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				//Log.i("pss", "onStatusChanged");
			}

			@Override
			public void onProviderEnabled(String provider) {
				//Log.i("pss", "onProviderEnabled");
			}

			@Override
			public void onProviderDisabled(String provider) {
				//Log.i("pss", "onProviderDisabled");
			}

		};

		// Monitored satellite state changes
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000, 1, mLocationListener);

		mLocationManager.addGpsStatusListener(new GpsStatus.Listener() {
			
			@Override
			public void onGpsStatusChanged(int event) {
				// TODO Auto-generated method stub
				GpsStatus status = mLocationManager.getGpsStatus(null);
				updateGpsStatus(event, status);
			}
		});
	}

    public void openGps() {
        boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(
                mContext.getContentResolver(), LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            // open GPS
            Settings.Secure.setLocationProviderEnabled(
                    mContext.getContentResolver(),
                    LocationManager.GPS_PROVIDER, true);
            Message msg = new Message();
            msg.arg1 = MSG_UPDATE_INIT_GPS;
            mHandler.sendMessage(msg);
        }
    }

    public void closeGps() {
        boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(
                mContext.getContentResolver(), LocationManager.GPS_PROVIDER);
        if (gpsEnabled) {
            // closeGPS
            Settings.Secure.setLocationProviderEnabled(
                    mContext.getContentResolver(),
                    LocationManager.GPS_PROVIDER, false);
        }
    }

	private List<GpsSatellite> mSatelliteList = new ArrayList<GpsSatellite>();
	/**
	 * @MethodName: getSateliteList
	 * @Description:Get the current search to the satellite information
	 * @return
	 * @return List<GpsSatellite>
	 * @throws Copyright
	 *             (c) 2015, Malata All Rights Reserved.
	 */
	public List<GpsSatellite> getSateliteList() {
		return mSatelliteList;
	}
	
	public void clearSateliteList(){
		mSatelliteList.clear();
	}

	/**
	 * @MethodName: updateGpsStatus
	 * @Description:Satellite state information change, can refresh the
	 *                        information, and send a message to the main
	 *                        thread, gps_list refresh
	 * @param event
	 * @param status
	 * @return
	 * @return List<GpsSatellite>
	 * @throws Copyright
	 *             (c) 2015, Malata All Rights Reserved.
	 */
	private List<GpsSatellite> updateGpsStatus(int event, GpsStatus status) {

		if (status == null) {

        } else if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {

        } else if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            int maxSatellites = status.getMaxSatellites();
            Iterator<GpsSatellite> it = status.getSatellites().iterator();
            mSatelliteList.clear();
            int count = 0;
            while (it.hasNext() && (count <= maxSatellites)) {
                GpsSatellite s = it.next();
                mSatelliteList.add(s);
                count++;
            }
        }
        if (mHandler != null) {
            Message msg = new Message();
            msg.arg1 = MSG_UPDATE_GPS_LISTVIEW;
            mHandler.sendMessage(msg);
        }
        return mSatelliteList;
    }

}
