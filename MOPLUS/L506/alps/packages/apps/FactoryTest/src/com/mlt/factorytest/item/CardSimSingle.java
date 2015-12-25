package com.mlt.factorytest.item;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mlt.factorytest.R;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.sax.StartElementListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;


//simCard
import com.android.internal.telephony.PhoneConstants;
import com.mediatek.telephony.TelephonyManagerEx;
import com.mlt.factorytest.ItemTestActivity;

//import com.mediatek.common.featureoption.FeatureOption;
//import com.mediatek.gemini.GeminiUtils;
//import com.mediatek.gemini.SimInfoRecord;
import android.telephony.SubscriptionManager;
//import android.telephony.SubInfoRecord;//yutianliang delete
import android.telephony.SubscriptionInfo;//yutianliang add 

////memoryCard
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.os.SystemProperties;
import android.content.res.Resources;

/**
 * @ClassName: SimCardAndSingle
 * @Description: This kind of mobile phone CARDS, memory CARDS and call test
 * @Function: 1, read the sim card information, to determine whether a sim card
 *            2, read the memory card information, to determine whether a memory
 *            card 3, set up the call button, click to dial the phone number of
 *            the corresponding
 * @author: peisaisai
 * @date: 2015-01-15 13:47:25 Copyright (c) 2015, malata All Rights Reserved.
 */
public class CardSimSingle extends AbsHardware {
    private Context context;

    // Four buttons respectively unicom
    // calls button and dial the mobile phone button
    private Button mbtnCallSim1, mbtnCallSim2;

    // Used to display the memory card information, sim1 test results, sim2 test
    // results,
    private TextView mtvSim1Card,
                     mtvSim2Card,
                     mtvTCard,
                     mtvEmmcCard,
                     mtvEmmcFATCard,
                     mtvUSBCard,
                     mtvTSize,
                     mtvEmmcSize,
                     mtvEmmcFATSize,
                     mtvUSBSize;

    // If the mode is single card mode, then display only mRelSim2,or all can
    // disappear
    private RelativeLayout mRelSim1, mRelSim2;

    // Mobile and China unicom's phone number
    private final int UNICOM_PHONENUMBER = 10010;
    private final int MOBILE_PHONENUMBER = 10086;

    private  String TNAME = "";
    private final String FLASHNAME = "Phone storage";
    private  String USBNAME = "";

    // the message to update the view
    private final int UPDATE_SIMCARDANDSIGLE = 0;

    // the flag of unicom or mobile
    private final int UNICOM_FLAG = 1;
    private final int MOBILE_FLAG = 2;
    private final int NULL_FLAG = 0;

    // Memory card information management class instances
    private StorageManager mStorageManager = null;

    // Sim card information management instance
    private TelephonyManagerEx mTelephonyManagerEx;

    // the flag to judge the EMMC,SDcard,sim1,sim2 and usb etc
    private boolean mIsMountedEmmc,
                    mIsMountedEmmcFAT,
                    mIsMountedUSB,
                    mIsMountedT,
                    mIsMountedSim1,
                    mIsMountedSim2;

    private float mEmmcTotalSize,
                  mEmmcFATTotalSize,
                  mTTotalSize,
                  mTAvailableSize,
                  mUSBTotalSize,
                  mUSBAvailableSize;
    
    private int mSim1CardType, mSim2CardType;
    private long mSim1CardInfoId, mSim2CardInfoId;

    // a list store the siminfo variable
    //private List<SubInfoRecord> mSimInfoList = new ArrayList<SubInfoRecord>();//yutianliang delete
    private List<SubscriptionInfo> mSimInfoList = new ArrayList<SubscriptionInfo>();//yutianliang add

    public CardSimSingle(String text, Boolean visible) {
        super(text, visible);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        /** set the acitivty title */
        // ItemTestActivity.itemActivity.setTitle(R.string.item_SIM);
        Resources r = Resources.getSystem();
        TNAME = Resources.getSystem().getText(com.android.internal.R.string.storage_sd_card).toString();
        USBNAME = Resources.getSystem().getString(com.android.internal.R.string.storage_external_usb);
        getSimInfo();
    }

