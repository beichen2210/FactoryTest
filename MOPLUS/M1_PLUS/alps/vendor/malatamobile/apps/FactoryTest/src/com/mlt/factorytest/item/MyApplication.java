package com.mlt.factorytest.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.gxFP.FingerprintManager;

import com.mlt.factorytest.R;
import com.mlt.factorytest.item.AbsHardware.TestResult;
/**
 * 
 * file name:MyApplication.java
 * Copyright MALATA ,ALL rights reserved
 * 2015-2-11
 * author:laiyang
 * 
 * The Application of FactoryTest,when the program is running,cache the test
 * items and test results 
 * 
 * Modification history
 * -------------------------------------
 * 
 * -------------------------------------
 */
public class MyApplication extends Application {
	
	private static final String TAG = "Application";
	
	/**
	 * save current AutoTest process
	 */
	private static int currentAutoPos;
	private FingerprintManager mFingerprintManager;
	private static MyApplication mApplication;

	public static List<TestResult> getResults() {
		return mResults;
	}
	
	public void setResults(List<TestResult> mAutoResults) {
		mResults = mAutoResults;
	}
	
	private static List<TestResult> mResults;


	public static void setmResults(List<TestResult> mResults) {
		MyApplication.mResults = mResults;
	}

	private static List<String> mHWTexts;
	
	public static List<String> getHWTexts() {
		return mHWTexts;
	}

	public static void setHWTexts(List<String> texts) {
		mHWTexts = texts;
	}

	public static List<String> getHWClassNames() {
		return mHWClassNames;
	}

	public static void setHWClassNames(List<String> classNames) {
		mHWClassNames = classNames;
	}

	private static List<String> mHWClassNames;
	
	/**
	 * get current auto test position
	 * @date 2015-4-11 am 9:33:23
	 * @return
	 */
	public static int getCurrentAutoPos() {
		return currentAutoPos;
	}
	
	//pss add FactoryTest for keyandmotor 20150714 start
	public static String getCurrentTestClassName(int position) {
	    if(mHWClassNames == null || mHWClassNames.size() < currentAutoPos) {
	        return null;
	    }
	    return mHWClassNames.get(position);
	}
	
	public static void saveClassNameUseSharePreference(Context mContext,String mClassName){
	    SharedPreferences share = mContext.getSharedPreferences("FactoryTestSharePreference", 4);
	    Editor editor = share.edit();
	    editor.putString("classname", mClassName);
	    editor.commit();

	}
	
	//pss add FactoryTest for keyandmotor 20150714 end
	/**
	 * set current auto test position
	 * @date 2015-4-11 am 9:34:27
	 * @param currentAutoPos the position to set
	 */
	public static void setCurrentAutoPos(int currentAutoPos) {
		MyApplication.currentAutoPos = currentAutoPos;
	}

	/*
	 * when program start,init AutoTest's data and ManualTest's data
	 * 
	 */
	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate Application");
		
		
		mResults = new ArrayList<TestResult>();
		init();
		currentAutoPos = 0;
		mApplication = this;
		initFpMangerService();
		super.onCreate();
	} 
	
	private void init() {
		mHWTexts = new ArrayList<String>();
		mHWClassNames = new ArrayList<String>();
		// get ItemNames and ClassNames from Arrays.xml
		String[] hwTexts = getResources().getStringArray(R.array.testitems);
		String[] hwClassName = getResources().getStringArray(R.array.testClassNames);
		mHWTexts = Arrays.asList(hwTexts);
		mHWClassNames = Arrays.asList(hwClassName);
		
		for(int i = 0; i < hwTexts.length; i++) {
			mResults.add(TestResult.UnCheck);	
		}
	}

	/**
	 * clear AutoTest's results,set these results uncheck
	 * @date 2015-2-11 am 11:20:42
	 */
	public static void clearResult() {
		int size = mResults.size();
		for(int i = 0; i < size; i++) {
			mResults.set(i, TestResult.UnCheck);
		}
	}
	
	private void initFpMangerService() {
		if (mFingerprintManager == null) {
			mFingerprintManager=FingerprintManager.getFpManager();
		}
	}



	public FingerprintManager getFpServiceManager() {
		if (mFingerprintManager == null) {
			initFpMangerService();
		}
		return mFingerprintManager;
	}

	//FpApplication
	public synchronized static MyApplication  getInstance() {
		return mApplication;
	}
	
}
