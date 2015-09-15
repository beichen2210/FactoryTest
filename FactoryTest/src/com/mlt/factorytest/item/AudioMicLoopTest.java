package com.mlt.factorytest.item;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mlt.factorytest.ItemTestActivity;
import com.mlt.factorytest.R;

/** 
* @ClassName: Ringtone 
* @Description: Ringtone and Mic Test
* @author:   chehongbin
* @date:     2015年1月13日  下午7:05:22  
* Copyright (c) 2015,  Malata All Rights Reserved.
* Modify History
* ---------------------------
* Who	:	chehongbin
* When	:	2015-8-6
* JIRA	:	LFZS-137
* What	:	Receiver test, for the first time into the silent, the actual sound is very small, enter normal again.
*/
public class AudioMicLoopTest  extends AbsHardware{
	
	private Context context;
	private TextView mHeadsetout;
	
	/** Called when the activity is first created. */
	boolean isRecording = false;//Whether recording mark
	static final int frequency = 44100;
	@SuppressWarnings("deprecation")
	static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	int recBufSize,playBufSize;
	AudioRecord audioRecord;
	AudioTrack audioTrack;
	AudioManager audioManager;

	private int max; //ringtone max voice
	private int current;// ringtone current voice
	private int syscurrent;// ringtone current voice
	private int sysMax;// ringtone current voice
	
	public AudioMicLoopTest(String text, Boolean visible) {
		super(text, visible);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		recBufSize = AudioRecord.getMinBufferSize(frequency,channelConfiguration, audioEncoding);

		playBufSize=AudioTrack.getMinBufferSize(frequency,channelConfiguration, audioEncoding);
			
		audioManager = (AudioManager) ItemTestActivity.itemActivity.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setSpeakerphoneOn(false);   
		audioManager.setMode(AudioManager.MODE_IN_CALL);// 把模式调成听筒放音模式
			
		max = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
		current = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
			
		sysMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
		syscurrent = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
			
			
		/**set to maximum volume*/
		audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, max,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, sysMax,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_RAISE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		audioManager.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			
		// -----------------------------------------
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,channelConfiguration, audioEncoding, recBufSize);
		audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, frequency,channelConfiguration, audioEncoding,playBufSize, AudioTrack.MODE_STREAM);
		//------------------------------------------
		isRecording = true;
		
	}
	
	// chb modify for LFZS-119:factorytest add headset out tip 2015.7.23 begin 
	BroadcastReceiver headSetBroadcastReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
              String action = intent.getAction();
              if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                  if (intent.getIntExtra("state", 0) == 1) {
                      mHeadsetout.setVisibility(View.VISIBLE);
                 } else {
                      mHeadsetout.setVisibility(View.INVISIBLE);
                  }
              }
		}
	};
	// chb modify for LFZS-119:factorytest add headset out tip 2015.7.23 end

	@Override
	public void onResume() {
		super.onResume();
		// chb modify for LFZS-119:factorytest add headset out tip 2015.7.23 begin
		 /**Create the headset plug event listeners of the filter*/
		  IntentFilter ifilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		  context.registerReceiver(headSetBroadcastReceiver, ifilter);
		// chb modify for LFZS-119:factorytest add headset out tip 2015.7.23 end
		isRecording = true;
		new RecordPlayThread().start();// 开一条线程边录边放
		audioTrack.setStereoVolume(1f, 1f);//设置当前音量大小
	}
	
	
	
	@SuppressLint("InflateParams")
	@Override
	public View getView(Context context) {
		this.context = context;
		View view = LayoutInflater.from(context)
                .inflate(R.layout.item_ringtone, null);
		// chb modify for LFZS-119:factorytest add headset out tip 2015.7.23 begin
		mHeadsetout = (TextView) view.findViewById(R.id.headsetout_tip);
		// chb modify for LFZS-119:factorytest add headset out tip 2015.7.23 end
		return view;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		isRecording = false;
		//android.os.Process.killProcess(android.os.Process.myPid());//kill recording  thread
		//Setting the volume to before.
		audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, current,
				AudioManager.USE_DEFAULT_STREAM_TYPE);
		audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, syscurrent,
				AudioManager.USE_DEFAULT_STREAM_TYPE);
		audioManager.setMode(AudioManager.MODE_NORMAL);
	}
	
	// chb modify for LFZS-119:factorytest add headset out tip 2015.7.23 begin
	@Override
	public void onStop() {
		super.onStop();
		isRecording = false;
		context.unregisterReceiver(headSetBroadcastReceiver); //when stop the activity; unregister the headsetReceiver
	}
	// chb modify for LFZS-119:factorytest add headset out tip 2015.7.23 end
	
	@Override
	public void onPause() {
		super.onPause();
		isRecording = false;
	}
	/** 
	* @ClassName: RecordPlayThread 
	* @PackageName:com.mlt.factorytest.item
	* @Description: Record Thread
	*/
	class RecordPlayThread extends Thread {
		public void run() {
			try {
				byte[] buffer = new byte[recBufSize];
				audioRecord.startRecording();//Start Recording
				Thread.sleep(400);
				audioTrack.play();//Start playing
				
				while (isRecording) {
					//MIC stored data to the buffer
					int bufferReadResult = audioRecord.read(buffer, 0,recBufSize);
					byte[] tmpBuf = new byte[bufferReadResult];
					System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
					//Write data  begin play
					audioTrack.write(tmpBuf, 0, tmpBuf.length);
				}
				audioTrack.stop();
				audioRecord.stop();
			} catch (Throwable t) {
			}
		}
	};
}



