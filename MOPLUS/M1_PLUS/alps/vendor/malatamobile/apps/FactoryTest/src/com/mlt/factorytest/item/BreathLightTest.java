package com.mlt.factorytest.item;

import com.mlt.factorytest.R;
import com.mlt.factorytest.R.string;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.os.IBreathLedsService; 
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.app.Activity;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;

public class BreathLightTest extends AbsHardware {
	private final int MAX_NUMBER = 3;
    private final int LED_ACTION_LOOP_BREATH = 0;
    private final int LED_ACTION_LOOP_TWINKLE = 1;

    private IBreathLedsService ledSvc = null; 

    private int index = 0;
    private int loopCount = 0;
    private int i = 2;
    private boolean isloopBreathEnd = false;
    private boolean isloopTwinkleEnd = false;
    
    private int mLedAction = LED_ACTION_LOOP_BREATH;

    private final int BREATH_LED_LOOP_MSG    = 1;        
    private final int BREATH_LED_TWINKLE_MSG = 2;
    private final int BREATH_LED_CLOSE_LOOP_MSG   = 3;
    private final int BREATH_LED_CLOSE_TWINKLE_MSG = 4;
    
    private TextView mBreathLightStatus;
    
	public BreathLightTest(String text, Boolean visible) {
	       super(text, visible);
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(Context context) {
	    LayoutInflater factory = LayoutInflater.from(context);
	    View view = factory.inflate(R.layout.item_breath_light, null);
	    mBreathLightStatus = (TextView) view.findViewById(R.id.breath_change);
	    return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		 mLedAction = LED_ACTION_LOOP_BREATH;
         index = 0;
         loopCount = 0;
         mBreathLightStatus.setText(R.string.breathing);
         mBreathLightStatus.setTextColor(Color.GREEN);
         
         if(ledSvc == null){
             ledSvc = IBreathLedsService.Stub.asInterface(ServiceManager.getService("breath_leds"));
         }
         closeAllBreathLed();
         
         Thread thread = new Thread(ledRunnable);
         thread.start(); 
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeMessages(BREATH_LED_LOOP_MSG);
		mHandler.removeMessages(BREATH_LED_TWINKLE_MSG);
		mHandler.removeMessages(BREATH_LED_CLOSE_LOOP_MSG);
		mHandler.removeMessages(BREATH_LED_CLOSE_TWINKLE_MSG);
		closeAllBreathLed();
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		closeAllBreathLed();
	}
	
	private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            Thread thread = null;
            
            switch (msg.what) {
	            case BREATH_LED_LOOP_MSG:
	                 mLedAction = LED_ACTION_LOOP_BREATH;
	                 thread = new Thread(ledRunnable);
	                 thread.start();   
	       	     break;      
	
	            case BREATH_LED_TWINKLE_MSG:
	                 mLedAction = LED_ACTION_LOOP_TWINKLE;
	                 thread = new Thread(ledRunnable);
	                 thread.start();   
	       	     break;    
	       	    
	            case BREATH_LED_CLOSE_LOOP_MSG :
	            	 closeAllBreathLed(); 
	            	 mHandler.removeMessages(BREATH_LED_LOOP_MSG);
	            	 mHandler.sendEmptyMessageDelayed(BREATH_LED_TWINKLE_MSG, 200);
	
	            case BREATH_LED_CLOSE_TWINKLE_MSG:
	                 closeAllBreathLed(); 
	                 mHandler.removeMessages(BREATH_LED_TWINKLE_MSG);
	                 mHandler.sendEmptyMessageDelayed(BREATH_LED_LOOP_MSG, 200);
	       	     break;
	
	            default:
	                 System.out.println("handler error");
	                 break;
           }
       }			
	};
	
	private Runnable ledRunnable = new Runnable() {
		@Override
		public void run() {
			if(mLedAction == LED_ACTION_LOOP_BREATH){
				loopBreathAction();
				if(!isloopBreathEnd){
					mHandler.removeMessages(BREATH_LED_TWINKLE_MSG);
					mHandler.sendEmptyMessageDelayed(BREATH_LED_LOOP_MSG, 4000);
				}else{
					mHandler.removeMessages(BREATH_LED_LOOP_MSG);
					isloopBreathEnd = false;
				}
           }else if(mLedAction == LED_ACTION_LOOP_TWINKLE){
			   	loopTwinkleAction();
			   	if(!isloopTwinkleEnd){
			   		mHandler.removeMessages(BREATH_LED_LOOP_MSG);
			   		mHandler.sendEmptyMessageDelayed(BREATH_LED_TWINKLE_MSG, 2000);
			   	}else{
			   		mHandler.removeMessages(BREATH_LED_TWINKLE_MSG);
			   		isloopTwinkleEnd = false;
			   	}
           }
		}
	};
	
	private void closeAllBreathLed(){
         try{
             System.out.println("closeAllBreathLed");
             if(ledSvc != null){
                 System.out.println("closeAllBreathLed no null");
                 ledSvc.turnOffLeds(0);
                 ledSvc.turnOffLeds(1);
                 ledSvc.turnOffLeds(2);
             }
         }catch(RemoteException e){
             e.printStackTrace();
         }
     }

     private void closeBreathLed(int index){
         try{
             if(ledSvc != null){
                 ledSvc.turnOffLeds(index);
             }
         }catch(RemoteException e){
             e.printStackTrace();
         }
     }

     private void breathLedAction(int index){
			try{
             if(ledSvc != null){
                 ledSvc.ledBreath(index);
             }
         }catch(RemoteException e){
             e.printStackTrace();
         }
     }

     private void twinkleLedAction(int index){
			try{
             if(ledSvc != null){
                 ledSvc.ledTwinkle(index);
             }
         }catch(RemoteException e){
             e.printStackTrace();
         }
     }

     private void loopBreathAction(){
         breathLedAction(index);
         index++;

         if(index == MAX_NUMBER){
             index = 0;
             isloopBreathEnd = true;
             mHandler.sendEmptyMessageDelayed(BREATH_LED_CLOSE_LOOP_MSG, 4000);
         }
     }
     
     private void loopTwinkleAction(){
        /* if(index == 0){
             closeBreathLed(2);
         }else{
             closeBreathLed(index - 1);
         }*/
         twinkleLedAction(index);
         index++;

         if(index == MAX_NUMBER){
            index = 0;
            isloopTwinkleEnd = true;
            mHandler.sendEmptyMessageDelayed(BREATH_LED_CLOSE_TWINKLE_MSG, 2000);
		}
     }

    /* private void unionBreathAction(){
			try{
             if(ledSvc != null){
                 ledSvc.ledBreath(0);
                 ledSvc.ledBreath(1);
                 ledSvc.ledBreath(2);
             }                
         }catch(RemoteException e){
             e.printStackTrace();
         }
         mHandler.sendEmptyMessageDelayed(BREATH_LED_CLOSE_UNION_MSG, 4000);
     }*/

     

     /*private void unionTwinkleAction(){
			try{
             if(ledSvc != null){
                 ledSvc.ledTwinkle(0);
                 ledSvc.ledTwinkle(1);
                 ledSvc.ledTwinkle(2);
             }
         }catch(RemoteException e){
             e.printStackTrace();
         }
         mHandler.sendEmptyMessageDelayed(BREATH_LED_CLOSE_UNION_TWINKLE_MSG, 2000);
     }*/
     
   /* private Context mContext;
    private static final String FACTORYTEST_CLOSE_BREATHLIGHT = "com.android.factorytest.closebreathlight";
    private static final String FACTORYTEST_OPEN_BREATHLIGHT0 = "com.android.factorytest.openbreathlight0";
    private static final String FACTORYTEST_OPEN_BREATHLIGHT1 = "com.android.factorytest.openbreathlight1";
    private static final String FACTORYTEST_OPEN_BREATHLIGHT2 = "com.android.factorytest.openbreathlight2";
    private static final String FACTORYTEST_STOP_BREATHLIGHT = "com.android.factorytest.stopbreathlight";
    public static boolean mIsBreathLightTestStop = false; 
    public BreathLightTest(String text, Boolean visible) {
        super(text, visible);
        // TODO Auto-generated constructor stub
    }

    @SuppressLint("InflateParams")
	@Override
    public View getView(Context context) {
    	this.mContext = context;
        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.item_breath_light, null);
        return view;
    }
    
    private final   Handler handler = new Handler();  
    static int key = 1;
	private Intent mBreathLightCloseIntent = new Intent();
	private final   Runnable breathtask = new Runnable() {  
        public void run() {  
        	if (!mIsBreathLightTestStop) {
        		switch (key) {
				case 1:
					Intent mBreathLightOpenIntent0 = new Intent();
			    	mBreathLightOpenIntent0.setAction(FACTORYTEST_OPEN_BREATHLIGHT0);
			        mContext.sendBroadcast(mBreathLightOpenIntent0);
					key = 2;
					handler.postDelayed(this,800); 
					break;
				case 2:
					mBreathLightCloseIntent.setAction(FACTORYTEST_CLOSE_BREATHLIGHT);
				    mContext.sendBroadcast(mBreathLightCloseIntent);
				        
					Intent mBreathLightOpenIntent1 = new Intent();
			    	mBreathLightOpenIntent1.setAction(FACTORYTEST_OPEN_BREATHLIGHT1);
			        mContext.sendBroadcast(mBreathLightOpenIntent1);
					key = 3;
					handler.postDelayed(this, 800);  
					break;
				case 3:
					mBreathLightCloseIntent.setAction(FACTORYTEST_CLOSE_BREATHLIGHT);
				    mContext.sendBroadcast(mBreathLightCloseIntent);
				    
					Intent mBreathLightOpenIntent2 = new Intent();
			    	mBreathLightOpenIntent2.setAction(FACTORYTEST_OPEN_BREATHLIGHT2);
			        mContext.sendBroadcast(mBreathLightOpenIntent2);
					key = 4;
					handler.postDelayed(this, 800);  
					break;
				
				case 4:
					mBreathLightCloseIntent.setAction(FACTORYTEST_CLOSE_BREATHLIGHT);
			        mContext.sendBroadcast(mBreathLightCloseIntent);
					//mIsBreathLightTestStop = true;
			        key = 1;
					handler.postDelayed(this, 400);  
					break;
				}
			}
        }  
    };  
    
    @Override
    public void onCreate() {
        super.onCreate();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
        mIsBreathLightTestStop = false;
        handler.post(breathtask);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	mIsBreathLightTestStop = true;
        mBreathLightCloseIntent.setAction(FACTORYTEST_CLOSE_BREATHLIGHT);
        mContext.sendBroadcast(mBreathLightCloseIntent);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsBreathLightTestStop = true;
        Intent mBreathLightStopIntent = new Intent();
        mBreathLightStopIntent.setAction(FACTORYTEST_STOP_BREATHLIGHT);
        mContext.sendBroadcast(mBreathLightStopIntent);
        
    }*/
    
}
