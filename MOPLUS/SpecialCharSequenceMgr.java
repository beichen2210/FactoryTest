/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.dialer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
//cqf add for MOPLUES-11 20151105 start
import android.os.SystemProperties; 
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import android.content.BroadcastReceiver;
import com.android.internal.os.storage.ExternalStorageFormatter;
import android.content.DialogInterface.OnClickListener;
import android.app.AlertDialog.Builder;
import android.view.WindowManager.LayoutParams;
import com.mediatek.xlog.Xlog;
//cqf add for MOPLUES-11 20151105 end

import com.android.common.io.MoreCloseables;
import com.android.contacts.common.database.NoNullCursorAsyncQueryHandler;
import com.android.contacts.common.widget.SelectPhoneAccountDialogFragment;
import com.android.contacts.common.widget.SelectPhoneAccountDialogFragment.SelectPhoneAccountListener;
import com.android.dialer.calllog.PhoneAccountUtils;

import com.android.internal.telephony.ITelephony;
import com.mediatek.dialer.ext.ExtensionManager;
import com.mediatek.dialer.util.DialerFeatureOptions;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to listen for some magic character sequences
 * that are handled specially by the dialer.
 *
 * Note the Phone app also handles these sequences too (in a couple of
 * relatively obscure places in the UI), so there's a separate version of
 * this class under apps/Phone.
 *
 * TODO: there's lots of duplicated code between this class and the
 * corresponding class under apps/Phone.  Let's figure out a way to
 * unify these two classes (in the framework? in a common shared library?)
 */
public class SpecialCharSequenceMgr {
    private static final String TAG = "SpecialCharSequenceMgr";

    private static final String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";
    private static final String MMI_IMEI_DISPLAY = "*#06#";
    private static final String MMI_REGULATORY_INFO_DISPLAY = "*#07#";
    private static final boolean DEBUG = DialtactsActivity.DEBUG;
    //cqf add for MOPLUES-11 20151105 start
    private static final String ENGINEER_MODE_ENTRY = "*953*1#";
    private static final String INTERNAL_VERSION_ENTRY = "*658*1#";
    private static final String FACTORY_RESET_SETTINGS = "*#119*#";
    private static final boolean DIALOG_FEATURE = true;
    //cqf add for MOPLUES-11 20151105 end
    //chb add for factorytest  start 2015-11-5 10:16:32
    private static final String FACTORYTEST_MODE_MALATA_ENTRY = "*953*2#";
    //chb add for factorytest  end 2015-11-5 10:16:44

    /**
     * Remembers the previous {@link QueryHandler} and cancel the operation when needed, to
     * prevent possible crash.
     *
     * QueryHandler may call {@link ProgressDialog#dismiss()} when the screen is already gone,
     * which will cause the app crash. This variable enables the class to prevent the crash
     * on {@link #cleanup()}.
     *
     * TODO: Remove this and replace it (and {@link #cleanup()}) with better implementation.
     * One complication is that we have SpecialCharSequenceMgr in Phone package too, which has
     * *slightly* different implementation. Note that Phone package doesn't have this problem,
     * so the class on Phone side doesn't have this functionality.
     * Fundamental fix would be to have one shared implementation and resolve this corner case more
     * gracefully.
     */
    private static QueryHandler sPreviousAdnQueryHandler;
    private static final String ICC_ADN_SUBID_URI = "content://icc/adn/subId/";

    /** This class is never instantiated. */
    private SpecialCharSequenceMgr() {
    }

    public static boolean handleChars(Context context, String input, EditText textField) {
        /// M: for ALPS01692450 @{
        // check null
        if(context == null) {
            return false;
        }
        /// @}

        //get rid of the separators so that the string gets parsed correctly
        String dialString = PhoneNumberUtils.stripSeparators(input);

        if (handleDeviceIdDisplay(context, dialString)
                || handleRegulatoryInfoDisplay(context, dialString)
                || handlePinEntry(context, dialString)
                || handleAdnEntry(context, dialString, textField)
                || handleFactoryResetSettings(context,dialString)//cqf add MOPLUES-11 for reset factory settings version 20150313
                || handleQueryInternalVersionEntry(context,dialString) //cqf add MOPLUES-11 for query internal version 20150313
                || handleEngineerModeEntry(context,dialString) //cqf add MOPLUES-11 for ENGINEER MODE 20150313
                || handleSecretCode(context, dialString)
                || handleFactoryTestModeEntry(context,dialString) //chb add for factorytest 2015-11-5 10:18:12
                /// M: for plug-in @{
                || ExtensionManager.getInstance().getDialPadExtension().handleChars(context, dialString)
                /// @}
                ) {
            return true;
        }

        return false;
    }

