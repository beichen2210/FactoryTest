package com.mlt.factorytest.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.mlt.factorytest.ItemTestActivity;
import com.mlt.factorytest.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Service;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.gxFP.IEnrollCallback;
import com.goodix.device.MessageType;

import android.gxFP.FingerprintManager.EnrollSession;
import com.mlt.util.AlgoResult;
import com.mlt.util.Fingerprint;
import com.mlt.util.Preferences;
import com.mlt.widget.FingerprintProgressBar;
/** 
* @ClassName: FingerprintTest 
* @PackageName:com.mlt.factorytest.item
* @author:   chehongbin
* @date:     2015-11-5 下午2:07:52  
* Copyright (c) 2015 MALATA,All Rights Reserved.
*/
public class FingerprintTest extends AbsHardware {
	private View view;
	private static Context mContext;
	private static final long CANCEL_TIME_INTERVAL = 30000;
	private static final long RELEASE_TIME_INTERVAL = 100;
	private static final int FORECAST_PERCENT = 7;
	private static final String TAG = "FingerprintTest";
	private static ViewGroup mRootGroup;
	private static ImageView mPhoneImage;
	private static TextView mTitleTxt;
	private static TextView mTitleNoticeTxt;
	private static TextView mSubInfoTxt;
	private static TextView mSubInfoTxtOutside;
	private static FingerprintProgressBar mRegisterProgressBar;
	private static Handler mCancelHandler;
	private static Runnable mCancelRunable;
	private static Handler mReleaseFingerHandler;
	private static ReleaseTouchRunnable mReleaseFingerRunable;
	private static int mPercent = 0;
	private static EnrollSession mSession;
	private static LinearLayout mAlgoLog;
	private static TextView mTopView;
	private static TextView mBehandView;
	private static ImageView mImageOne;
	
	private boolean mIsStartFingerPrintTest = false;
	private static Vibrator vib ;
	private static AlertDialog mDialog;

	public static FingerprintTest instance;

	private ArrayList<Fingerprint> mDataList = null;

	public FingerprintTest(String text, Boolean visible) {
		super(text, visible);
	}

	@Override
	public View getView(Context context) {
		this.mContext =context; 
		instance = this;
		initRegister();
		startCancelTimer();
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		view = layoutInflater.inflate(R.layout.item_fingerprint_test, null);
		initView();
		ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_UNCLICKABLE);// pass button unclickable
		return view;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onPause() {
		Log.v(TAG, "onPause");
		if(mSession!=null) {
			mSession.exit();
			cancelCancelTimer();
		}
		mPercent = 0;
		mIsStartFingerPrintTest = false;
		super.onPause();
	}


	@Override
	public void onResume() {
		Log.v(TAG, "onResume");
		super.onResume();
		mIsStartFingerPrintTest = true;
		vib = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy");
		instance = null;
		cancelCancelTimer();
		cancelReleaseFingerTimer();
		mIsStartFingerPrintTest = false;
		super.onDestroy(); 
	}
	private void initRegister() {

		if (null == mSession) {
			mSession = MyApplication.getInstance().getFpServiceManager().newEnrollSession(mEnrollCallback);
		}
		mSession.enter();
	}
	
	private void initView() {
		mRootGroup = (ViewGroup) view.findViewById(R.id.register_root_group);
		mRootGroup.setOnClickListener(new RootGroupClickListener());
		mPhoneImage = (ImageView) view.findViewById(R.id.register_phone);
		mTitleTxt = (TextView) view.findViewById(R.id.title_text);
		mTitleNoticeTxt = (TextView) view.findViewById(R.id.title_notice_text);
		mRegisterProgressBar = (FingerprintProgressBar) view.findViewById(R.id.register_progress);
		mSubInfoTxt = (TextView) view.findViewById(R.id.register_sub_info);

		mSubInfoTxt.setText(getStyle(String.format(mContext.getString(R.string.capture_notice_put_on_screen),mContext.getString(R.string.center_area)),mContext.getString(R.string.center_area)));

		mSubInfoTxtOutside = (TextView) view.findViewById(R.id.register_sub_info_outside);

		mAlgoLog = (LinearLayout) view.findViewById(R.id.register_info);
		if (Preferences.getEnableEM() == true) {
			mAlgoLog.setVisibility(View.VISIBLE);
		} else {
			mAlgoLog.setVisibility(View.GONE);
		}

		mTopView = (TextView) view.findViewById(R.id.top_textview);
		mBehandView = (TextView) view.findViewById(R.id.behand_textview);

		mImageOne = (ImageView) view.findViewById(R.id.image_one);

		if (null == mCancelHandler) {
			mCancelHandler = new Handler();
		}
		if (null == mCancelRunable) {
			mCancelRunable  = new CancelRunnable(this);
		}
		if (null == mReleaseFingerHandler) {
			mReleaseFingerHandler = new Handler();
		}
		if (null == mReleaseFingerRunable) {
			mReleaseFingerRunable = new ReleaseTouchRunnable((ItemTestActivity) mContext);
		}
	}

