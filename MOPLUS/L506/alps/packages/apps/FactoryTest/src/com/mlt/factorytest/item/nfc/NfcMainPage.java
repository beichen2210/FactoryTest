package com.mlt.factorytest.item.nfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.mlt.factorytest.R;
import com.mlt.factorytest.item.nfc.ShellExe;
import com.mlt.factorytest.item.nfc.NfcCommand.CommandType;

import java.io.IOException;

public class NfcMainPage extends Activity {

    public static final String TAG = "HGX";
    private static final String START_LIB_COMMAND = "./system/xbin/nfcstackp";
    private static final int DIALOG_WARN = 1;
    private ConnectServerTask mTask;
    private boolean mShowDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_nfc);
        NfcAdapter adp = NfcAdapter.getDefaultAdapter(getApplicationContext());
        if (adp != null && adp.isEnabled()) {
            showDialog(DIALOG_WARN);
            mShowDialog = true;
            return;
        }
        executeXbinFile(START_LIB_COMMAND, 500);
        mTask = new ConnectServerTask();
        mTask.execute();
    }

    protected void onDestroy() {
        if(mShowDialog == false) {
            NfcClient.getInstance().sendCommand(CommandType.MTK_NFC_EM_STOP_CMD, null);
            NfcClient.getInstance().closeConnection();
            mTask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = null;
        switch (id) {
        case DIALOG_WARN:
            builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.hqa_nfc_dialog_warn);
            builder.setCancelable(false);
            builder.setMessage(getString(R.string.hqa_nfc_dialog_warn_message));
            builder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            dialog = builder.create();
            break;
        default:
            Log.i(TAG, "error dialog ID");
            break;
        }
        return dialog;
    }


    private void executeXbinFile(final String command, int sleepTime) {
        new Thread() {
            @Override
            public void run() {
                    Log.i(TAG, "[NfcMainPage]nfc command:" + command);
                try {
                    int err = ShellExe.execCommand(command);
                    Log.i(TAG, "[NfcMainPage]nfc command:result: " + err);
                } catch (IOException e) {
                    Log.i(TAG, "[NfcMainPage]executeXbinFile IOException: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.i(TAG, "[NfcMainPage]executeXbinFile InterruptedException: " + e.getMessage());
        }
    }

    private class ConnectServerTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            return NfcClient.getInstance().createConnection(NfcMainPage.this);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (null != result && result.booleanValue()) {
            /*	Log.i(NfcMainPage.TAG, "onpostexecute");
                PreferenceScreen screen = getPreferenceScreen();
                int count = screen.getPreferenceCount();
                for (int index = 0; index < count; index++) {
                    screen.getPreference(index).setEnabled(true);
                }*/
                NfcClient.getInstance().sendCommand(CommandType.MTK_NFC_EM_START_CMD, null);
            } else {
                Toast.makeText(NfcMainPage.this, R.string.hqa_nfc_connect_fail, Toast.LENGTH_SHORT).show();
                // NfcMainPage.this.finish();
            }
        }
    }

    // TODO: remove "\\"
}
