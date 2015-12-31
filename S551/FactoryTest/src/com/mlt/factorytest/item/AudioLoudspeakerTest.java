package com.mlt.factorytest.item;

import android.annotation.SuppressLint;

import android.app.AlertDialog;

import android.app.AlertDialog.Builder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;

import android.content.DialogInterface.OnClickListener;

import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Color;

import android.media.AudioManager;
import android.media.MediaPlayer;

////memoryCard
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import com.mlt.factorytest.R;
import com.mlt.factorytest.ItemTestActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.security.auth.PrivateCredentialPermission;


/**
* @ClassName: AudioLoudspeakerTest
* @PackageName:com.malata.factorytest.item
* @Description: TODO ADD Description
* @Function: TODO ADD FUNCTION
* @author:   chehongbin
* @date:     2015-3-24 ����1:52:29
* Copyright (c) 2015 MALATA,All Rights Reserved.
* Modify History
* ---------------------------
* Who        :        chehongbin
* When        :        2015-3-24
* JIRA        :
* What        :        ADD text dispaly of sms input number
*/
public class AudioLoudspeakerTest extends AbsHardware {
    private static final String TAG = "LOUDSPEAKER";
    private Context mContext;
    private TextView mtvHeadsetstates;
    private TextView mtvPlayMusicExist;
    private boolean mLoudSpeaker = false; // check now is loudspeaker or not
                                          // true  :loudspeaker; false :headset 
    private boolean mHeadsetNormal = false; //check headset normal or not
    private boolean mIsMounted = false; //check the SD Card whether exit 
    private boolean mfileExist = false; //check the flie whether exit 
    private StorageManager mStorageManager = null;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private AlertDialog.Builder mBuilder;
    private int currentVol;
    private int maxVol;
    
    private String path = null;
    
    /** sd card path*/
    private String musicPath = "/storage/sdcard1/"; // music path at sd card S503
    private String musicPath1 = "/storage/sdcard0/"; // music path at sd card s551
    private File file = new File("/storage/sdcard1"); //  s503
    private File file1= new File("/storage/sdcard0");// s551
    