    private void getSimInfo() {
        Log.d("pss", "getSimInfo()");
        //mSimInfoList = SubscriptionManager.getAllSubInfoList();//yutianliang delete
        mSimInfoList = SubscriptionManager.from(context).getAllSubscriptionInfoList();//yutianliang add
        int mSimNum = mSimInfoList.size();
        Log.d("pss", "total inserted sim card =" + mSimNum);
        // Collections.sort(mSimInfoList, new GeminiUtils.SIMInfoComparable());
        // for debug purpose to show the actual sim information
        int slot;
        for (int i = 0; i < mSimInfoList.size(); i++) {
            //slot = mSimInfoList.get(i).slotId;//yutialiang delete
        	slot = mSimInfoList.get(i).getSimSlotIndex();//yutialiang add
            if (slot == 0) {
                //mSim1CardInfoId = mSimInfoList.get(i).subId;//yutialiang delete
            	mSim1CardInfoId = mSimInfoList.get(i).getSubscriptionId();//yutialiang add
            } else if (slot == 1) {
                //mSim2CardInfoId = mSimInfoList.get(i).subId;//yutialiang delete
                mSim2CardInfoId = mSimInfoList.get(i).getSubscriptionId();//yutialiang add
            }
            //Log.i("pss", "siminfo.mSimSlotId = " + slot + "subid = "
                    //+ mSimInfoList.get(i).subId);
        }
    }