	private static void showTextTranslateAnim(View v, int animID, int visible) {
		Animation animation = AnimationUtils.loadAnimation(mContext, animID);
		animation.setAnimationListener(new TitleExitAnimListener(v, visible));
		v.startAnimation(animation);
	}

	private void startCancelTimer() {
		if (null != mCancelHandler && null != mCancelRunable) {
			mCancelHandler.postDelayed(mCancelRunable, CANCEL_TIME_INTERVAL);
		}
	}

	private static void cancelCancelTimer() {
		Log.v(TAG, "cancelCancelTimer");
		if (null != mCancelHandler && null != mCancelRunable) {
			mCancelHandler.removeCallbacks(mCancelRunable);
		}
	}

	private static void resetCancelTimer() {
		Log.v(TAG, "resetCancelTimer");
		if (null != mCancelHandler && null != mCancelRunable) {
			mCancelHandler.removeCallbacks(mCancelRunable);
			mCancelHandler.postDelayed(mCancelRunable, CANCEL_TIME_INTERVAL);
		}
	}

	private static void startReleaseFingerTimer() {
		Log.v(TAG, "startReleaseFingerTimer");
		if (null != mReleaseFingerHandler && null != mReleaseFingerRunable) {
			mReleaseFingerHandler.postDelayed(mReleaseFingerRunable, RELEASE_TIME_INTERVAL);
		}
	}

	private static void cancelReleaseFingerTimer() {
		Log.v(TAG, "cancelReleaseFingerTimer");
		if (null != mReleaseFingerHandler && null != mReleaseFingerRunable) {
			if (true == mReleaseFingerRunable.bWarning) {
				Log.v(TAG, "Cancel Warning!");
				mReleaseFingerRunable.bWarning = false;
				cancelWarning();
			}
			mReleaseFingerHandler.removeCallbacks(mReleaseFingerRunable);
		}

	}

	private static void startWarning(int textID) {
		mTitleNoticeTxt.setVisibility(View.VISIBLE);
		mTitleNoticeTxt.setText(textID);
		showTextTranslateAnim(mTitleNoticeTxt, R.anim.register_title_text_enter, View.VISIBLE);
		showTextTranslateAnim(mTitleTxt, R.anim.register_title_text_exit, View.INVISIBLE);
	}

	private static SpannableStringBuilder getStyle(String text,String keyTex) {
		
		int length  = 0;
		int index = 0;
		if (TextUtils.isEmpty(text) || TextUtils.isEmpty(keyTex)) {
			return null;
		}
		index = text.indexOf(keyTex);
		length = keyTex.length();
		SpannableStringBuilder style = new SpannableStringBuilder(text);
		style.setSpan(new ForegroundColorSpan(Color.RED), index, index + length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE); 
		return style;

	}

	private static void startSubWarning(String text,String keyText) {

		mSubInfoTxtOutside.setVisibility(View.VISIBLE);
		mSubInfoTxtOutside.setText(getStyle(text,keyText));
		showTextTranslateAnim(mSubInfoTxtOutside, R.anim.register_title_text_enter, View.VISIBLE);
		showTextTranslateAnim(mSubInfoTxt, R.anim.register_title_text_exit, View.INVISIBLE);

	}

	private static void cancelWarning() {
		showTextTranslateAnim(mTitleNoticeTxt, R.anim.register_title_text_exit, View.INVISIBLE);
		showTextTranslateAnim(mTitleTxt, R.anim.register_title_text_enter, View.VISIBLE);
	}

	private class CancelRunnable implements Runnable {
		private WeakReference<FingerprintTest> mActivityReference;

