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
//chb add for VFOZESGW-81 at 20151023 begin
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.widget.Toast;
//chb add for VFOZESGW-81 at 20151023 end

import com.mlt.factorytest.ItemTestActivity;
import com.mlt.factorytest.R;


/** 
* @ClassName: FlashLihgtTest 
* @PackageName:com.malata.factorytest.item
* @Description: TODO for test flash light
* @author:   chehongbin
* @date:     2015年1月27日 下午1:31:37  
* Copyright (c) 2015 MALATA,All Rights Reserved.
*/
public class FlashLihgtTest extends AbsHardware {
	private View view;
	private Camera camera = null;
	private Camera.Parameters param;
	private ToggleButton toggleButton;
	boolean isflash = false;
	private int mCameraPosition;
	private TextView mtvCameraExist;
	private boolean mFirst = true; //chb add for VFOZESGW-81 at 20151023 end
	
	public FlashLihgtTest(String text, Boolean visible) {
		super(text, visible);
	}

	@Override
	public View getView(Context context) {
		this.mContext = context;//chb add for VFOZESGW-81 at 20151023
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
		        	//chb add for VFOZESGW-81 at 20151023 begin
		        	if (mFirst) {
		        		try {
		        			 camera = Camera.open();
		        			 if (camera != null){
		                    	 param = camera.getParameters();
		                         //camera.startPreview();
		                         param.setFlashMode("torch");
		                         camera.setParameters(param);
		                    }
						} catch (Exception e) {
							e.printStackTrace();
					        if (camera != null) {
							    //camera.stopPreview();
							    camera.release();
							    camera = null;
						    }
					        Toast.makeText(mContext, R.string.flash_exception, Toast.LENGTH_LONG).show();
					        ItemTestActivity.itemActivity.finish();
					    }
		        		mFirst = false;
		            }else {
		        	    param.setFlashMode("torch");
		        	    if (camera != null){
		        		    camera.setParameters(param);
		        	    }
				    }
		        }
	        	while (localToggleButton.isChecked()) {
	        		return;
	        	}
	        	
		        param.setFlashMode("off");
		        if (camera != null) {
		        	camera.setParameters(param);
		          return;
		        }
		        Camera.open();
		        camera.setParameters(param);
				
		     }
			else {
				mCameraExitDialog();
				//Toast.makeText(mContext, R.string.bcamera_no_exits, Toast.LENGTH_LONG).show(); 
			}
			//chb add for VFOZESGW-81 at 20151023 end
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
			//this.camera.stopPreview();
		    this.camera.release();
		    this.camera = null;
		}
		
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		/*chb add for VFOZESGW-81 at 20151023 begin
		if (FindBackCameraExist()) {
            //chb add for VFOZESGW-81 at 20151023 begin
            try {
                this.camera = Camera.open();
                if (FlashLihgtTest.this.camera != null){
                	 this.param = this.camera.getParameters();
                     this.camera.startPreview();
                     FlashLihgtTest.this.param.setFlashMode("torch");
                    FlashLihgtTest.this.camera.setParameters(FlashLihgtTest.this.param);
                }
            } catch (Exception e) {
            	        e.printStackTrace();
		        Toast.makeText(mContext, R.string.flash_exception, Toast.LENGTH_LONG).show();
		        if (camera != null) {
			    this.camera.stopPreview();
			    this.camera.release();
			    this.camera = null;
			}
		        ItemTestActivity.itemActivity.finish();
            }
        }else {
            mCameraExitDialog();
        //chb add for VFOZESGW-81 at 20151023 end
		}
        //chb add for VFOZESGW-81 at 20151023 end*/
	}
	
	public void onDestroy() {
		super.onDestroy();
		// release camera ,when destory
		if (camera!=null) {
			camera.release();
			camera =null;
		}
	};
	
	//chb add for VFOZESGW-81 at 20151023 begin
	private Context mContext;
	private AlertDialog.Builder mBuilder;
	private void mCameraExitDialog() {
	    mBuilder = new Builder(mContext);
	    if (!FindBackCameraExist()) {
			mBuilder.setMessage(R.string.bcamera_no_exits_flash);
		} 
	    else if(!FindFrontCameraExist()){
			mBuilder.setMessage(R.string.fcamera_no_exits_flash);
		}else {
			mBuilder.setMessage(R.string.flash_exception);
		}
		mBuilder.setTitle(R.string.tip);
		mBuilder.setPositiveButton(R.string.tip_ok, new android.content.DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				//do something , When you click on the Dialog button
			}
		});
		mBuilder.create().show();
	}
	//chb add for VFOZESGW-81 at 20151023 end
}
