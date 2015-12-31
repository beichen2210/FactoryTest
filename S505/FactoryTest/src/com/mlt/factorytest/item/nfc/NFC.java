package com.mlt.factorytest.item.nfc;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mlt.factorytest.R;
import com.mlt.factorytest.ItemTestActivity;
import com.mlt.factorytest.item.AbsHardware;

/**
 * @ClassName: NFC
 * @Description: This class is mainly used to test card read mode and Virtual
 *               card mode to enter from here
 * @Function: Test card read mode
 * @author: huangguoxiong
 * @date: 2015-2-11 am10:49:12 Copyright (c) 2015, Malata All Rights Reserved.
 */
public class NFC extends AbsHardware {

	// To obtain the NFC adapter
	private NfcAdapter mNfcAdapter;
	private View mView;
	// Filter can read the card types
	private String[][] mTechList;
	private IntentFilter[] mIntentFilters;
	private PendingIntent mPendingIntent;
	//Card information stored in the tag
	private Tag mTag;
	// Into the virtual card mode button
	private Button mbtVirtaulMode;
	private static final int NFCINTESTING = 1;
	private static final int TVSETVISIABLE = 2;
	private static final int TVSETINVISIABLE = 3;
	private static final int BTSETCLIABLE = 4;
	// Timeout handling
	public Timer mTimer;
	public Timer mTimer2;
	private boolean mSetTextVisiable;
	private Context mContext;
	private TextView mtvReadCradPass;
	private TextView mCardSimulatePass;
	// If there is no NFC function Set the text to inform the user
	private RelativeLayout mrlNoSupportNfc;
	private TextView mtvIsSupportNfc;
	private TextView mtvInTesting;

	public NFC(String text, Boolean visible) {
		super(text, visible);
	}

	@Override
	public View getView(Context context) {
		mContext = context;
		mView = LayoutInflater.from(context).inflate(R.layout.item_nfc, null);
		mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
		findViewById(mView);
		/** If mNfcAdapter is null there is no NFC function */
		if (mNfcAdapter != null) {
			if (!mNfcAdapter.isEnabled()) {
				mNfcAdapter.enable();
			}
			mtvIsSupportNfc.setVisibility(View.GONE);
			mTechList = new String[][] {
					new String[] { android.nfc.tech.NfcA.class.getName() },
					new String[] { android.nfc.tech.NfcB.class.getName() },
					new String[] { android.nfc.tech.NfcF.class.getName() },
					new String[] { android.nfc.tech.IsoDep.class.getName() },
					new String[] { android.nfc.tech.Ndef.class.getName() },
					new String[] { android.nfc.tech.MifareClassic.class
							.getName() },
					new String[] { android.nfc.tech.MifareUltralight.class
							.getName() },
					new String[] { android.nfc.tech.NfcBarcode.class.getName() },
					new String[] { android.nfc.tech.NfcV.class.getName() }, };

			mIntentFilters = new IntentFilter[] {
					new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
					new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
					new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED) };

			mPendingIntent = PendingIntent.getActivity(context, 0, new Intent(
					context, ItemTestActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			mSetTextVisiable = true;
			mTimer = new Timer();
			mTimer2 = new Timer();
			task();
		} else {
			mrlNoSupportNfc.setVisibility(View.GONE);
		}

		return mView;
	}

	private void findViewById(View view) {
		mrlNoSupportNfc = (RelativeLayout) view.findViewById(R.id.rl);
		mtvIsSupportNfc = (TextView) view.findViewById(R.id.tv_nfctest);
		mbtVirtaulMode = (Button) view.findViewById(R.id.bt_cardSimulate);

		mbtVirtaulMode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ItemTestActivity.itemActivity,
						VirtualCardFunction.class);
				/** If NFC didn't close, close the NFC in the first place */
				if (mNfcAdapter.isEnabled()) {
					mNfcAdapter.disable();
					ItemTestActivity.itemActivity.startActivity(intent);
				} else {
					ItemTestActivity.itemActivity.startActivity(intent);
				}
			}
		});
		mbtVirtaulMode.setClickable(false);
		mtvReadCradPass = (TextView) view.findViewById(R.id.tv_readCardPass);
		mtvInTesting = (TextView) view.findViewById(R.id.tv_readCardIntesting);
		mCardSimulatePass = (TextView) view
				.findViewById(R.id.tv_cardSimulatePass);
		ItemTestActivity.itemActivity.handler
				.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_UNCLICKABLE);

	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NFC.NFCINTESTING:
				mtvReadCradPass.setText(R.string.nfc_fail);
				mtvReadCradPass.setBackgroundColor(Color.RED);
				mTimer2.cancel();
				mbtVirtaulMode.setClickable(true);
				mHandler.sendEmptyMessage(TVSETVISIABLE);
				mtvInTesting.setText(R.string.nfc_testfinish);
				mNfcAdapter.disableForegroundDispatch((Activity) mContext);
				break;
			case NFC.TVSETVISIABLE:
				mtvInTesting.setVisibility(View.VISIBLE);
				break;
			case NFC.TVSETINVISIABLE:
				mtvInTesting.setVisibility(View.INVISIBLE);
				break;
			case BTSETCLIABLE:
				mbtVirtaulMode.setClickable(true);
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onResume() {
		/** start read card */
		if (mNfcAdapter != null) {
			mNfcAdapter.enableForegroundDispatch(ItemTestActivity.itemActivity,
					mPendingIntent, mIntentFilters, mTechList);
		}

		/** set the acitivty title */
		ItemTestActivity.itemActivity.setTitle(R.string.item_NFC);
		super.onResume();
	}

	/**
	 * When the Activity is not in at the front desk, cancel the card read
	 */
	@Override
	public void onPause() {
		super.onPause();
		if (null != mNfcAdapter) {
			mNfcAdapter
					.disableForegroundDispatch(ItemTestActivity.itemActivity);
		}
	}

	/**
	 * When the card read success Into this method
	 * 
	 */
	@Override
	public void onNewIntent(Intent intent) {
		// Information stored in the tag
		mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (mTag != null) {
			mTimer.cancel();
			mtvReadCradPass.setBackgroundColor(Color.GREEN);
			mtvReadCradPass.setText(R.string.nfc_pass);
			mtvInTesting.setText(R.string.nfc_testfinish);
			mTimer2.cancel();
			mHandler.sendEmptyMessage(TVSETVISIABLE);
			mHandler.sendEmptyMessage(BTSETCLIABLE);
			ItemTestActivity.itemActivity.handler
					.sendEmptyMessage(ItemTestActivity.MSG_BTN_PASS_CLICKABLE);
		}

		// return;
	}

	/**
	 * onDestroy close NFC and cancel timer
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != mNfcAdapter) {
			mNfcAdapter.disable();
		}
		if (mTimer != null) {
			mTimer.cancel();
		} else if (mTimer2 != null) {
			mTimer2.cancel();
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			onDestroy();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void task() {
		/**
		 * If you couldn't longer than 20 seconds to read the card, is set to
		 * fail
		 */
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				mHandler.sendEmptyMessage(NFC.NFCINTESTING);
			}
		}, 20000);

		/** Set the text flashing */
		mTimer2.schedule(new TimerTask() {

			@Override
			public void run() {
				if (mSetTextVisiable) {
					mHandler.sendEmptyMessage(TVSETVISIABLE);
					mSetTextVisiable = false;
				} else {
					mHandler.sendEmptyMessage(TVSETINVISIABLE);
					mSetTextVisiable = true;
				}
			}
		}, 0, 700);

	}
}
