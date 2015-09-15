package com.mlt.factorytest.item.tools;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.mlt.factorytest.item.AbsHardware.TestResult;
import com.mlt.factorytest.item.MyApplication;

/**
 * 
 * file name:SaveStatusTool.java
 * Copyright MALATA ,ALL rights reserved
 * 2015-1-31
 * author:laiyang
 * 
 * The class is a tool to save test status,save in SharedPreference
 * Sharedpreference file name is "TestResult"
 * 
 * Modification history
 * -------------------------------------
 * 
 * -------------------------------------
 */
public class SaveStatusTool {
	// sharedpreferece's file name 
	public final static String SP_FILE_NAME = "TestResult"; 
	// test result is pass
	public final static int TEST_PASS = 1;
	// test result is uncheck
	public final static int TEST_UNCHECK = 0;
	// Test result is fail
	public final static int TEST_FAIL = 2;

	/**
	 * get SharedPreference and save test results
	 * @date 2015-4-13 pm 2:44:29
	 * @param context
	 */
	public static void saveTestResults(Context context) {
		// get SharedPreference's editor
		Editor editor = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE).edit();
		saveResults(MyApplication.getResults(), editor);
		// commit datas
		editor.commit();
	}   
	/**
	 * save test results
	 * @date 2015-4-13 pm 3:11:33
	 * @param results
	 * @param editor
	 */
	private static void saveResults(List<TestResult> results, Editor editor) {
		int keyIndex = 0;
		String key = "";
		for(TestResult result :results) {
			key = keyIndex + "";
			// get test result.
			if(TestResult.Fail == result) {// test fail
				editor.putInt(key, TEST_FAIL);
			} else if(TestResult.Pass == result) {// test pass
				editor.putInt(key, TEST_PASS);
			} else {// test uncheck
				editor.putInt(key, TEST_UNCHECK);
			}
			keyIndex++;
		}
	}
	
	/**
	 * read test results from SharedPreferences,read into MyApplication.results  
	 * @date 2015-4-13 pm 3:12:01
	 * @param context
	 */
	public static void readResults(Context context) {
		SharedPreferences sp = context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
		int size = MyApplication.getResults().size();
		List<TestResult> results = MyApplication.getResults();
		String key = "";
		int result = 0;
		for(int i = 0; i < size; i++) {
			key = i + "";
			result = sp.getInt(key, 0);
			if(result == TEST_FAIL) {
				results.set(i, TestResult.Fail);
			} else if(result == TEST_PASS) {
				results.set(i, TestResult.Pass);
			} else if(result == TEST_UNCHECK){
				results.set(i, TestResult.UnCheck);
			}
		}
	}
}