    private void loadSimFlashInfoThread() {
        // TODO Auto-generated method stub
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                // check Sim Card
                simCardTest();

                // check memory Card
                memoryCardTest();
                
                Message msg = new Message();
                msg.what = UPDATE_SIMCARDANDSIGLE;
                handler.sendMessage(msg);
            }
        }).start();
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

    }

    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // Unicom calls
        mbtnCallSim1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                setDefaultSIM(context, Settings.System.VOICE_CALL_SIM_SETTING,
                        mSim1CardInfoId);

                if (UNICOM_FLAG == mSim1CardType) {
                    callUnicom();
                } else if (MOBILE_FLAG == mSim1CardType) {
                    callMobile();
                }
            }
        });
        // Mobile calls
        mbtnCallSim2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                setDefaultSIM(context, Settings.System.VOICE_CALL_SIM_SETTING,
                        mSim2CardInfoId);
                if (UNICOM_FLAG == mSim2CardType) {
                    callUnicom();
                } else if (MOBILE_FLAG == mSim2CardType) {
                    callMobile();
                }
            }
        });
        loadSimFlashInfoThread();
    }

    /**
     * @MethodName: getDefaultSIM
     * @Description:get the cell phone the current default SIM
     * @param context
     * @param businessType
     * @return
     * @return long
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    public static long getDefaultSIM(Context context, String businessType) {
        return Settings.System.getLong(context.getContentResolver(),
                businessType, -1);
    }

    /**
     * @MethodName: setDefaultSIM
     * @Description:Set the cell phone the current default SIM
     * @param context
     * @param businessType
     * @param simId
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    public static void setDefaultSIM(Context context, String businessType,
            long simId) {
        Settings.System.putLong(context.getContentResolver(), businessType,
                simId);
    }

    /**
     * @MethodName: callMobile
     * @Description:Dial the mobile phone
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void callMobile() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(context
                .getString(R.string.mtvtel) + String.valueOf(MOBILE_PHONENUMBER)));
        ItemTestActivity.itemActivity.startActivity(intent);
    }

    /**
     * @MethodName: callUnicom
     * @Description:Dial the unicom phone
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void callUnicom() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(context
                .getString(R.string.mtvtel) + String.valueOf(UNICOM_PHONENUMBER)));
        ItemTestActivity.itemActivity.startActivity(intent);
    }

    /**
     * @MethodName: memoryCardTest
     * @Description: Through getVolumeList () to obtain memory card information,
     *               information obtained through the custom class mountPoint
     *               memory card, through tv_TFlash test results displayed.
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void memoryCardTest() {
        // TODO Auto-generated method stub
        mStorageManager = (StorageManager) context
                .getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] storageVolumeList = mStorageManager.getVolumeList();
        if (storageVolumeList != null) {
            for (StorageVolume volume : storageVolumeList) {
                MountPoint mountPoint = new MountPoint();
                mountPoint.mDescription = volume.getDescription(context);
                mountPoint.mPath = volume.getPath();
                try {
                    StatFs stat = new StatFs(volume.getPath());
                    long blockSize = stat.getBlockSize();
                    long availableBlocks = stat.getAvailableBlocks();
                    long totalSize = (stat.getTotalBytes() / 1024 / 1024);
                    mountPoint.mIsMounted = isMounted(volume.getPath());
                    mountPoint.mMaxFileSize = (blockSize * availableBlocks
                            / 1024 / 1024);// volume.getMaxFileSize()
                    Log.i("pss", "name:" + mountPoint.mDescription + "size:"
                            + totalSize + "IsMounted:" + mountPoint.mIsMounted);

                    if (mountPoint.mDescription.contains(TNAME)) {
                        mTTotalSize = totalSize;
                        mTAvailableSize = mountPoint.mMaxFileSize;
                        mIsMountedT = mountPoint.mIsMounted;
                    } else if (mountPoint.mDescription.equals(USBNAME)) {
                        mUSBTotalSize = totalSize;
                        mUSBAvailableSize = mountPoint.mMaxFileSize;
                        mIsMountedUSB = mountPoint.mIsMounted;
                    }
                } catch (Exception e) {

                }
            }
        }
        if (externalMemoryAvailable()) {
            mIsMountedEmmc = true;
            mIsMountedEmmcFAT = true;
            mEmmcFATTotalSize = (float) getTotalExternalMemorySize() / 1024 / 1024;
            mEmmcTotalSize = (float) (getTotalInternalMemorySize() + getTotalExternalMemorySize()) / 1024 / 1024 / 1024;
        }
    }

    //
    /**
     * @ClassName: MountPoint
     * @Description: Class used to temporarily save memory card information
     * @Function: TODO ADD FUNCTION
     * @author: peisaisai
     * @date: 2015-01-15 14:01:26 Copyright (c) 2015, Malata All Rights
     *        Reserved.
     */
    private static class MountPoint {
        String mDescription;
        String mPath;
        boolean mIsExternal;
        boolean mIsMounted;
        long mMaxFileSize;
    }

    /**
     * This method checks whether SDcard is mounted or not
     * 
     * @param mountPoint
     *            the mount point that should be checked
     * @return true if SDcard is mounted, false otherwise
     */
    protected boolean isMounted(String mountPoint) {

        String state = null;
        state = mStorageManager.getVolumeState(mountPoint);
        // LogUtils.d(TAG, "state = " + state);
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * @MethodName: simCardTeXst
     * @Description:Is used to detect the sim card, if it is a single card,
     *                 displays sim1, if it is a dual sim card, display sim1 and
     *                 sim2.If you can detect the sim card, the corresponding
     *                 button will turn green, at the same time shows through.If
     *                 you don't read the sim card, the following two dial
     *                 button will not show
     * @return void
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private void simCardTest() {
        // TODO Auto-generated method stub

        String imsi_1, imsi_2;
        if (!isGeminiEnabled()) {
            // mRelSim2.setVisibility(View.INVISIBLE);
            TelephonyManager telManager = (TelephonyManager) context
                    .getSystemService(context.TELEPHONY_SERVICE);
            if (telManager.getSubscriberId() != null) {
                mIsMountedSim1 = true;
                String operator = telManager.getSimOperator();
                if (operator != null) {
                    if (operator.equals("46000") || operator.equals("46002")
                            || operator.equals("46007")) {
                        mSim1CardType = MOBILE_FLAG;
                    } else if (operator.equals("46001")) {
                        mSim1CardType = UNICOM_FLAG;
                    } else{
                        mSim1CardType = MOBILE_FLAG;
                    }
                }
            } else {
                mIsMountedSim1 = false;
            }
        } else {
            mTelephonyManagerEx = TelephonyManagerEx.getDefault();
            imsi_1 = mTelephonyManagerEx
                    .getSubscriberId(PhoneConstants.SIM_ID_1);// GEMINI_SIM_1
            imsi_2 = mTelephonyManagerEx
                    .getSubscriberId(PhoneConstants.SIM_ID_2);// GEMINI_SIM_2
            Log.i("pss", "imsi_1:" + imsi_1);
            Log.i("pss", "imsi_2:" + imsi_2);
            if (imsi_1 != null) {
                mIsMountedSim1 = true;
            } else {
                mIsMountedSim1 = false;
            }
            if (imsi_2 != null) {
                mIsMountedSim2 = true;
            } else {
                mIsMountedSim2 = false;
            }
            if ((imsi_1 == null) && (imsi_2 == null)) {
                mSim1CardType = NULL_FLAG;
                mSim2CardType = NULL_FLAG;
            } else if ((imsi_1 != null) || (imsi_2 != null)) {

                if (MOBILE_FLAG == judgeSimMode(imsi_1)) {
                    mSim1CardType = MOBILE_FLAG;
                } else if (UNICOM_FLAG == judgeSimMode(imsi_1)) {
                    mSim1CardType = UNICOM_FLAG;
                } else {
                    mSim1CardType = NULL_FLAG;
                }
                if (MOBILE_FLAG == judgeSimMode(imsi_2)) {
                    mSim2CardType = MOBILE_FLAG;
                } else if (UNICOM_FLAG == judgeSimMode(imsi_2)) {
                    mSim2CardType = UNICOM_FLAG;
                } else {
                    mSim2CardType = NULL_FLAG;
                }
            }
        }
    }

    /**
     * @MethodName: judgeSimMode
     * @Description:Judgment is sim card mobile or unicom
     * @param imsi
     * @return
     * @return int
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    private int judgeSimMode(String imsi) {
        if (imsi != null) {
            if (imsi.startsWith("46000") || imsi.startsWith("46002")
                    || imsi.startsWith("46007")) {
                return MOBILE_FLAG;
            } else if (imsi.startsWith("46001")) {
                return UNICOM_FLAG;
            }
        }
        return 0;
    }

    /**
     * @MethodName: isGeminiEnabled
     * @Description:Detection of mobile phone is single or double card,return
     *                        true is single,or double
     * @return
     * @return boolean
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    public static boolean isGeminiEnabled() {
        // return FeatureOption.MTK_GEMINI_SUPPORT;
        return getValue("ro.mtk_gemini_support");
    }
    
    private static boolean getValue(String key) {
        return SystemProperties.get(key).equals("1");
    }
    
    /**
     * @MethodName: getTotalInternalMemorySize
     * @Description: Access to mobile phone memory size data area
     * @return
     * @return long
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    public long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    public boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * @MethodName: getTotalExternalMemorySize
     * @Description:Get FAT storage size
     * @return
     * @return long
     * @throws Copyright
     *             (c) 2015, Malata All Rights Reserved.
     */
    public long getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } else {
            return -1;
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (UPDATE_SIMCARDANDSIGLE == msg.what) {
                DecimalFormat decimalFormat = new DecimalFormat(".00");
                if (mIsMountedT) {
                    mtvTCard.setText(context.getText(R.string.mtvhascard));
                    mtvTCard.setBackgroundColor(Color.GREEN);
                    mtvTSize.setText(mTAvailableSize
                            + context.getString(R.string.mtvseperator)
                            + mTTotalSize + context.getString(R.string.mtvmb));
                } else {
                    mtvTCard.setText(context.getText(R.string.mtvnocard));
                    mtvTCard.setBackgroundColor(Color.RED);
                }
                if (mIsMountedEmmc) {
                    mtvEmmcCard.setText(context.getText(R.string.mtvhascard));
                    mtvEmmcCard.setBackgroundColor(Color.GREEN);
                    mtvEmmcSize.setText(decimalFormat.format(mEmmcTotalSize)
                            + context.getString(R.string.mtvgb));
                } else {
                    mtvEmmcCard.setText(context.getText(R.string.mtvnocard));
                    mtvEmmcCard.setBackgroundColor(Color.RED);
                }
                if (mIsMountedEmmcFAT) {
                    mtvEmmcFATCard.setText(context.getString(R.string.mtvhascard));
                    mtvEmmcFATCard.setBackgroundColor(Color.GREEN);
                    mtvEmmcFATSize.setText(decimalFormat
                            .format(mEmmcFATTotalSize)
                            + context.getString(R.string.mtvmb));
                } else {
                    mtvEmmcFATCard.setText(context.getString(R.string.mtvnocard));
                    mtvEmmcFATCard.setBackgroundColor(Color.RED);
                }
                if (mIsMountedUSB) {
                    mtvUSBCard.setText(context.getText(R.string.mtvhascard));
                    mtvUSBCard.setBackgroundColor(Color.GREEN);
                    mtvUSBSize.setText(mUSBAvailableSize
                            + context.getString(R.string.mtvseperator)
                            + mUSBTotalSize + context.getString(R.string.mtvmb));
                } else {
                    mtvUSBCard.setText(context.getText(R.string.mtvnocard));
                    mtvUSBCard.setBackgroundColor(Color.RED);
                }
                if (mIsMountedSim1) {
                    mbtnCallSim1.setVisibility(View.VISIBLE);
                    mtvSim1Card.setBackgroundColor(Color.GREEN);
                    mtvSim1Card.setText(context.getText(R.string.mtvhascard));
                } else {
                    mtvSim1Card.setBackgroundColor(Color.RED);
                    mtvSim1Card.setText(context.getText(R.string.mtvnocard));
                }
                if (mIsMountedSim2) {
                    mbtnCallSim2.setVisibility(View.VISIBLE);
                    mtvSim2Card.setText(context.getText(R.string.mtvhascard));
                    mtvSim2Card.setBackgroundColor(Color.GREEN);
                } else {
                    mtvSim2Card.setText(context.getText(R.string.mtvnocard));
                    mtvSim2Card.setBackgroundColor(Color.RED);
                }
                if (UNICOM_FLAG == mSim1CardType) {
                    mbtnCallSim1.setText(context
                            .getString(R.string.mbtsim1callunicom));
                } else if (MOBILE_FLAG == mSim1CardType) {
                    mbtnCallSim1.setText(context
                            .getString(R.string.mbtsim1callmobile));
                }
                if (UNICOM_FLAG == mSim2CardType) {
                    mbtnCallSim2.setText(context
                            .getString(R.string.mbtsim2callunicom));
                } else if (MOBILE_FLAG == mSim2CardType) {
                    mbtnCallSim2.setText(context
                            .getString(R.string.mbtsim2callmobile));
                }
            }
        };
    };

    @Override
    public View getView(Context context) {
        // TODO Auto-generated method stub
        this.context = context;
        LayoutInflater factory = LayoutInflater.from(context);
        View view = factory.inflate(R.layout.item_sim_flash, null);
        mtvSim1Card = (TextView) view.findViewById(R.id.mtvsim1card);
        mtvSim2Card = (TextView) view.findViewById(R.id.mtvsim2card);
        mtvEmmcFATCard = (TextView) view.findViewById(R.id.mtvemmcfatcard);
        mtvEmmcCard = (TextView) view.findViewById(R.id.mtvemmccard);
        mtvTCard = (TextView) view.findViewById(R.id.mtvtcard);
        mtvUSBCard = (TextView) view.findViewById(R.id.mtvusbcard);
        mtvEmmcFATSize = (TextView) view.findViewById(R.id.mtvemmcfatsize);
        mtvTSize = (TextView) view.findViewById(R.id.mtvtsize);
        mtvEmmcSize = (TextView) view.findViewById(R.id.mtvemmcsize);
        mtvUSBSize = (TextView) view.findViewById(R.id.mtvusbsize);
        mRelSim1 = (RelativeLayout) view.findViewById(R.id.mrelsim1);
        mRelSim2 = (RelativeLayout) view.findViewById(R.id.mrelsim2);
        mbtnCallSim1 = (Button) view.findViewById(R.id.mbtsim1);
        mbtnCallSim2 = (Button) view.findViewById(R.id.mbtsim2);
        return view;
    }
}