		public CancelRunnable(FingerprintTest fingerprintTest) {
			mActivityReference = new WeakReference<FingerprintTest>(fingerprintTest);
		}

		@Override
		public void run() {
			if (mIsStartFingerPrintTest) {
				FingerprintTest activity = (FingerprintTest) mActivityReference.get();
				if (null != activity) {
					
					try {
						if (null != mSession) {
							mSession.exit();
						}
						startWarning(R.string.register_register_failed);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		}
	}

	private class ReleaseTouchRunnable implements Runnable {
		private WeakReference<ItemTestActivity> mActivityReference;
		public boolean bWarning = false;

		public ReleaseTouchRunnable(ItemTestActivity activity) {
			mActivityReference = new WeakReference<ItemTestActivity>(activity);
		}

		@Override
		public void run() {
			if (mIsStartFingerPrintTest) {
				Log.v(TAG, "ReleaseTouchRunnable:Run...");
				ItemTestActivity activity = (ItemTestActivity) mActivityReference.get();
				if (null != activity) {
					bWarning = true;
					startWarning(R.string.register_notice_the_hand);
				}
			}
		}
	}

	private static class TitleExitAnimListener implements AnimationListener {
		int visible;
		private WeakReference<View> mViewReference;

		public TitleExitAnimListener(View v, int visible) {
			this.visible = visible;
			mViewReference = new WeakReference<View>(v);
		}

		@Override
		public void onAnimationEnd(Animation arg0) {
			TextView textView = (TextView) mViewReference.get();
			if (null != textView) {
				textView.setVisibility(visible);
			}
		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
		}

		@Override
		public void onAnimationStart(Animation arg0) {
		}
	}

	private class RootGroupClickListener implements View.OnClickListener {
		@Override
		public void onClick(View arg0) {
			// Show notice dialog;
			OnClickListener listener = new OnClickListener() {
				public void onClick(DialogInterface dialog, int arg1) {
					dialog.dismiss();
				}
			};
			showOneButtonDialog(false, R.string.register_touchhomekey, R.string.register_notice_content_one, R.string.register_dialog_key_good, listener);
		}
	}

	private void showOneButtonDialog(boolean bCancel, int titleID, int messageID, int keyID, OnClickListener listener) {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setCancelable(bCancel);
		builder.setMessage(messageID);
		builder.setTitle(titleID);
		builder.setPositiveButton(keyID, listener);
		builder.create().show();
	}

	private IEnrollCallback mEnrollCallback = new IEnrollCallback.Stub() {
		@Override
		public void handleMessage(int msg, int arg0, int arg1, byte[] data) throws RemoteException {
			Log.v(TAG, String.format("msg = %d , arg0 = %d ,arg1 = %d", msg, arg0, arg1));
			mHandler.sendMessage(mHandler.obtainMessage(msg, arg1, arg0, data));
	//		return false;
		}
	};

	private static  void showNoteDialog(Context context, boolean bCancel, int titleID, int messageID, int keyID, OnClickListener listener) {
		if (mDialog != null && mDialog.isShowing()) {
			return;
		}
		AlertDialog.Builder builder = new Builder(context);
		builder.setCancelable(bCancel);
		builder.setMessage(messageID);
		builder.setTitle(titleID);
		//builder.setPositiveButton(keyID, listener);
		mDialog = builder.create();
		mDialog.show();

	}
	private static void showNoteDialog(Context context, boolean bCancel, int titleID, String messageID, int keyID, OnClickListener listener) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setCancelable(bCancel);
		builder.setMessage(messageID);
		builder.setTitle(titleID);
		builder.setPositiveButton(keyID, listener);
		builder.create().show();

	}

	private RegisterHandler mHandler = new RegisterHandler(ItemTestActivity.itemActivity);

	private static class RegisterHandler extends Handler {
		private final WeakReference<ItemTestActivity> mActivityRef;
		private final static int MAX_ACTION_ERROR = 4;
		private int mBadImageCount = 0;
		private int mNoPieceTime = 0;
		private int mNoMoveTime = 0;

		private boolean bHasShowBadDialog = false;
		private boolean bHasShowNoPieceDialog = false;
		private boolean bHasShowMoveDialog = false;

		private boolean bHasShowAni = false;
		
		private boolean bToutch = false;

		private OnClickListener mListener = new OnClickListener() {
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
			}
		};

		public RegisterHandler(ItemTestActivity activity) {
			mActivityRef = new WeakReference<ItemTestActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			if (mActivityRef.get() == null)
				return;
			final ItemTestActivity activity = (ItemTestActivity) mActivityRef.get();
			if (null == activity) {
				return;
			}

			switch (msg.what) {
				case MessageType.MSG_TYPE_COMMON_NOTIFY_INFO :
					Object obj = msg.obj;
					if (obj != null) {
						byte[] loginfo = (byte[]) obj;
						String str = new String(loginfo);
						if (!AlgoResult.isMatchInfo(str)) {
							if (AlgoResult.isFilePath(str)) {
								mTopView.setTextSize(13);
								mTopView.setText(AlgoResult.bulidLog(mContext, str, AlgoResult.FILTER_REGISTER, 0));
							}
							mBehandView.setText(AlgoResult.bulidLog(activity, str, AlgoResult.FILTER_REGISTER, 0));
							mBehandView.setTextSize(15);
						}
						int index = str.indexOf("=", str.indexOf("=") + 1);

						if (-1 != index) {
							String fileName = null;
							if (index != -1) {
								fileName = str.substring(index + 1, str.length() - 1);
								File file = new File(fileName);
								try {
									InputStream in = new FileInputStream(file);
									Bitmap map = BitmapFactory.decodeStream(in);

									mImageOne.setImageBitmap(map);

									in.close();
								} catch (Exception e) {
									//e.printStackTrace();
									Log.d("chehongbin", "Exception e:"+e);
								}
							}
						}
					}
					break;
				case MessageType.MSG_TYPE_REGISTER_DUPLICATE_REG :
					Log.d("chehongbin", "MessageType_MSG_TYPE_REGISTER_DUPLICATE_REG:"+MessageType.MSG_TYPE_REGISTER_DUPLICATE_REG);
					/*//L.d("index == " + activity.getFingerViewIndex(msg.arg1));
					showNoteDialog(activity, false, R.string.register_notice, String.format(activity.getResources().getString(R.string.register_duplicate), activity.getFingerViewIndex(msg.arg1)),
							R.string.register_dialog_key_good, new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									dialog.dismiss();
									activity.finish();
									activity.overridePendingTransition(0, 0);
								}
							});

					break;*/
				case MessageType.MSG_TYPE_REGISTER_PIECE :
				case MessageType.MSG_TYPE_REGISTER_NO_PIECE :
				case MessageType.MSG_TYPE_REGISTER_NO_EXTRAINFO :
				case MessageType.MSG_TYPE_REGISTER_LOW_COVER :
				case MessageType.MSG_TYPE_REGISTER_GET_DATA_FAILED :
				case MessageType.MSG_TYPE_REGISTER_BAD_IMAGE :
					Log.v(TAG, "RegisterHandler: Result");
					Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
					vib.vibrate(100);
					if (msg.what == MessageType.MSG_TYPE_REGISTER_PIECE || msg.what == MessageType.MSG_TYPE_REGISTER_NO_PIECE) {
						mPercent = msg.arg2;
					}
					if (msg.what == MessageType.MSG_TYPE_REGISTER_BAD_IMAGE || msg.what == MessageType.MSG_TYPE_REGISTER_GET_DATA_FAILED) {
						//mBadImageCount++;
						mPercent = msg.arg2;
						//if (mBadImageCount >= MAX_ACTION_ERROR && bHasShowBadDialog == false) {
						  if (bToutch == true) {
								showNoteDialog(activity, true, R.string.register_notice, R.string.register_bad_images, R.string.register_dialog_key_good, mListener);
								bHasShowBadDialog = true;
								mBadImageCount = 0;
						  } else {
								showNoteDialog(activity, true, R.string.register_notice_steady, R.string.register_notiece_steady_content, R.string.register_dialog_key_good, mListener);
						  }
	
						//}
					} else if (msg.what != MessageType.MSG_TYPE_REGISTER_BAD_IMAGE && msg.what != MessageType.MSG_TYPE_REGISTER_GET_DATA_FAILED && msg.what != MessageType.MSG_TYPE_COMMON_TOUCH
							&& msg.what != MessageType.MSG_TYPE_COMMON_UNTOUCH) {
						mBadImageCount = 0;
					}

					if (msg.what == MessageType.MSG_TYPE_REGISTER_NO_EXTRAINFO) {
						mNoMoveTime++;
						//if (mNoMoveTime >= MAX_ACTION_ERROR && bHasShowMoveDialog == false) {
							showNoteDialog(activity, true, R.string.register_notice, R.string.register_no_move, R.string.register_dialog_key_good, mListener);
							bHasShowMoveDialog = true;
							mNoMoveTime = 0;
						//}
					} else if (msg.what != MessageType.MSG_TYPE_REGISTER_NO_EXTRAINFO && msg.what != MessageType.MSG_TYPE_COMMON_TOUCH && msg.what != MessageType.MSG_TYPE_COMMON_UNTOUCH) {
						mNoMoveTime = 0;
					}

					if (msg.what == MessageType.MSG_TYPE_REGISTER_NO_PIECE) {
						mNoPieceTime++;

						if (mNoPieceTime >= MAX_ACTION_ERROR && bHasShowNoPieceDialog == false) {
							showNoteDialog(activity, true, R.string.register_notice, R.string.register_no_piece_together, R.string.register_dialog_key_good, mListener);
							bHasShowNoPieceDialog = true;
							mNoPieceTime = 0;
						}
					} else if (msg.what != MessageType.MSG_TYPE_REGISTER_NO_PIECE && msg.what != MessageType.MSG_TYPE_COMMON_TOUCH && msg.what != MessageType.MSG_TYPE_COMMON_UNTOUCH) {
						mNoPieceTime = 0;
					}

					int index = msg.arg1;
					mRegisterProgressBar.setProgress(mPercent);

					Log.v(TAG, "RegisterHandler: mPercent" + mPercent);
					if (mPercent >= 100) { //100 % 
						try {
							mSession.save(index);
						} catch (Exception e) {
							e.printStackTrace();
						}
						//CaptureResult(Integer.toString(index));
						cancelReleaseFingerTimer();
						cancelCancelTimer();
						startWarning(R.string.register_register_complete);
						ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_CLICKABLE);
						mSubInfoTxt.setText(R.string.register_complete_infomation);
						mRootGroup.setOnClickListener(null);
					} else {
						if (mPercent >= 70 && bHasShowAni == false) {
							bHasShowAni = true;
							startSubWarning(
									String.format(activity.getResources().getString(R.string.capture_notice_put_on_screen_outside),
											activity.getResources().getString(R.string.outside_area)),activity.getResources().getString(R.string.outside_area));
							// activity.mSubInfoTxt.setText(R.string.capture_notice_put_on_screen_outside);
						}
						if (bToutch == true) {
						    startReleaseFingerTimer();
						}
					}
					break;
				case MessageType.MSG_TYPE_COMMON_TOUCH :
					Log.v(TAG, "RegisterHandler:MSG_TYPE_COMMON_TOUCH");
					//vib.vibrate(100);
					bToutch = true;
					if (mDialog != null) {
						mDialog.dismiss();
					}
					Log.d("chehongbin", "mDialog"+mDialog);
					mRegisterProgressBar.setProgress(FORECAST_PERCENT + mPercent);
					// activity.cancelCancelTimer();
					resetCancelTimer();

					// Dissolve phone image.
					if (mPhoneImage.getVisibility() == View.VISIBLE) {
						Animation animation = AnimationUtils.loadAnimation(activity, R.anim.register_phone_image_exit);
						animation.setAnimationListener(new PhoneImageExitAnimListener(mPhoneImage));
						mPhoneImage.startAnimation(animation);
						mRegisterProgressBar.setSlideVisible(true);
						//startWarning(R.string.register_register_complete);
						//ItemTestActivity.itemActivity.handler.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_CLICKABLE);
					}
					
					break;
				case MessageType.MSG_TYPE_COMMON_UNTOUCH :
					bToutch = false;
					Log.v(TAG, "RegisterHandler:MSG_TYPE_COMMON_UNTOUCH");
					cancelReleaseFingerTimer();
					break;

				default :
					break;
			}
		}

		public class PhoneImageExitAnimListener implements AnimationListener {
			private WeakReference<View> mViewReference;

			public PhoneImageExitAnimListener(View v) {
				mViewReference = new WeakReference<View>(v);
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				View widget = (View) mViewReference.get();
				if (null != widget) {
					widget.setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationStart(Animation arg0) {
			}
		}
	}
	
}
