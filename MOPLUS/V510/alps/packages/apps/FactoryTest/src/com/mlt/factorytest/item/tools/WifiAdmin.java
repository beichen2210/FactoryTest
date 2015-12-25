package com.mlt.factorytest.item.tools;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * @ClassName: WifiAdmin
 * @Description: The wifi management class
 * @Function: Scanning control opening and closing of wifi, wifi hotspot around
 *            information, wifi connection specify no password, get connected
 *            wi-fi information, etc
 * @author: peisaisai
 * @date: 2015-01-15 13:07:50 Copyright (c) 2015, Malata All Rights Reserved.
 */
public class WifiAdmin {
	
	// Define WifiManager object
	public WifiManager mWifiManager;
	
	// Define WifiInfo object
	private WifiInfo mWifiInfo;
	
	// Scan the list of network connection
	private List<ScanResult> mWifiList;
	
	// The network connection list
	private List<WifiConfiguration> mWifiConfiguration;
	
	//forget wifi
	private WifiManager.ActionListener mForgetListener; //pss add for VFOZBENQ-140 20150922
	
	public final int SECURITY_NONE = 0;
	public final int SECURITY_WEP = 1;
	public final int SECURITY_PSK = 2;
    /// M: security type @{
	public final int SECURITY_WPA_PSK = 3;
	public final int SECURITY_WPA2_PSK = 4;
	public final int SECURITY_EAP = 5;
	public final int SECURITY_WAPI_PSK = 6;
	public final int SECURITY_WAPI_CERT = 7;
	
	// Define one WifiLock
	WifiLock mWifiLock;
	
	WifiManager getinstance(Context context) {
		if (mWifiManager == null) {
			// acquire WifiManager object
			mWifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
		}
		return mWifiManager;
	}

	
	public WifiAdmin(Context context) {
		// acquire WifiManager pbject
		mWifiManager = getinstance(context);
		// acquire WifiInfo object
		mWifiInfo = mWifiManager.getConnectionInfo();
	}

	
	public void openWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	
	public void closeWifi() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	/** 
	* @MethodName: checkState 
	* @Description:Check the current WIFI state
	* @return  
	* @return int   
	* @throws 
	* Copyright (c) 2015,  Malata All Rights Reserved.
	*/
	public int checkState() {
		return mWifiManager.getWifiState();
	}

	/** 
	* @MethodName: getConfiguration 
	* @Description:Get configured network
	* @return  
	* @return List<WifiConfiguration>   
	* @throws 
	* Copyright (c) 2015,  Malata All Rights Reserved.
	*/
	public List<WifiConfiguration> getConfiguration() {
		return mWifiConfiguration;
	}

	/** 
	* @MethodName: connectConfiguration 
	* @Description:Specify the configured network connection
	* @param index  
	* @return void   
	* @throws 
	* Copyright (c) 2015,  Malata All Rights Reserved.
	*/
	public void connectConfiguration(int index) {
		// The index is greater than the configured network index returns
		if (index > mWifiConfiguration.size()) {
			return;
		}
		// Connection configured network of the specified ID
		mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,
				true);
	}

	public void startScan() {
		mWifiManager.startScan();
		// Scan results
		mWifiList = mWifiManager.getScanResults();
		// Get configured network connection
		mWifiConfiguration = mWifiManager.getConfiguredNetworks();
	}

	// Get the list of network
	public List<ScanResult> getWifiList() {
		return mWifiList;
	}


	public int getWifiState() {
		return mWifiManager.getWifiState();
	}

	// get mac 
	public String getMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	// get BSSID
	public String getBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}

	// get IP
	public int getIPAddress() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	// get ID
	public int getNetworkId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	// get WifiInfo
	public String getWifiInfo() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}

    /**
     * @MethodName: addNetwork
     * @Description: add a network and connect
     * @param wcg
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    public void addNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        // Log.i("peisaisai", "wifi return:"+wcgID);
        boolean b = mWifiManager.enableNetwork(wcgID, true);
        Log.i("peisaisai", "wifi return:" + b);
    }

    public void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    /**
     * @MethodName: CreateWifiInfo
     * @Description:Create a network connection, can only use no password
     * @param SSID
     * @param Password
     * @param Type
     * @return
     * @return WifiConfiguration
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    public WifiConfiguration createWifiInfo(String SSID, String Password,
            int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

		WifiConfiguration tempConfig = this.IsExsits(SSID);
		if (tempConfig != null) {
			mWifiManager.removeNetwork(tempConfig.networkId);
		}

		if (Type == 1) // WIFICIPHER_NOPASS
		{
			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == 2) // WIFICIPHER_WEP
		{
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + Password + "\"";
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == 3) // WIFICIPHER_WPA
		{
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		return config;
	}
	private WifiConfiguration IsExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = mWifiManager
				.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}
		return null;
	}

	//get ssid
	public String getSSID() {
		// TODO Auto-generated method stub
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
	}

	//get level
	public int getLevel() {
		// TODO Auto-generated method stub
		return (Integer) (mWifiInfo.getRssi());
	}
	
	//get speed 
	public int getSpeed() {
		// TODO Auto-generated method stub
		if (mWifiInfo != null) {
			return mWifiInfo.getLinkSpeed();
		}
		return -1;
	}
	
	public  int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WAPI-PSK")) {
            /// M:  WAPI_PSK
            return SECURITY_WAPI_PSK;
        } else if (result.capabilities.contains("WAPI-CERT")) {
            /// M: WAPI_CERT
            return SECURITY_WAPI_CERT;
        } else if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }
	
	//pss add for VFOZBENQ-140 20150922 start 
	public void forget(int netId){
	    mForgetListener = new WifiManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i("pss","success!");
            }
            @Override
            public void onFailure(int reason) {
                Log.i("pss","fail!");
            }
        };
	    mWifiManager.forget(netId, mForgetListener);
	}
	//pss add for VFOZBENQ-140 20150922 end
	
}