    /**
     * Cleanup everything around this class. Must be run inside the main thread.
     *
     * This should be called when the screen becomes background.
     */
    public static void cleanup() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Log.wtf(TAG, "cleanup() is called outside the main thread");
            return;
        }

        if (sPreviousAdnQueryHandler != null) {
            sPreviousAdnQueryHandler.cancel();
            sPreviousAdnQueryHandler = null;
        }
    }

    /**
     * Handles secret codes to launch arbitrary activities in the form of *#*#<code>#*#*.
     * If a secret code is encountered an Intent is started with the android_secret_code://<code>
     * URI.
     *
     * @param context the context to use
     * @param input the text to check for a secret code in
     * @return true if a secret code was encountered
     */
    static boolean handleSecretCode(Context context, String input) {
        // Secret codes are in the form *#*#<code>#*#*

        /// M: for plug-in @{
        input = ExtensionManager.getInstance().getDialPadExtension().handleSecretCode(input);
        /// @}

        int len = input.length();
        if (len > 8 && input.startsWith("*#*#") && input.endsWith("#*#*")) {
            final Intent intent = new Intent(SECRET_CODE_ACTION,
                    Uri.parse("android_secret_code://" + input.substring(4, len - 4)));
            context.sendBroadcast(intent);
            return true;
        }

        return false;
    }

    //chb add for factorytest start 2015-11-5 10:19:46
    static private boolean handleFactoryTestModeEntry(Context context,String input){
        if(input.equals(FACTORYTEST_MODE_MALATA_ENTRY)){
               Intent intentEm = new Intent("com.mlt.factorytest.action.TEST_LIST");
               intentEm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               context.startActivity(intentEm);                
               return true;
        }       
       return false;       
    } 
    //chb add for factorytest end 2015-11-5 10:19:53
    
    /**
     * Handle ADN requests by filling in the SIM contact number into the requested
     * EditText.
     *
     * This code works alongside the Asynchronous query handler {@link QueryHandler}
     * and query cancel handler implemented in {@link SimContactQueryCookie}.
     */
    static boolean handleAdnEntry(final Context context, String input, EditText textField) {
        /* ADN entries are of the form "N(N)(N)#" */

        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        /** M: Bug Fix for ALPS02007941 @{ */
        if (telephonyManager == null
                || (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_GSM
                    && telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA)) {
            return false;
        }
        /** @} */
        // if the phone is keyguard-restricted, then just ignore this
        // input.  We want to make sure that sim card contacts are NOT
        // exposed unless the phone is unlocked, and this code can be
        // accessed from the emergency dialer.
        KeyguardManager keyguardManager =
                (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.inKeyguardRestrictedInputMode()) {
            return false;
        }

        int len = input.length();
        if ((len > 1) && (len < 5) && (input.endsWith("#"))) {
            try {
                // get the ordinal number of the sim contact
                final int index = Integer.parseInt(input.substring(0, len-1));

                /// M: ALPS01760178, The index of contacts saved in sim account starts from 1. @{ 
                if (index <= 0) {
                    return false;
                }
                /// @}

                // The original code that navigated to a SIM Contacts list view did not
                // highlight the requested contact correctly, a requirement for PTCRB
                // certification.  This behaviour is consistent with the UI paradigm
                // for touch-enabled lists, so it does not make sense to try to work
                // around it.  Instead we fill in the the requested phone number into
                // the dialer text field.

                // create the async query handler
                final QueryHandler handler = new QueryHandler (context.getContentResolver());

                // create the cookie object
                final SimContactQueryCookie sc = new SimContactQueryCookie(index, handler,
                        ADN_QUERY_TOKEN);

                /// M: Fix CR ALPS01863413. Record the ADN query cookie.
                sSimContactQueryCookie = sc;

                // setup the cookie fields
                /** M: delete, no need to setup twice
                sc.contactNum = index - 1;
                */
                sc.setTextField(textField);

                // create the progress dialog
                sc.progressDialog = new ProgressDialog(context);
                sc.progressDialog.setTitle(R.string.simContacts_title);
                sc.progressDialog.setMessage(context.getText(R.string.simContacts_emptyLoading));
                sc.progressDialog.setIndeterminate(true);
                sc.progressDialog.setCancelable(true);
                sc.progressDialog.setOnCancelListener(sc);
                sc.progressDialog.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

                /// M: for plug-in @{
                ExtensionManager.getInstance().getDialPadExtension().customADNProgressDialog(sc.progressDialog);
                /// @}

                final TelecomManager telecomManager =
                        (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
                List<PhoneAccountHandle> subscriptionAccountHandles =
                        PhoneAccountUtils.getSubscriptionPhoneAccounts(context);

                boolean hasUserSelectedDefault = subscriptionAccountHandles.contains(
                        telecomManager.getUserSelectedOutgoingPhoneAccount());

                if (subscriptionAccountHandles.size() == 1 || hasUserSelectedDefault) {
                    Uri uri = telecomManager.getAdnUriForPhoneAccount(null);
                    handleAdnQuery(handler, sc, uri);
                } else if (subscriptionAccountHandles.size() > 1){
                    SelectPhoneAccountListener listener = new SelectPhoneAccountListener() {
                        @Override
                        public void onPhoneAccountSelected(PhoneAccountHandle selectedAccountHandle,
                                boolean setDefault) {
                            Uri uri =
                                    telecomManager.getAdnUriForPhoneAccount(selectedAccountHandle);
                            handleAdnQuery(handler, sc, uri);
                            //TODO: show error dialog if result isn't valid
                        }
                        @Override
                        public void onDialogDismissed() {}
                    };

                    SelectPhoneAccountDialogFragment.showAccountDialog(
                            ((Activity) context).getFragmentManager(), subscriptionAccountHandles,
                            listener);
                } else {
                    return false;
                }

                return true;
            } catch (NumberFormatException ex) {
                // Ignore
            }
        }
        return false;
    }
    //cqf add for MOPLUES-11 20151105 start 
    static private boolean handleEngineerModeEntry(Context context,
                String input){
        if(input.equals(ENGINEER_MODE_ENTRY)){
            Intent intent = new Intent();
            intent.setClassName("com.mediatek.engineermode","com.mediatek.engineermode.EngineerMode");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        }
        return false;
    }
	
    static private boolean handleQueryInternalVersionEntry(Context context,
                String input) {
        if (input.equals(INTERNAL_VERSION_ENTRY)) {
            String strInternalVersion = SystemProperties.get("ro.build.mlt.internal.version") + "\n";
			strInternalVersion += getFormattedKernelVersion();  //caoqiaofeng add LFZS-158 20150925
            AlertDialog alert = new AlertDialog.Builder(context)
                .setTitle(R.string.query_internal_version)
                .setMessage(strInternalVersion)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false).create();
                alert.show();
                return true;
        }
        return false;
    }

	public static String readLine(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename),
				256);
		try {
			return reader.readLine();
		} finally {
			reader.close();
		}
	}

     public static String getFormattedKernelVersion() {
         try {
             return formatKernelVersion(readLine("/proc/version"));
         } catch (IOException e) {
             Log.e(TAG, "IO Exception when getting kernel version for Device Info screen", e);
         return "Unavailable";
         }
     }
	
     public static String formatKernelVersion(String rawKernelVersion) {
     
         final String PROC_VERSION_REGEX =
             "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
             "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
             "(?:\\(gcc.+? \\)) " +    /* ignore: GCC version information */
             "(#\\d+) " +              /* group 3: "#1" */
             "(?:.*?)?" +              /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
             "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */
         
         Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
         if (!m.matches()) {
             Log.e(TAG, "Regex did not match on /proc/version: " + rawKernelVersion);
             return "Unavailable";
         } else if (m.groupCount() < 4) {
             Log.e(TAG, "Regex match on /proc/version only returned " + m.groupCount()
             + " groups");
             return "Unavailable";
         }
         return m.group(4);  
    }
     
    static boolean 	handleFactoryResetSettings(Context context,String input){
        if(input.equals(FACTORY_RESET_SETTINGS)){
            final Context mContext = context;
            DialogInterface.OnClickListener mOnClickListenter = new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog,int which){
                    resetFactory(mContext);
                }
        };
        if(DIALOG_FEATURE){
            AlertDialog dialog = new AlertDialog.Builder(context).setTitle(context.getString(R.string.reset_factory_title))
                .setMessage(context.getString(R.string.reset_factory_context))
                .setPositiveButton(context.getString(R.string.reset_factory_ok),mOnClickListenter)
                .setNegativeButton(context.getString(R.string.reset_factory_cancel), null)
                .setCancelable(false)
                .create();
            dialog.show();
        }
        else{
            resetFactory(context);
        }
            return true;
        }
        return false;
    }
	
    private static void resetFactory(Context context){
        Intent intent1 = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
        intent1.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
        context.startService(intent1);
    }
    //tiandajiao add for MTSFEFL-17 end 20150313

    private static void handleAdnQuery(QueryHandler handler, SimContactQueryCookie cookie,
            Uri uri) {
        if (handler == null || cookie == null || uri == null) {
            Log.w(TAG, "queryAdn parameters incorrect");
            return;
        }

        // display the progress dialog
        cookie.progressDialog.show();

        // run the query.
        handler.startQuery(ADN_QUERY_TOKEN, cookie, uri, new String[]{ADN_PHONE_NUMBER_COLUMN_NAME},
                null, null, null);

        if (sPreviousAdnQueryHandler != null) {
            // It is harmless to call cancel() even after the handler's gone.
            sPreviousAdnQueryHandler.cancel();
        }
        sPreviousAdnQueryHandler = handler;
    }

    static boolean handlePinEntry(Context context, final String input) {
        if ((input.startsWith("**04") || input.startsWith("**05")) && input.endsWith("#")) {
            final TelecomManager telecomManager =
                    (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            List<PhoneAccountHandle> subscriptionAccountHandles =
                    PhoneAccountUtils.getSubscriptionPhoneAccounts(context);
            boolean hasUserSelectedDefault = subscriptionAccountHandles.contains(
                    telecomManager.getUserSelectedOutgoingPhoneAccount());

            if (subscriptionAccountHandles.size() == 1 || hasUserSelectedDefault) {
                // Don't bring up the dialog for single-SIM or if the default outgoing account is
                // a subscription account.
                return telecomManager.handleMmi(input);
            } else if (subscriptionAccountHandles.size() > 1){
                SelectPhoneAccountListener listener = new SelectPhoneAccountListener() {
                    @Override
                    public void onPhoneAccountSelected(PhoneAccountHandle selectedAccountHandle,
                            boolean setDefault) {
                        telecomManager.handleMmi(selectedAccountHandle, input);
                        //TODO: show error dialog if result isn't valid
                    }
                    @Override
                    public void onDialogDismissed() {}
                };

                SelectPhoneAccountDialogFragment.showAccountDialog(
                        ((Activity) context).getFragmentManager(), subscriptionAccountHandles,
                        listener);
            }
            return true;
        }
        return false;
    }

    // TODO: Use TelephonyCapabilities.getDeviceIdLabel() to get the device id label instead of a
    // hard-coded string.
    static boolean handleDeviceIdDisplay(Context context, String input) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if (telephonyManager != null && input.equals(MMI_IMEI_DISPLAY)) {
            int labelResId = (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) ?
                    R.string.imei : R.string.meid;

            String imei_invalid = context.getResources().getString(R.string.imei_invalid);
            List<String> deviceIds = new ArrayList<String>();
            for (int slot = 0; slot < telephonyManager.getPhoneCount(); slot++) {
                String imei = telephonyManager.getDeviceId(slot);
                deviceIds.add(TextUtils.isEmpty(imei) ? imei_invalid : imei);
            }

            /// M: for ALPS01954192 @{
            // Add single IMEI plugin       
            deviceIds = ExtensionManager.getInstance().getDialPadExtension().getSingleIMEI(deviceIds);
            /// @}

            AlertDialog alert = new AlertDialog.Builder(context)
                    .setTitle(labelResId)
                    .setItems(deviceIds.toArray(new String[deviceIds.size()]), null)
                    .setPositiveButton(android.R.string.ok, null)
                    .setCancelable(false)
                    .show();
            return true;
        }
        return false;
    }

    private static boolean handleRegulatoryInfoDisplay(Context context, String input) {
        if (input.equals(MMI_REGULATORY_INFO_DISPLAY)) {
            Log.d(TAG, "handleRegulatoryInfoDisplay() sending intent to settings app");
            Intent showRegInfoIntent = new Intent(Settings.ACTION_SHOW_REGULATORY_INFO);
            try {
                context.startActivity(showRegInfoIntent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "startActivity() failed: " + e);
            }
            return true;
        }
        return false;
    }

    /**
     * Get the Id of each slot
     * @return int array of slot Ids
     */
    private static int[] getSlotIds() {
        int slotCount = TelephonyManager.getDefault().getPhoneCount();
        int[] slotIds = new int[slotCount];
        for (int i = 0; i < slotCount; i++) {
            slotIds[i] = i;
        }
        return slotIds;
    }

    /*******
     * This code is used to handle SIM Contact queries
     *******/
    private static final String ADN_PHONE_NUMBER_COLUMN_NAME = "number";
    private static final String ADN_NAME_COLUMN_NAME = "name";
    private static final String ADN_ADDITIONAL_PHONE_NUMBER_COLUMN_NAME = "additionalNumber";

    /// M: ALPS01764940, Add index to indicate the queried contacts @{
    private static final String ADN_ID_COLUMN_NAME = "index";
    /// @}

    private static final int ADN_QUERY_TOKEN = -1;

    /**
     * Cookie object that contains everything we need to communicate to the
     * handler's onQuery Complete, as well as what we need in order to cancel
     * the query (if requested).
     *
     * Note, access to the textField field is going to be synchronized, because
     * the user can request a cancel at any time through the UI.
     */
    private static class SimContactQueryCookie implements DialogInterface.OnCancelListener{
        public ProgressDialog progressDialog;
        public int contactIndex;

        // Used to identify the query request.
        private int mToken;
        private QueryHandler mHandler;

        // The text field we're going to update
        private EditText textField;

        public SimContactQueryCookie(int index, QueryHandler handler, int token) {
            contactIndex = index;
            mHandler = handler;
            mToken = token;
        }

        /**
         * Synchronized getter for the EditText.
         */
        public synchronized EditText getTextField() {
            return textField;
        }

        /**
         * Synchronized setter for the EditText.
         */
        public synchronized void setTextField(EditText text) {
            textField = text;
        }

        /**
         * Cancel the ADN query by stopping the operation and signaling
         * the cookie that a cancel request is made.
         */
        public synchronized void onCancel(DialogInterface dialog) {
            /** M: Fix CR ALPS01863413. Call QueryHandler.cancel(). @{ */
            /* original code:
            // close the progress dialog
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            // setting the textfield to null ensures that the UI does NOT get
            // updated.
            textField = null;

            // Cancel the operation if possible.
            mHandler.cancelOperation(mToken);
            */
            mHandler.cancel();
            /** @} */
        }
    }

    /**
     * Asynchronous query handler that services requests to look up ADNs
     *
     * Queries originate from {@link #handleAdnEntry}.
     */
    private static class QueryHandler extends NoNullCursorAsyncQueryHandler {

        private boolean mCanceled;

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        /**
         * Override basic onQueryComplete to fill in the textfield when
         * we're handed the ADN cursor.
         */
        @Override
        protected void onNotNullableQueryComplete(int token, Object cookie, Cursor c) {
            try {
                sPreviousAdnQueryHandler = null;
                /// M: Fix CR ALPS01863413. Clear the ADN query cookie.
                sSimContactQueryCookie = null;

                if (mCanceled) {
                    return;
                }

                SimContactQueryCookie sc = (SimContactQueryCookie) cookie;

                // close the progress dialog.
                sc.progressDialog.dismiss();

                // get the EditText to update or see if the request was cancelled.
                EditText text = sc.getTextField();

                // if the textview is valid, and the cursor is valid and postionable
                // on the Nth number, then we update the text field and display a
                // toast indicating the caller name.

                String name = null;
                String number = null;
                String additionalNumber = null;

                if ((c != null) && (text != null)) {

                    while (c.moveToNext()) {
                        if (c.getInt(c.getColumnIndexOrThrow(ADN_ID_COLUMN_NAME)) == sc.contactIndex) {
                            name = c.getString(c.getColumnIndexOrThrow(ADN_NAME_COLUMN_NAME));
                            number = c.getString(c.getColumnIndexOrThrow(ADN_PHONE_NUMBER_COLUMN_NAME));
                            additionalNumber = c.getString(c.getColumnIndexOrThrow(ADN_ADDITIONAL_PHONE_NUMBER_COLUMN_NAME));
                            break;
                        }
                    }

                    // fill the text in.
                    if (!TextUtils.isEmpty(number)) {
                        text.getText().replace(0, 0, number);
                    } else if (!TextUtils.isEmpty(additionalNumber)) {
                        text.getText().replace(0, 0, additionalNumber);
                    }

                    // display the name as a toast
                    if (name != null) {
                        Context context = sc.progressDialog.getContext();
                        name = context.getString(R.string.menu_callNumber, name);
                        Toast.makeText(context, name, Toast.LENGTH_SHORT)
                            .show();
                    }

                }
            } finally {
                MoreCloseables.closeQuietly(c);
            }
        }

        public void cancel() {
            mCanceled = true;
            // Ask AsyncQueryHandler to cancel the whole request. This will fails when the
            // query already started.
            cancelOperation(ADN_QUERY_TOKEN);
            /// M: Fix CR ALPS01863413. Dismiss the progress and clear the ADN query cookie.
            if (sSimContactQueryCookie != null
                    && sSimContactQueryCookie.progressDialog != null) {
                sSimContactQueryCookie.progressDialog.dismiss();
                sSimContactQueryCookie = null;
            }
        }
    }

    /**
     * Query Adn from the specific subscription
     * @param handler
     * @param cookie
     * @param uri
     */
    private static void queryAdn(QueryHandler handler, SimContactQueryCookie cookie, Uri uri) {
        if (handler == null || cookie == null || uri == null) {
            Log.w(TAG, "queryAdn parameters incorrect");
            return;
        }

        // display the progress dialog
        cookie.progressDialog.show();

        if (DEBUG) {
            Log.d(TAG, "AdnQuery onSubPick, uri=" + uri);
        }
        handler.startQuery(ADN_QUERY_TOKEN, cookie, uri,
                    new String[] {ADN_PHONE_NUMBER_COLUMN_NAME, ADN_ID_COLUMN_NAME, ADN_ADDITIONAL_PHONE_NUMBER_COLUMN_NAME}, null, null, null);
        if (sPreviousAdnQueryHandler != null) {
            // It is harmless to call cancel() even after the handler's gone.
            sPreviousAdnQueryHandler.cancel();
        }
        sPreviousAdnQueryHandler = handler;
    }

    /**
     * Handle PinMmi by the specific subscription
     * @param subId The id of the subscription
     * @param input The PinMmi text
     */
    private static boolean handlePinMmi(int subId, String input) {
        try {
            boolean result = ITelephony.Stub.asInterface(ServiceManager.getService("phone"))
                    .handlePinMmiForSubscriber(subId, input);
            if (DEBUG) {
                Log.d(TAG, "Pin onSubPick(" + subId + ", " + input + ")=" + result);
            }
            return result;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to handlePinMmi due to remote exception");
            return false;
        }
    }

    private static boolean isValidSubId(long subId) {
        return subId > SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    }

    ///--------------------------Mediatek----------------------
    /** M: Fix CR ALPS01863413. Make the progress dismiss after the ADN query be cancelled.
     *  And make it support screen rotation while phone account pick dialog shown. @{ */
    private static SimContactQueryCookie sSimContactQueryCookie;

    /**
     * For ADN query with multiple phone accounts. If the the phone account pick
     * dialog shown, then rotate the screen and select one account to query ADN.
     * The ADN result would write into the old text view because the views
     * re-created but the class did not known. So, the dialpad fragment should
     * call this method to update the digits text filed view after it be
     * re-created.
     *
     * @param textFiled
     *            the digits text filed view
     */
    public static void updateTextFieldView(EditText textFiled) {
        if (sSimContactQueryCookie != null) {
            sSimContactQueryCookie.setTextField(textFiled);
        }
    }
    /** @} */
}
