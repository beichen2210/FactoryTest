package com.mlt.factorytest.item;


import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;

import com.mlt.factorytest.R;

/** 
* @ClassName: AudioRefMicTest 
* @PackageName:com.malata.factorytest.item
* @Description: TODO ADD Description
* @Function: TODO ADD FUNCTION
* @author:   chehongbin
* @date:     2015骞�鏈�7鏃�涓嬪崍1:31:21  
* Copyright (c) 2015 MALATA,All Rights Reserved.
* Modify History
* ---------------------------
* Who	:	chehongbin
* When	:	2015骞�鏈�7鏃�
* JIRA	:	
* What	:	ADD text dispaly of sms input number
*/  
public class AudioRefMicTest extends Activity {
	//private Context context;
	//private TextView tvMainMic;
	private AudioManager mAudioManager = null;
	private boolean mRunning = false;
	//private boolean soundeffect = false;
	//private boolean isSubMicThread = true;
	//private boolean hasTest = false;
	private static final String TAG = "AudioRefMicLoopTest";
	//private int isHeadsetConnect;

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audiorefmictest);
		//tvMainMic =(TextView) findViewById(R.id.textview_main);
		this.mAudioManager = ((AudioManager)getSystemService("audio"));
		
	}
	
	/*class loopRecord extends Thread{
		@Override
		public void run() {
			  try   {
		          Thread.sleep(1000L);
		        	if (MainActivity.this.running){
		        		//hasTest =true;
		        		if (isSubMicThread) {
		        			if (MainActivity.this.soundeffect == true);
		        			MainActivity.this.mAudioManager.setParameters("SET_LOOPBACK_TYPE=25");
						}
		          }
		          return;
		       }catch (InterruptedException localInterruptedException) {
		        	
		       }
		}
	}*/
	
	

	@Override
	protected void onResume() {
		super.onResume();
	    this.mRunning = true;
	    new Thread() {
	    	public void run() {
	        try {
	          Thread.sleep(1000L);
	          Log.i(TAG, "audioRefMicTest thread!");
	          if (AudioRefMicTest.this.mRunning) {
	        	  //if (MainActivity.this.soundeffect == true);
	        	  //淇敼涓哄壇楹﹀彂澹�
	        	  mAudioManager.setParameters("SET_LOOPBACK_TYPE=25");
	          }
	          return;
	        } catch (InterruptedException localInterruptedException) {
	        	
	        }
	      }
	    }
	    .start();
	}
		

	
	@Override
	protected void onPause() {
		
		super.onPause();
	    this.mRunning = false;
	    this.mAudioManager.setParameters("SET_LOOPBACK_TYPE=0");
	}
	
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		this.mRunning = false;
	    this.mAudioManager.setParameters("SET_LOOPBACK_TYPE=0");
	}

}
