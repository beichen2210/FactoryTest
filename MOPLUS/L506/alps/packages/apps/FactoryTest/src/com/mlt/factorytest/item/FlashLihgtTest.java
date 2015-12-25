package com.mlt.factorytest.item;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Camera;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.mlt.factorytest.R;
import com.mlt.factorytest.ItemTestActivity;

/**
* @ClassName: FlashLihgtTest
* @PackageName:com.malata.factorytest.item
* @Description: TODO ADD Description
* @Function: TODO ADD FUNCTION
* @author:   chehongbin
* @date:     2015年1月27日 下午1:31:37
* Copyright (c) 2015 MALATA,All Rights Reserved.
* Modify History
* ---------------------------
* Who        :        chehongbin
* When        :        2015年1月27日
* JIRA        :
* What        :        ADD text dispaly of sms input number
*/
public class FlashLihgtTest extends AbsHardware {
    private Button mbtnFlashButton;
    private TextView mtvFrontFlash;
    private TextView mtvBackFlash;
    private Camera camera = null;
    private Camera.Parameters param;
    boolean isflash = false; //the boolean ,save the flash light had open status.
    private int mCameraPosition;
    private boolean mCameraExit = false;
    private boolean mIsBackCamera = true;
    //chb modify for LFZS-132 2015-7-30 begin
    private Context mContext;
    private AlertDialog.Builder mBuilder;
    //chb modify for LFZS-132 2015-7-30 end
    
    
    public FlashLihgtTest(String text, Boolean visible) {
        super(text, visible);
    }