    //different audio format music name
    String[] ext = { "testsong.mp3", "testsong.wav,", "testsong.mid", "testsong.asf", "testsong.mpg",
			"testsong.avi", "testsong.tti", "testsong.aac", "testsong.m4a", "testsong.ogg", "testsong.flac" };                        
    /** 
    * @Fields: headSetReceiver 
    * 						: Receive the headphones plug, and carries on the processing
    * @Description�� TODO��
    */
    BroadcastReceiver headSetReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("Headset", "into headsetreceiver!");
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                    if (intent.getIntExtra("state", 0) == 1) { //headset  has  pluged
                        Log.d("Headset", "this is headphone plugged");
                        mtvHeadsetstates.setText(R.string.headset_connected);
                        mtvHeadsetstates.setBackgroundColor(Color.GREEN); //change the UI
                        mLoudSpeaker = false; //change to earspeaker
                        mHeadsetNormal = true;
                        isTestSuccess();
                    } else {
                        Log.d("Headset", "this is headphone unplugged");
                        mtvHeadsetstates.setText(R.string.headset_unconnected);
                        mtvHeadsetstates.setBackgroundColor(Color.RED); //change the UI
                        mLoudSpeaker = true; //change to loudspeaker
                    }
                }
            }
        };

    public AudioLoudspeakerTest(String text, Boolean visible) {
        super(text, visible);
    }
   
    @Override
    public void onCreate() {
        super.onCreate();
        /**judge headset whether pluged*/
        /**Create the headset plug event listeners of the filter*/
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        mContext.registerReceiver(headSetReceiver, ifilter);
        /** get the addio service */
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMediaPlayer = new MediaPlayer();
        /**set the pass button can't click*/
        ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_UNCLICKABLE);
        //startLoudspeakTest();
        startLoudspeakTest();

        /**get the system  sound size*/
        //currentVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        //startLoudspeakTest(); //To begin testing process, 
                              //first of all to determine whether a SD card.
    }

    @Override
    public View getView(Context context) {
        this.mContext = context;
        View view = LayoutInflater.from(context)
                                  .inflate(R.layout.item_loudspeaker, null);
        mtvHeadsetstates = (TextView) view.findViewById(R.id.headset_states);
        mtvPlayMusicExist = (TextView) view.findViewById(R.id.music_file_exit);
        return view;
    }

    /**
    * @MethodName: mStartLoudspeakTest
    * @Functions:To begin testing process,
    *                         First of all to determine whether a SD card;
    *                         If there is a SD card, to determine whether there is a music file;
    *                         If there is a music file,begin  playing the audio;
    *                         If not just copy the software built-in audio to SD card directory, play again
    * @return        :void
    */
    private void startLoudspeakTest() {
    	SDCardTest();
        if (mfileExist) {
            mtvPlayMusicExist.setText(R.string.exits);
            mtvPlayMusicExist.setBackgroundColor(Color.GREEN);
            playSDCardMusic();
        } else {
        	playRawMusic();
        	mSDExitDialog();
            mtvPlayMusicExist.setText(R.string.file_no_exits);
            mtvPlayMusicExist.setBackgroundColor(Color.RED);
            
        }
    } 
    /**
    * @MethodName: playSDCardMusic
    * @Description:Play music files specified directory;
    *                            Music file exit ,playing "test.mp3"
    * @return void
    */
    public void playSDCardMusic() {
        mAudioManager.setMode(AudioManager.MODE_NORMAL); //Turn into Loudspeaker playing mode

        try {
                try {
                    mMediaPlayer.setDataSource(path); //set the music path
                    mMediaPlayer.setLooping(true); //set play loop	
                  //mMediaPlayer.setVolume(0.9f, 0.9f); // set play music sound size
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
        catch (Exception e) {
        	e.printStackTrace();
        }
     } //end of try first
    
    /** 
	* @MethodName: playmusic 
	* @Description:playing music ,music file at   
	* @return void   
	* @throws 
	*/
	public void playRawMusic() {
		//mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setMode(AudioManager.MODE_NORMAL); //Turn into Loudspeaker playing mode
		try {
			 try {
				 mMediaPlayer = MediaPlayer.create(mContext, R.raw.testsong);
				 mMediaPlayer.setLooping(true); 
				 mMediaPlayer.setVolume(0.9f, 0.9f); // set play music sound size
				 mMediaPlayer.start();
			 } catch (Exception e) {
             	e.printStackTrace();
             }
			 
		}  catch (Exception e) {
         	e.printStackTrace();
         } 
	}
    
       
    /**
    * @MethodName: mSDCardTest
    * @Functions:To traverse the mobile storage system, detection of sd card exists
    * @return        :void
    */
    private void SDCardTest() {
        mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] storageVolumeList = mStorageManager.getVolumeList();
        if (storageVolumeList != null) {
            int mTimes = 3; // find the sd caed
            for (StorageVolume volume : storageVolumeList) {
                try {
                	// SD card  S503/S505 is 2 (/sdcard1);
                	// s551 is 3 (sdcard0)  
                    if (mTimes == 2 || mTimes ==3) { 
                        mIsMounted = isMounted(volume.getPath()); //sd  exist? return true!
                    }
                    mTimes--;
                } catch (Exception e) {
                }
            }
            if (mIsMounted = true) {
            	searchMusic(); //serch music
            			
			}
        }
    }

    /**
     * This method checks whether SDcard is mounted or not
     * @param mountPoint    the mount point that should be checked
     * @return true if  SDcard is mounted, false otherwise
     */
    protected boolean isMounted(String mountPoint) {
        String state = null;
        state = mStorageManager.getVolumeState(mountPoint);
        return Environment.MEDIA_MOUNTED.equals(state);
    }
    
	/** 
	* @MethodName: searchMusic 
	* @Functions:TODO serch music 
	* @return	:void   
	* @throws 
	*/
	private void searchMusic() {
		for (int i = 0; i < ext.length; i++) {
			File mFile = new File(file+"/"+ext[i]);
			File mFile1 = new File(file1+"/"+ext[i]);
			Log.e(TAG, mFile+"");
			Log.e(TAG, mFile1+"");
			if (mFile.exists()) {
				mfileExist =true;
				path = musicPath+ext[i];
				Log.e(TAG, path+"");
				break;
			}
			else if (mFile1.exists()){
				mfileExist =true;
				path = musicPath1+ext[i];
				Log.e(TAG, path+"");
			}
			else {
				Log.e(TAG,"music file no exists!");
			}
		}
	}
    
    
 
    /**
    * @MethodName: mSDExitDialog
    * @Functions:Dialog:
    *                          when the sd card does not exist,
    *                         or audio file does not exist, the user is prompted
    * @return        :void
    */
    private void mSDExitDialog() {
        mBuilder = new Builder(mContext);
        mBuilder.setMessage(R.string.SDcard_no_exits);
        mBuilder.setTitle(R.string.tip);
        mBuilder.setPositiveButton(R.string.tip_ok,
            new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //do something , When you click on the Dialog button
                }
            });
        mBuilder.create().show();
    }

    /**
    * @MethodName: isTestSuccess
    * @Functions: Whether the test pass
    * @return        :void
    */
    private void isTestSuccess() {
        if (mHeadsetNormal && file.exists()) {
            ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_CLICKABLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /**Recovery system sound size*/
        //  mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVol, AudioManager.FLAG_PLAY_SOUND);
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }

        mContext.unregisterReceiver(headSetReceiver); //Exit and unregistration
    }
}
