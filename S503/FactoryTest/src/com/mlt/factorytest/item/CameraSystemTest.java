package com.mlt.factorytest.item;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mlt.factorytest.R;
import com.mlt.factorytest.ItemTestActivity;

/** 
* @ClassName: CameraSystemTest 
* @PackageName:com.malata.factorytest.item
* @Description: TODO ADD Description
* @Function: TODO ADD FUNCTION
* @author:   chehongbin
* @date:     2015年1月27日 下午1:31:30  
* Copyright (c) 2015 MALATA,All Rights Reserved.
* Modify History
* ---------------------------
* Who	:	chehongbin
* When	:	2015年1月27日
* JIRA	:	
* What	:	ADD text dispaly of sms input number
*/
public class CameraSystemTest extends AbsHardware {
	private String TAG = "CAMERA";
	private Context mContext;
	private TextView mtvBackCameraOpen,mtvFrontCameraOpen,mtvBackCameraTakePicture,mtvFrontCameraTakePicture;
	private Intent mIntent = null;
	private  int mOpenCameraTimes = 2 ;
	private int mCameraPosition;
	
	private boolean mBackCameraOK = false;
	private boolean mFrontCameraOK = false;
	private AlertDialog.Builder mBuilder;
	private static final int PICK_FRONT_CAMERA = 200;
	public static final int  PICK_BACK_CAMERA = 100;
	
	public void onCreate() {
		mBackCameraOK = false;
		mFrontCameraOK = false;
		mOpenCameraTimes = 2;  //Initialize the camera open times
		/**set the pass button can't click*/
		ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_UNCLICKABLE);
		