    /**
    * @Fields: flashlistener
    * @Description： To switch the flash condition monitoring events
    */
    OnClickListener flashlistener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_CLICKABLE);

                if (mIsBackCamera) {
                    //open front flashlight ;close back flashlight
                    mOpenFrontCameraFlashLight();
                    mbtnFlashButton.setText(R.string.openback_flash);
                } else if (!mIsBackCamera) {
                    //open back flashlight ;close front flashlight
                    mOpenBackCameraFlashLight();
                    mbtnFlashButton.setText(R.string.openfront_flash);
                }
            }
        };

    @Override
    public void onCreate() {
        super.onCreate();

        /**set the pass button can't click*/
        ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_UNCLICKABLE);
        /**set the acitivty title*/
        ItemTestActivity.itemActivity.setTitle(R.string.item_Flash);

    }

    @Override
    public View getView(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_flashled, null);
        mbtnFlashButton = (Button) view.findViewById(R.id.flash_front_button);
        mtvBackFlash = (TextView) view.findViewById(R.id.back_cameraflash);
        mtvFrontFlash = (TextView) view.findViewById(R.id.front_cameraflash);
        mbtnFlashButton.setOnClickListener(flashlistener);

        return view;
    }

    /**
    * @MethodName: FindFrontCamera
    * @Functions:Get front-facing camera.
    * @return        :int
    */
    @SuppressLint("NewApi")
    private int FindFrontCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for (mCameraPosition = 0; mCameraPosition < cameraCount;
                mCameraPosition++) {
            Camera.getCameraInfo(mCameraPosition, cameraInfo); // get camerainfo

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) { //front camera
                mCameraExit = true; //camera exited

                return mCameraPosition; //0
            }
        }

        return -1;
    }

    /**
    * @MethodName: FindBackCamera
    * @Functions:Get back-facing camera.
    * @return        :int
    */
    @SuppressLint("NewApi")
    private int FindBackCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for (mCameraPosition = 0; mCameraPosition < cameraCount;
                mCameraPosition++) {
            Camera.getCameraInfo(mCameraPosition, cameraInfo); // get camerainfo

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) { //back camera
                mCameraExit = true; //camera exited

                return mCameraPosition; //1
            }
        }

        return -1;
    }

    /**
    * @MethodName: mOpenFrontCameraFlashLight
    * @Functions:Close the back camera in the first,
    *                         then  open the front camera again.
    * @return        :void
    */
    private void mOpenFrontCameraFlashLight() {
        param.setFlashMode("off"); //close the back flash

        if (camera != null) {
            camera.setParameters(param);
            mtvBackFlash.setBackgroundColor(0xFFFAF0);
            mtvBackFlash.setText(R.string.had_close);
        }
        FindFrontCamera();
        if (mCameraExit) {
            camera.stopPreview();
            camera.release();
            camera = null;
            camera = Camera.open(mCameraPosition);
            camera.startPreview();
            mIsBackCamera = false;
            param.setFlashMode("torch"); //open the front flash

            if (camera != null) {
                camera.setParameters(param);
                mtvFrontFlash.setBackgroundColor(Color.GREEN);
                mtvFrontFlash.setText(R.string.had_open);
            }
        //chb modify for LFZS-132 2015-7-30 begin
        }else {
	    CameraNoExitDialog();
	}
        //chb modify for LFZS-132 2015-7-30 end
    }

    /**
    * @MethodName: mOpenBackCameraFlashLight
    * @Functions:Close the front camera in the first,
    *                          then  open the back camera again.
    * @return        :void
    * @throws
    */
    private void mOpenBackCameraFlashLight() {
        param.setFlashMode("off"); //close the front flash

        if (camera != null) {
            camera.setParameters(param);
            mtvFrontFlash.setBackgroundColor(0xFFFAF0);
            mtvFrontFlash.setText(R.string.had_close);
        }
        FindBackCamera();

        if (mCameraExit) {
            camera.stopPreview();
            camera.release();
            camera = null;
            camera = Camera.open(mCameraPosition);
            camera.startPreview();
            mIsBackCamera = true;
            param.setFlashMode("torch"); // open the back flash

            if (camera != null) {
                camera.setParameters(param);
                mtvBackFlash.setBackgroundColor(Color.GREEN);
                mtvBackFlash.setText(R.string.had_open);
            }
         //chb modify for LFZS-132 2015-7-30 begin
        }else {
           CameraNoExitDialog();
	}
      //chb modify for LFZS-132 2015-7-30 begin
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
	
	//chb modify for LFZS-132 2015-7-30 begin
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
	* @MethodName: CameraNoExitDialog 
	* @Functions:Camera no exit
	*/
	private void CameraNoExitDialog() {
	    mBuilder = new Builder(mContext);
	    if (!FindBackCameraExist()) {
			mBuilder.setMessage(R.string.bcamera_no_exits);
		} 
	    else if(!FindFrontCameraExist()){
			mBuilder.setMessage(R.string.fcamera_no_exits);
		}else {
			mBuilder.setMessage(R.string.camera_no_exits);
		}
		mBuilder.setTitle(R.string.tip);
		mBuilder.setPositiveButton(R.string.tip_ok, new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		mBuilder.create().show();
	}
	//chb modify for LFZS-132 2015-7-30 end

    @Override
    public void onPause() {
        super.onPause();
        if (camera != null) {
        	 camera.stopPreview();
             camera.release();
             camera = null;
		}
    }

    @Override
    public void onResume() {
        super.onResume();
        //chb modify for LFZS-132 2015-7-30 begin
    	if (FindBackCameraExist()) {
         	 camera = Camera.open();
             param = camera.getParameters();
             camera.startPreview();
             param.setFlashMode("torch"); // start the activity,open back flash light.
             /**Initializes the UI state*/
             mIsBackCamera = true;
             mtvBackFlash.setBackgroundColor(Color.GREEN);
             mtvBackFlash.setText(R.string.had_open);
             mtvFrontFlash.setText(R.string.had_close);
             if (camera != null) {
                 camera.setParameters(param);
             }
    	}else {
    		CameraNoExitDialog();
    	}
        //chb modify for LFZS-132 2015-7-30 end
    }

    public void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.release(); // release camera ,when destory
        }
    }
}
