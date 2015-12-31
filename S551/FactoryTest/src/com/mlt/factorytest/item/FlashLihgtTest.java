package com.mlt.factorytest.item;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.mlt.factorytest.ItemTestActivity;
import com.mlt.factorytest.R;


/** 
* @ClassName: FlashLihgtTest 
* @PackageName:com.malata.factorytest.item
* @Description: TODO for test flash light
* @author:   chehongbin
* @date:     2015年1月27日 下午1:31:37  
* Copyright (c) 2015 MALATA,All Rights Reserved.
* Modify History
* ---------------------------
* Who	:	chehongbin
* When	:	2015年1月27日
* JIRA	:	
* What	:	ADD text dispaly of sms input number
*/
public class FlashLihgtTest extends AbsHardware {
	private View view;
	private Camera camera = null;
	private Camera.Parameters param;
	private ToggleButton toggleButton;
	boolean isflash = false;
	private int mCameraPosition;
	private TextView mtvCameraExist;
	
	
	public FlashLihgtTest(String text, Boolean visible) {
		super(text, visible);
	}

	@Override
	public View getView(Context context) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		view = layoutInflater.inflate(R.layout.item_flashled, null);
		mtvCameraExist = (TextView) view.findViewById(R.id.camera_exist);
		toggleButton = (ToggleButton) view.findViewById(R.id.flash_button);
		toggleButton.setOnClickListener(flashlistener);
		/**set the pass button can't click*/
		ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_UNCLICKABLE);
		
		if (!FindBackCameraExist()) {
			mtvCameraExist.setVisibility(View.VISIBLE);
			mtvCameraExist.setTextColor(Color.RED);
			toggleButton.setClickable(false);
		}
		
		
		return view;
	}
	
	
	
	OnClickListener flashlistener = new OnClickListener() {
		@Override
		public void onClick(View  paramAnonymousView) {
			if (FindBackCameraExist()) {
				ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_CLICKABLE);
				ToggleButton localToggleButton = (ToggleButton)paramAnonymousView;
		        if (localToggleButton.isChecked()) {
		        	FlashLihgtTest.this.param.setFlashMode("torch");
		        	if (FlashLihgtTest.this.camera != null){
		        		FlashLihgtTest.this.camera.setParameters(FlashLihgtTest.this.param);
		        	}
		        }
	        	while (localToggleButton.isChecked()) {
	        		return;
	        	}
	        	
		        FlashLihgtTest.this.param.setFlashMode("off");
		        if (FlashLihgtTest.this.camera != null) {
		        	FlashLihgtTest.this.camera.setParameters(FlashLihgtTest.this.param);
		          return;
		        }
		        Camera.open();
		        FlashLihgtTest.this.camera.setParameters(FlashLihgtTest.this.param);
				
		     }
			else {
				//Toast.makeText(mContext, R.string.bcamera_no_exits, Toast.LENGTH_LONG).show(); 
			}
		}
				
	};
	
//	        mBuilder.setPositiveButton(R.string.tip_ok,
//	            new OnClickListener() {
//	                @Override
//	                public void onClick(DialogInterface dialog, int which) {
//	                    dialog.dismiss();
//	                    
//	                }
//	            });
//	        mBuilder.create().show();
	   // }
	
		
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
		
	
		

	@Override
	public void onPause() {
		super.onPause();
		this.toggleButton.setChecked(true);
		if (camera != null) {
			this.camera.stopPreview();
		    this.camera.release();
		    this.camera = null;
		}
		
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (FindBackCameraExist()) {
			this.camera = Camera.open();
			this.param = this.camera.getParameters();
			this.camera.startPreview();
			FlashLihgtTest.this.param.setFlashMode("torch");
	        if (FlashLihgtTest.this.camera != null){
	       	FlashLihgtTest.this.camera.setParameters(FlashLihgtTest.this.param);
	       }
		}
		 
	}
	
	public void onDestroy() {
		super.onDestroy();
		// release camera ,when destory
		if (camera!=null) {
			camera.release();
		}
	};
}