		if (checkCameraHardware()) { // if camera exited
			if (FindBackCameraExist()) {
				mIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				mIntent.putExtra("autofocus", true); //  autofocus
				mIntent.putExtra("fullScreen", false); // no fullScreen
				mIntent.putExtra("android.intent.extras.CAMERA_FACING", 0);// open back Camera
				/**open camera by Method Call startActivityForResult*/
				ItemTestActivity.itemActivity.startActivityForResult(mIntent, PICK_BACK_CAMERA);
			}else {
				mCameraExitDialog();
				mtvBackCameraTakePicture.setText(R.string.noprocess);
				mtvBackCameraTakePicture.setBackgroundColor(Color.RED);
			}
			
		}else {
			mCameraExitDialog(); //tips
		}
	}
	
	public CameraSystemTest(String text, Boolean visible) {
		super(text, visible);
	}
	
	@Override
	public View getView(Context context) {
		this.mContext = context;
		View view = LayoutInflater.from(context).inflate(R.layout.item_camera_system, null);
		mtvBackCameraOpen = (TextView) view.findViewById(R.id.back_camera_open_tag);
		mtvFrontCameraOpen = (TextView) view.findViewById(R.id.front_camera_open_tag);
		mtvBackCameraTakePicture = (TextView) view.findViewById(R.id.back_camera_takepicture_tag);
		mtvFrontCameraTakePicture = (TextView) view.findViewById(R.id.front_camera_takepicture_tag);
		
		return view;
	}
	
	
	/** 
	* @MethodName: checkCameraHardware 
	* @Functions:Check whether the phone has a camera
	* @return	:boolean   
	*/
	private boolean checkCameraHardware () {
	    if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}
	
	
	/* (non-Javadoc)
	 * @see com.malata.factorytest.item.AbsHardware#onActivityResult(int, int, android.content.Intent)
	 */
	public void onActivityResult (int requestCode, int resultCode,	Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		Log.d(TAG, "requestCode:"+requestCode);
		Log.d(TAG, "resultCode:" +resultCode);
		Log.d(TAG, "REsult_OK:"+Activity.RESULT_OK);
		Log.d(TAG, "intent:"+intent);
		if (requestCode == PICK_BACK_CAMERA) { //open back camera
			mtvBackCameraOpen.setBackgroundColor(Color.GREEN);
			if (resultCode == Activity.RESULT_OK) {
				/*Bundle extras = intent.getExtras();
				Bitmap bmp = (Bitmap) extras.get("data");*/
				//mivBackCameraPicture.setImageBitmap(bmp);
			    //mtvBackCameraTakePicture.setBackgroundColor(Color.GREEN);
				mBackCameraOK = true;
				mtvBackCameraTakePicture.setText(R.string.success);
				mtvBackCameraTakePicture.setBackgroundColor(Color.GREEN);
				//isTestSuccess();
			}
			else {
				mtvBackCameraTakePicture.setText(R.string.fail);
				mtvBackCameraTakePicture.setBackgroundColor(Color.RED);
			}
		}
		
		
		if (requestCode == PICK_FRONT_CAMERA) { //open front camera
			mtvFrontCameraOpen.setBackgroundColor(Color.GREEN);
			if (resultCode == Activity.RESULT_OK) {
				/*Bundle extras = intent.getExtras();
				Bitmap bmp = (Bitmap) extras.get("data");*/
				//mivFrontCameraPicture.setImageBitmap(bmp);
			    //mtvFrontCameraTakePicture.setBackgroundColor(Color.GREEN);
				mtvFrontCameraTakePicture.setText(R.string.success);
				mtvFrontCameraTakePicture.setBackgroundColor(Color.GREEN);
				mFrontCameraOK = true;
				isTestSuccess();
			}else {
				mtvFrontCameraTakePicture.setText(R.string.fail);
				mtvFrontCameraTakePicture.setBackgroundColor(Color.RED);
			}
		}
		
		
		
		//Each open two camera, 
		//open the back camera for the first time,
		//the second open front camera.
		if (FindFrontCameraExist()) {
			if (mOpenCameraTimes > 1) {
				Log.d(TAG, "requestCode2:"+requestCode);
				Log.d(TAG, "resultCode2:" +resultCode);
				Log.d(TAG, "REsult_OK2:"+Activity.RESULT_OK);
				Log.d(TAG, "intent2:"+intent);
				Intent intentfront = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);  
				intentfront.putExtra("android.intent.extras.CAMERA_FACING", 1);
				ItemTestActivity.itemActivity.startActivityForResult(intentfront, PICK_FRONT_CAMERA); 
				mOpenCameraTimes = mOpenCameraTimes - 1;
			}
		}else {
			mCameraExitDialog();// tip
			mtvFrontCameraTakePicture.setText(R.string.noprocess);
			mtvFrontCameraTakePicture.setBackgroundColor(Color.RED);
		}
		
	
	}
	
	
	

	/** 
	* @MethodName: FindFrontCamera 
	* @Functions:Get front-facing camera.
	* @return	:int   
	*/
	@SuppressLint("NewApi")
	private boolean FindFrontCameraExist(){
	    int cameraCount = 0;
	    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
	    cameraCount = Camera.getNumberOfCameras(); // get cameras number
	    for (mCameraPosition = 0; mCameraPosition < cameraCount;mCameraPosition++ ) {
	    	Camera.getCameraInfo( mCameraPosition, cameraInfo ); // get camerainfo
	        if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_FRONT ) { // front camera
	           return  true;//camera exited
	          
	        }
	    }
		return false;
	}
		
	/** 
	* @MethodName: FindBackCamera 
	* @Functions:Get back-facing camera.
	* @return	:int   
	*/
	@SuppressLint("NewApi")
	private boolean FindBackCameraExist(){
	        int cameraCount = 0;
	        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
	        cameraCount = Camera.getNumberOfCameras(); // get cameras number
	        for (mCameraPosition = 0; mCameraPosition < cameraCount;mCameraPosition++ ) {
	            Camera.getCameraInfo( mCameraPosition, cameraInfo ); // get camerainfo
	            if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_BACK ) {  // back camera
	               return  true; //camera exited
	            }
	        }
	    	return false;
	}	
	
	
	private void mCameraExitDialog() {
	    mBuilder = new Builder(mContext);
	    if (!FindBackCameraExist()) {
			mBuilder.setMessage(R.string.bcamera_no_exits);
		} 
	    else if(!FindFrontCameraExist()){
			mBuilder.setMessage(R.string.fcamera_no_exits);
		}
		mBuilder.setTitle(R.string.tip);
		mBuilder.setPositiveButton(R.string.tip_ok, new OnClickListener() {
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
	* @return	:void   
	* @throws 
	*/
	private void isTestSuccess() {
		if (mBackCameraOK && mFrontCameraOK) {
			ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_CLICKABLE);
		}
	}	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mOpenCameraTimes = 2; 
	}
	
}
